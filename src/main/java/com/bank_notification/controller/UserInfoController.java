package com.bank_notification.controller;

import com.bank_notification.model.User;

import com.bank_notification.repository.UserRepository;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.security.core.userdetails.UserDetails;

import org.springframework.web.bind.annotation.GetMapping;

import org.springframework.web.bind.annotation.RestController;


import java.util.Map;

import java.util.Optional;


@RestController

public class UserInfoController {


    private final UserRepository userRepository;


    public UserInfoController(UserRepository userRepository) {

        this.userRepository = userRepository;

    }


    /**

     * GET /api/userinfo

     * Returns minimal info about the currently authenticated user:

     * { "email": "...", "fullName": "...", "balance": 123.45 }

     */

    @GetMapping("/api/userinfo")

    public ResponseEntity<?> userInfo(@AuthenticationPrincipal UserDetails userDetails) {

        if (userDetails == null) {

            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        }


        Optional<User> maybe = userRepository.findByEmail(userDetails.getUsername());

        if (maybe.isEmpty()) {

            return ResponseEntity.status(404).body(Map.of("error", "User not found"));

        }

        User u = maybe.get();


        // return minimal JSON (avoid serializing lazy fields)

        return ResponseEntity.ok(Map.of(

                "email", u.getEmail(),

                "fullName", u.getFullName(),

                "balance", u.getBalance()

        ));

    }

}