package org.example.dataexchangesystem.service;


import com.nimbusds.oauth2.sdk.util.StringUtils;
import jakarta.transaction.Transactional;
import org.example.dataexchangesystem.azure.AzureFileStorageClient;
import org.example.dataexchangesystem.config.FileUploadProgressWebSocketHandler;
import org.example.dataexchangesystem.exception.FileReadException;
import org.example.dataexchangesystem.exception.UserNotFoundException;
import org.example.dataexchangesystem.log.LogBookService;
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
import java.sql.SQLOutput;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    @Autowired
    private LogBookService logBookService;

    public BlobDTO uploadFile(MultipartFile file, String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje."));

        String originalFileName = file.getOriginalFilename();
        if (originalFileName == null || originalFileName.isBlank()) {
            throw new IllegalArgumentException("Nazwa pliku nie może być pusta.");
        }

        String baseFileName = originalFileName.startsWith(username + "_") ?
                originalFileName : username + "_" + originalFileName;

        List<UserFile> existingFiles = userFilesRepository.findByUserAndBlobBaseName(user, baseFileName);

        int version = 1;
        if (!existingFiles.isEmpty()) {
            version = existingFiles.stream()
                    .mapToInt(UserFile::getVersion)
                    .max()
                    .orElse(0) + 1;
        }

        String versionedFileName = baseFileName + "_v" + version;

        BlobDTO blobDTO;
        try (InputStream inputStream = file.getInputStream()) {
            blobDTO = this.azureFileStorageClient.uploadFile(
                    "file-container",
                    versionedFileName,
                    inputStream,
                    file.getSize());
        } catch (IOException e) {
            throw new FileReadException("Nie udało się odczytać pliku: " + file.getOriginalFilename(), e);
        }

        if (blobDTO == null) {
            throw new IllegalStateException("File upload failed but no exception was thrown");
        }

        UserFile userFile = new UserFile(user, baseFileName, blobDTO.getBlobUrl(), version, file.getSize());
        userFilesRepository.save(userFile);

        String displayName = fileNameToDisplay(blobDTO.getBlobName(), username);
        blobDTO.setBlobName(displayName);
        blobDTO.setVersion(version);
        blobDTO.setFileSize(file.getSize());
        blobDTO.setLastModification(userFile.getLastModification());

        return blobDTO;
    }


    private String fileNameToDisplay(String fileName, String username) {
        // Usunięcie wersji (jeśli występuje)
        String fileNameWithoutVersion = fileName.contains("_v") ? fileName.split("_v")[0] : fileName;

        // Usunięcie nazwy użytkownika z początku (jeśli występuje)
        if (fileNameWithoutVersion.startsWith(username + "_")) {
            return fileNameWithoutVersion.substring(username.length() + 1); // Usunięcie "username_" z początku
        }

        return fileNameWithoutVersion;
    }


    private final FileUploadProgressWebSocketHandler webSocketHandler;

    @Autowired
    public UsersService(FileUploadProgressWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }



    public List<BlobDTO> uploadArchive(MultipartFile archive, String username) {
        Users user = usersRepository.findByUsername(username).orElseThrow(
                () -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje.")
        );

        List<BlobDTO> blobDTOList = new ArrayList<>();
        long totalSize = zipSize(archive);
        long uploaded = 0;

        // Przesyłanie plików i wysyłanie progresu
        try (ZipInputStream zipInputStream = new ZipInputStream(archive.getInputStream())) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && !zipEntry.getName().contains("..")) {
                    String fileName = zipEntry.getName();

                    String baseFileName = fileName.startsWith(username + "_") ? fileName : username + "_" + fileName;

                    List<UserFile> existingFiles = userFilesRepository.findByUserAndBlobBaseName(user, baseFileName);

                    int version = 1;
                    if (!existingFiles.isEmpty()) {
                        version = existingFiles.stream()
                                .mapToInt(UserFile::getVersion)
                                .max()
                                .orElse(0) + 1;
                    }

                    String versionedFileName = baseFileName + "_v" + version;

                    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    int i = 0;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        byteArrayOutputStream.write(buffer, 0, bytesRead);

                        if (webSocketHandler != null) {
                            uploaded += bytesRead;
                            if (i == 20 ) {
                                webSocketHandler.sendProgress(uploaded, totalSize);
                                i = 0;
                            }
                            i++;
                        }
                    }

                    byte[] fileBytes = byteArrayOutputStream.toByteArray();
                    long fileSize = fileBytes.length;

                    try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                        BlobDTO blobDTO = azureFileStorageClient.uploadFile("file-container", versionedFileName, inputStream, fileSize);

                        UserFile userFile = new UserFile(user, baseFileName, blobDTO.getBlobUrl(), version, fileSize);
                        userFilesRepository.save(userFile);

                        String displayName = fileNameToDisplay(blobDTO.getBlobName(), username);

                        blobDTO.setBlobName(displayName);
                        blobDTO.setVersion(version);
                        blobDTO.setFileSize(fileSize);
                        blobDTO.setLastModification(userFile.getLastModification());

                        blobDTOList.add(blobDTO);
                        webSocketHandler.sendProgress(uploaded, totalSize);
                    }
                }
            }
        } catch (IOException e) {
            throw new FileReadException("Failed to read file from archive: " + archive.getOriginalFilename(), e);
        }

        return blobDTOList;
    }


    public long zipSize(MultipartFile archive) {
        long totalSize = 0;

        try (ZipInputStream zipInputStream = new ZipInputStream(archive.getInputStream())) {
            ZipEntry zipEntry;
            byte[] buffer = new byte[4096];
            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory() && !zipEntry.getName().contains("..")) {
                    int bytesRead;
                    long entrySize = 0;
                    while ((bytesRead = zipInputStream.read(buffer)) != -1) {
                        entrySize += bytesRead;
                        System.out.println("bytesRead");
                        System.out.println(bytesRead);
                    }
                    totalSize += entrySize;
                }
            }
            System.out.println("Total size: " + totalSize);
        } catch (IOException e) {
            throw new FileReadException("Failed to read file from archive: " + archive.getOriginalFilename(), e);
        }
        return totalSize;
    }



    public void deleteFile(String fileName, String username) throws FileNotFoundException {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Użytkownik o nazwie " + username + " nie istnieje."));

        String baseFileName = fileName.startsWith(username + "_") ? fileName : username + "_" + fileName;

        List<UserFile> userFiles = userFilesRepository.findByUserAndBlobBaseName(user, baseFileName);

        if (userFiles.isEmpty()) {
            throw new FileNotFoundException("No files found with name " + fileName + " for user " + username);
        }

        for (UserFile userFile : userFiles) {
            String uniqueFileName = userFile.getBlobName();
            azureFileStorageClient.deleteFile("file-container", uniqueFileName);

            userFilesRepository.delete(userFile);
        }
    }

    public List<BlobDTO> getAllUserFileVersions(String username, String fileName) throws FileNotFoundException {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));

        List<UserFile> userFiles = userFilesRepository.findLatestFilesByBaseName(fileName);

        if (userFiles.isEmpty()) {
            throw new FileNotFoundException("File with name " + fileName + " not found for user " + username);
        }

        List<BlobDTO> blobDTOList = new ArrayList<>();

        // Loop through all found user files (versions) and create BlobDTOs
        for (UserFile userFile : userFiles) {
            BlobDTO blobDTO = createBlob(userFile, username);
            blobDTOList.add(blobDTO);
        }

        // Return the list of BlobDTOs for the file versions
        return blobDTOList;
    }



    public List<BlobDTO> getNewestUserFiles(String username) {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User with username " + username + " does not exist"));

        List<UserFile> userFiles = userFilesRepository.findByUserId(user.getId());

        Map<String, UserFile> latestFiles = new HashMap<>();

        for (UserFile userFile : userFiles) {
            String baseFileName = userFile.getBlobName().split("_v")[0];
            UserFile existingFile = latestFiles.get(baseFileName);

            if (existingFile == null || userFile.getLastModification().isAfter(existingFile.getLastModification())) {
                latestFiles.put(baseFileName, userFile);
            }
        }

        List<BlobDTO> blobDTOList = new ArrayList<>();

        for (UserFile userFile : latestFiles.values()) {
            BlobDTO blobDTO = createBlob(userFile, username);

            blobDTOList.add(blobDTO);
        }

        return blobDTOList;
    }

    //Auxiliary methods

    private BlobDTO createBlob(UserFile userFile, String username) {
        String blobName = userFile.getBlobName();
        String displayName = blobName.substring(username.length() + 1);

        BlobDTO blobDTO = new BlobDTO(displayName, userFile.getBlobUrl());
        blobDTO.setVersion(userFile.getVersion());
        blobDTO.setFileSize(userFile.getFileSize());
        blobDTO.setLastModification(userFile.getLastModification());

        return blobDTO;
    }

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
