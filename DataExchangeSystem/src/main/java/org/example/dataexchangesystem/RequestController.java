package org.example.dataexchangesystem;

import org.example.dataexchangesystem.model.Users;
import org.example.dataexchangesystem.service.UsersService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
public class RequestController {

    @Autowired
    private UsersService service;

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
        return service.verify(user);
    }
}

