package com.bank_notification.config;

import com.bank_notification.model.Admin;
import com.bank_notification.repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminInitializer implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminInitializer(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.findByUsername("admin").isEmpty()) {
            // HERE is where the automatic conversion happens!
            String rawPassword = "admin";
            String encodedPassword = passwordEncoder.encode(rawPassword);

            Admin admin = new Admin("admin", encodedPassword, "ROLE_ADMIN");
            adminRepository.save(admin);

            System.out.println("✅ Admin account created automatically with BCrypt!");
        }
    }
}