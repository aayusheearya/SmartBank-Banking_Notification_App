package com.bank_notification.service.impl;

import com.bank_notification.dto.UserRegistrationDto;
import com.bank_notification.model.User;
import com.bank_notification.repository.UserRepository;
import com.bank_notification.service.EmailService;
import com.bank_notification.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final BigDecimal defaultBalance;
    private final EmailService emailService;

    public UserServiceImpl(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.default-balance:1000.00}") String defaultBalanceStr, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.defaultBalance = new BigDecimal(defaultBalanceStr);
    }

    @Override
    public User registerNewUser(UserRegistrationDto dto) throws IllegalArgumentException {
        if (dto.getEmail() == null || dto.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User u = new User();
        u.setFullName(dto.getFullName());
        u.setEmail(dto.getEmail().toLowerCase().trim());
        u.setPhone(dto.getPhone());

        // This encodes the raw password into a BCrypt hash before saving
        u.setPassword(passwordEncoder.encode(dto.getPassword()));

        u.setBalance(defaultBalance);
        u.setEnabled(true);

        User saved = userRepository.save(u);

        // --- send welcome email
        try {
            String subject = "Welcome to SmartBank 🎉";
            String body = "Hello " + saved.getFullName() + ",\n\n" +
                    "Welcome to SmartBank! Your account has been created successfully.\n\n" +
                    "You can now login and start using the dashboard.\n\n" +
                    "Best regards,\nSmartBank Team";
            emailService.sendEmail(saved.getEmail(), subject, body);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return saved;
    }

    @Override
    public boolean emailExists(String email) {
        return email != null && userRepository.existsByEmail(email);
    }
}