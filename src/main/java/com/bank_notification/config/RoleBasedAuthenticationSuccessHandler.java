package com.bank_notification.config;

import com.bank_notification.service.EmailService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class RoleBasedAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(RoleBasedAuthenticationSuccessHandler.class);

    @Autowired
    private EmailService emailService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException, ServletException {

        log.info("Login successful for: {}", authentication.getName());

        // --- FIXED: Send email in background so it doesn't freeze the UI ---
        String email = authentication.getName();
        new Thread(() -> {
            try {
                emailService.sendEmail(
                        email,
                        "SmartBank Security Alert",
                        "A new login was detected for your account at: " + new java.util.Date()
                );
            } catch (Exception e) {
                log.error("Email notification failed: {}", e.getMessage());
            }
        }).start();

        // --- REDIRECT LOGIC ---
        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();
        boolean isAdmin = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equalsIgnoreCase("ROLE_ADMIN") || a.equalsIgnoreCase("ADMIN"));

        if (isAdmin) {
            log.info("Redirecting to Admin Dashboard");
            response.sendRedirect(request.getContextPath() + "/admin/dashboard");
        } else {
            log.info("Redirecting to User Dashboard");
            response.sendRedirect(request.getContextPath() + "/dashboard");
        }
    }
}