package org.example.dataexchangesystem.service;


import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.transaction.Transactional;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.example.dataexchangesystem.azure.AzureFileStorageClient;
import org.example.dataexchangesystem.azure.FileStorageClient;
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

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private FileStorageClient fileStorageClient;
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

    public BlobDTO uploadFile(MultipartFile file, String username) throws IOException {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje." +
                                                            " Błąd przy wgrywaniu plików."));

        BlobDTO blobDTO;

        try (InputStream inputStream = file.getInputStream()) {
            blobDTO = this.fileStorageClient.uploadFile(
                    "file-container",
                    file.getOriginalFilename(),
                    inputStream,
                    file.getSize());
        } catch (FileUploadException e) {
            throw new FileUploadException("Failed to upload file: " + file.getOriginalFilename()
                                            + "due to" + e.getMessage(), e);
        } catch (IOException e) {
            throw new FileUploadException("Failed to read file: " + file.getOriginalFilename(), e);
        }

        if (blobDTO == null) {
            throw new IllegalStateException("File upload failed but no exception was thrown");
        }

        UserFile userFile = new UserFile(user, blobDTO.getBlobName(), blobDTO.getBlobUrl());
        userFilesRepository.save(userFile);

        return blobDTO;
    }


    public List<BlobDTO> getFiles(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));

        List<UserFile> userFiles = userFilesRepository.findByUserId(user.getId());

        List<BlobDTO> blobDTOList = new ArrayList<>();

        for (UserFile userFile : userFiles) {
            String blobName = userFile.getBlobName();
            String blobUrl = userFile.getBlobUrl();

            blobDTOList.add(new BlobDTO(blobName, blobUrl));
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
