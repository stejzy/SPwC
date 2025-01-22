package org.example.dataexchangesystem.controller;

import org.example.dataexchangesystem.azure.AzureFileStorageClient;
import org.example.dataexchangesystem.azure.FileStorageClient;
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

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to our page";
    }

    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody UsersDTO user) {
        return ResponseEntity.ok(userService.login(user));
    }

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody UsersDTO user) {
        userService.registerUser(user);
        return ResponseEntity.ok("User registered successfully");
    }

//    @PostMapping("/addUser")
//    public Users addUser(@RequestBody UsersDTO userDTO) {
//        return userService.addUser(userDTO);
//    }

    @PostMapping( "/uploadFile")
    public BlobDTO uploadFile(@RequestParam MultipartFile file, String username) throws Exception {
        return userService.uploadFile(file, username);
    }

    @PostMapping("/uploadFiles")
    public ResponseEntity<List<BlobDTO>> uploadFiles(@RequestBody List<MultipartFile> files, String username) throws Exception {
        List<BlobDTO> uploadedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            BlobDTO blobDTO = userService.uploadFile(file, username);
            uploadedFiles.add(blobDTO);
        }
        return ResponseEntity.ok(uploadedFiles);
    }

    @PostMapping("/uploadArchive")
    public ResponseEntity<List<BlobDTO>> uploadArchive(@RequestParam MultipartFile archive, String username) throws Exception {
        return ResponseEntity.ok(userService.uploadArchive(archive, username));
    }

    @DeleteMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(@RequestParam String fileName, @RequestParam String username) throws FileNotFoundException {
        userService.deleteFile(fileName, username);
        return ResponseEntity.ok("File " + fileName + " deleted successfully.");
    }

    @GetMapping( "/files")
    public List<BlobDTO> getFiles(@RequestParam String username) {
        return userService.getFiles(username);
    }


}

