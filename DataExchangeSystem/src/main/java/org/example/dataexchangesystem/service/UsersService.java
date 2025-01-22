package org.example.dataexchangesystem.service;


import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.transaction.Transactional;
import org.example.dataexchangesystem.azure.AzureFileStorageClient;
import org.example.dataexchangesystem.exception.FileReadException;
import org.example.dataexchangesystem.exception.UserNotFoundException;
import org.example.dataexchangesystem.model.BlobDTO;
import org.example.dataexchangesystem.model.UserFile;
import org.example.dataexchangesystem.model.UsersDTO;
import org.example.dataexchangesystem.repository.UserFilesRepository;
import org.example.dataexchangesystem.repository.UsersRepository;
import org.example.dataexchangesystem.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class UsersService {

    @Autowired
    AuthenticationManager authenticationManager;
    @Autowired
    JwtService jwtService;
    @Autowired
    UsersRepository usersRepository;
    @Autowired
    UserFilesRepository userFilesRepository;
    @Autowired
    private AzureFileStorageClient azureFileStorageClient;

    public String login(UsersDTO user) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        }

        return "Fail";
    }

//    public Users addUser(UsersDTO userDTO) {
//
//        Users user = new Users();
//        user.setUsername(userDTO.getUsername());
//        user.setPassword(new BCryptPasswordEncoder(12).encode(userDTO.getPassword()));
//
//        return usersRepository.save(user);
//    }

    @Transactional
    public void registerUser(UsersDTO userDTO) {
        usersRepository.findByUsername(userDTO.getUsername())
                .ifPresent(user -> {
                    throw new IllegalArgumentException("Username is already taken");
                });


        if (!isValidPassword(userDTO.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters long, " +
                    "contain at least one letter, one digit, and one special character.");
        }

        Users user = new Users();
        user.setUsername(userDTO.getUsername());
        user.setPassword(new BCryptPasswordEncoder(12).encode(userDTO.getPassword()));
        user.setCreatedAt(LocalDateTime.now());

        usersRepository.save(user);
    }

    public BlobDTO uploadFile(MultipartFile file, String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje." +
                                                            " Błąd przy wgrywaniu plików."));

        BlobDTO blobDTO;

        String uniqueFileName = username + "_" + file.getOriginalFilename();

        try (InputStream inputStream = file.getInputStream()) {
            blobDTO = this.azureFileStorageClient.uploadFile(
                    "file-container",
                    uniqueFileName,
                    inputStream,
                    file.getSize());
        }
        catch (IOException e) {
            throw new FileReadException("Failed to read file: " + file.getOriginalFilename(), e);
        }

        if (blobDTO == null) {
            throw new IllegalStateException("File upload failed but no exception was thrown");
        }


        UserFile userFile = new UserFile(user, uniqueFileName, blobDTO.getBlobUrl());
        userFilesRepository.save(userFile);

        String displayName = blobDTO.getBlobName().substring(username.length() + 1);
        blobDTO.setBlobName(displayName);
        return blobDTO;
    }

    public List<BlobDTO> uploadArchive(MultipartFile archive, String username) {
        Users user = usersRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje."));

        List<BlobDTO> blobDTOList = new ArrayList<>();

        try(ZipInputStream zipInputStream = new ZipInputStream(archive.getInputStream())) {
            ZipEntry zipEntry;
            while((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && !zipEntry.getName().contains("..")) {
                    String fileName = zipEntry.getName();
                    String uniqueFileName = username + "_" + fileName;
                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;

                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);
                    }

                    byte[] fileBytes = byteArrayOutputStream.toByteArray();
                    long fileSize = fileBytes.length;

                    try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                        BlobDTO blobDTO = azureFileStorageClient.uploadFile("file-container", uniqueFileName, inputStream, fileSize);

                        String displayName = blobDTO.getBlobName().substring(username.length() + 1);
                        blobDTO.setBlobName(displayName);
                        blobDTOList.add(blobDTO);

                        UserFile userFile = new UserFile(user, uniqueFileName, blobDTO.getBlobUrl());
                        userFilesRepository.save(userFile);
                    }
                }

            }
        } catch (IOException e) {
            throw new FileReadException("Failed to read file of archive: " + archive.getOriginalFilename(), e);
        }

        return blobDTOList;
    }

    public void deleteFile(String fileName, String username) throws FileNotFoundException {
        usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje."));

        String uniqueFileName = username + "_" + fileName;

        UserFile userFile = userFilesRepository.findByBlobName(uniqueFileName)
                .orElseThrow(() -> new FileNotFoundException("File with name " + uniqueFileName + " does not exist for user " + username));

        azureFileStorageClient.deleteFile("file-container", uniqueFileName);
        userFilesRepository.delete(userFile);
    }

    public List<BlobDTO> getFiles(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));

        List<UserFile> userFiles = userFilesRepository.findByUserId(user.getId());

        List<BlobDTO> blobDTOList = new ArrayList<>();

        for (UserFile userFile : userFiles) {
            String blobName = userFile.getBlobName();
            String displayName = blobName.substring(username.length() + 1);

            String blobUrl = userFile.getBlobUrl();

            blobDTOList.add(new BlobDTO(displayName, blobUrl));
        }

        return blobDTOList;
    }

    //Auxiliary methods

    private boolean isValidPassword(String password) {
        if (StringUtils.isBlank(password) || password.length() < 8) {
            return false;
        }

        String regex = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[!@#$%^&*()_+\\-=\\[\\]{};:'\",.<>/?]).{8,}$";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(password);

        return matcher.matches();
    }
}
