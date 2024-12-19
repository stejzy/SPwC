package org.example.dataexchangesystem.service;


import org.example.dataexchangesystem.model.UsersDTO;
import org.example.dataexchangesystem.repository.UsersRepository;
import org.example.dataexchangesystem.model.Users;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UsersService {

    @Autowired
    AuthenticationManager authenticationManager;

    @Autowired
    JwtService jwtService;

    @Autowired
    UsersRepository usersRepository;

    public String verify(Users user) {
        Authentication authentication =
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(user.getUsername(), user.getPassword()));

        if (authentication.isAuthenticated()) {
            return jwtService.generateToken(user.getUsername());
        }

        return "Fail";
    }

    public Users addUser(UsersDTO userDTO) {

        Users user = new Users();
        user.setUsername(userDTO.getUsername());
        user.setPassword(new BCryptPasswordEncoder(12).encode(userDTO.getPassword()));

        return usersRepository.save(user);
    }


}
