package com.bank_notification.service;

import com.bank_notification.model.Admin;
import com.bank_notification.repository.AdminRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;

    public AdminDetailsService(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin not found: " + username));

        // Use the role from DB, default to ROLE_ADMIN if null
        String userRole = (admin.getRole() != null) ? admin.getRole() : "ROLE_ADMIN";

        return User.builder()
                .username(admin.getUsername())
                .password(admin.getPassword())
                .authorities(List.of(new SimpleGrantedAuthority(userRole)))
                .build();
    }
}