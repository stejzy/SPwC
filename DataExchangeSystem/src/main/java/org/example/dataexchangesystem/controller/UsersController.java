package org.example.dataexchangesystem.controller;

import org.example.dataexchangesystem.azure.AzureFileStorageClient;
import org.example.dataexchangesystem.azure.FileStorageClient;
import org.example.dataexchangesystem.log.LogBookService;
import org.example.dataexchangesystem.model.BlobDTO;
import org.example.dataexchangesystem.model.Users;
import org.example.dataexchangesystem.model.UsersDTO;
import org.example.dataexchangesystem.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@RestController
public class UsersController {

    @Autowired
    private UsersService userService;

    @Autowired
    private LogBookService logBookService;

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UsersDTO user) {
        logBookService.log("User login attempt: " + user.getUsername());
        String result = userService.login(user);
        logBookService.log("User login success: " + user.getUsername());
        return ResponseEntity.ok(result);
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UsersDTO user) {
        userService.registerUser(user);
        logBookService.log("User registered: " + user.getUsername());
        return ResponseEntity.ok("User registered successfully");
    }

    @PostMapping("/uploadFile")
    public BlobDTO uploadFile(@RequestParam MultipartFile file, String username) throws Exception {
        logBookService.log("File upload initiated by " + username + " for file: " + file.getOriginalFilename());
        BlobDTO blobDTO = userService.uploadFile(file, username);
        logBookService.log("File upload successful: " + file.getOriginalFilename() + " by " + username);
        return blobDTO;
    }

    @PostMapping("/uploadFiles")
    public ResponseEntity<List<BlobDTO>> uploadFiles(@RequestBody List<MultipartFile> files, String username) throws Exception {
        logBookService.log("Multiple file upload initiated by " + username);
        List<BlobDTO> uploadedFiles = new ArrayList<>();
        for (MultipartFile file : files) {
            BlobDTO blobDTO = userService.uploadFile(file, username);
            uploadedFiles.add(blobDTO);
            logBookService.log("File uploaded: " + file.getOriginalFilename() + " by " + username);
        }
        return ResponseEntity.ok(uploadedFiles);
    }

    @PostMapping("/uploadArchive")
    public ResponseEntity<List<BlobDTO>> uploadArchive(@RequestParam MultipartFile archive, String username) throws Exception {
        logBookService.log("Archive upload initiated by " + username + " for archive: " + archive.getOriginalFilename());
        List<BlobDTO> archiveContents = userService.uploadArchive(archive, username);
        logBookService.log("Archive upload successful: " + archive.getOriginalFilename() + " by " + username);
        return ResponseEntity.ok(archiveContents);
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName, @RequestParam String username) throws FileNotFoundException {
        logBookService.log("File deletion requested: " + fileName + " by " + username);
        userService.deleteFile(fileName, username);
        logBookService.log("File deletion successful: " + fileName + " by " + username);
        return ResponseEntity.ok("File " + fileName + " deleted successfully.");
    }

    @GetMapping("/allFileVersions")
    public List<BlobDTO> getAllUserFileVersions(@RequestParam String username, String fileName) throws FileNotFoundException {
        logBookService.log("Fetching all versions of file: " + fileName + " for user: " + username);
        return userService.getAllUserFileVersions(username, fileName);
    }

    @GetMapping("/newestFiles")
    public List<BlobDTO> getNewestUserFiles(@RequestParam String username) {
        logBookService.log("Fetching newest files for user: " + username);
        return userService.getNewestUserFiles(username);
    }
}



