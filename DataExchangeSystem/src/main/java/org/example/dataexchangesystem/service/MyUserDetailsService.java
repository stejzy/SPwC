package org.example.dataexchangesystem.service;

import org.example.dataexchangesystem.exception.UserNotFoundException;
import org.example.dataexchangesystem.model.UserPrincipal;
import org.example.dataexchangesystem.model.Users;
import org.example.dataexchangesystem.repository.UsersRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Users user = usersRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found. Login error."));

        return new UserPrincipal(user);
    }


}
