package com.bank_notification.service;

import com.bank_notification.model.User;

import com.bank_notification.repository.UserRepository;

import org.springframework.security.core.authority.SimpleGrantedAuthority;

import org.springframework.security.core.userdetails.*;

import org.springframework.stereotype.Service;


import java.util.List;


@Service

public class CustomUserDetailsService implements UserDetailsService {


    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {

        this.userRepository = userRepository;

    }


    @Override

    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        //  "username" to be the user's email

        User u = userRepository.findByEmail(username)

                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return org.springframework.security.core.userdetails.User

                .withUsername(u.getEmail())

                .password(u.getPassword())

                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))

                .build();

    }

}

