package org.example.dataexchangesystem.controller;

import org.example.dataexchangesystem.azure.FileStorageClient;
import org.example.dataexchangesystem.model.Users;
import org.example.dataexchangesystem.model.UsersDTO;
import org.example.dataexchangesystem.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

@RestController
public class UsersController {

    @Autowired
    private UsersService userService;

    @Autowired
    private FileStorageClient fileStorageClient;

    @GetMapping("/hello")
    public String hello() {
        return "Hello World";
    }

    @GetMapping("/")
    public String welcome() {
        return "Welcome to our page";
    }

    @PostMapping("/api/login")
    public String login(@RequestBody Users user) {
        return userService.verify(user);
    }

    @PostMapping("/addUser")
    public Users addUser(@RequestBody UsersDTO userDTO) {
        return userService.addUser(userDTO);
    }

    @PostMapping( "/uploadFile")
    public String uploadFile(@RequestParam String containerName, MultipartFile file) throws IOException {
        try(InputStream inputStream = file.getInputStream()) {
            return this.fileStorageClient.uploadFile(containerName, file.getOriginalFilename(), inputStream, file.getSize());
        }

    }
}
