package com.bank_notification.controller;

import com.bank_notification.dto.UserRegistrationDto;
import com.bank_notification.model.User;
import com.bank_notification.service.EmailService;
import com.bank_notification.service.OtpService;
import com.bank_notification.service.UserService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class RegistrationController {

    private final UserService userService;
    private final EmailService emailService;
    private final OtpService otpService;

    public RegistrationController(UserService userService,
                                  EmailService emailService,
                                  OtpService otpService) {
        this.userService = userService;
        this.emailService = emailService;
        this.otpService = otpService;
    }

    @PostMapping("/register")
    public String registerUser(@RequestParam(required = false) String fullName,
                               @RequestParam String email,
                               @RequestParam(required = false) String phone,
                               @RequestParam String password,
                               @RequestParam(required = false) String dob,
                               @RequestParam(required = false) String nationalId,
                               Model model) {

        // === NEW: Mandatory Field Check ===
        if (fullName == null || fullName.isBlank() ||
                email == null || email.isBlank() ||
                phone == null || phone.isBlank() ||
                password == null || password.isBlank() ||
                dob == null || dob.isBlank() ||
                nationalId == null || nationalId.isBlank()) {

            model.addAttribute("error", "All fields are mandatory, including Date of Birth and Document ID.");
            model.addAttribute("prefillName", fullName);
            model.addAttribute("prefillEmail", email);
            model.addAttribute("prefillPhone", phone);
            return "register";
        }
        // ==================================

        UserRegistrationDto dto = new UserRegistrationDto();
        dto.setFullName(fullName);
        dto.setEmail(email);
        dto.setPhone(phone);
        dto.setPassword(password);

        try {
            User saved = userService.registerNewUser(dto);

            // send a simple welcome email
            try {
                String recipient = saved.getEmail();
                String name = saved.getFullName() == null ? "" : saved.getFullName();
                String subject = "Welcome to SmartBank";
                String body = "Hi " + (name.isBlank() ? "there" : name) + ",\n\n"
                        + "Welcome to SmartBank — thanks for creating an account with us.\n"
                        + "You can login at: /login\n\n"
                        + "If you need help, reply to this email.\n\n"
                        + "— SmartBank Team";

                emailService.sendEmail(recipient, subject, body);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return "redirect:/login?registered";

        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("prefillName", fullName);
            model.addAttribute("prefillEmail", email);
            model.addAttribute("prefillPhone", phone);
            return "register";

        } catch (DataIntegrityViolationException dive) {
            model.addAttribute("error", "Email already exists");
            model.addAttribute("prefillName", fullName);
            model.addAttribute("prefillEmail", email);
            model.addAttribute("prefillPhone", phone);
            return "register";

        } catch (Exception e) {
            model.addAttribute("error", "Unexpected error: " + e.getMessage());
            return "register";
        }
    }

    // === OTP helpers ===
    @PostMapping(path = "/register/send-otp")
    @ResponseBody
    public String sendOtp(@RequestParam String email) {
        if (email == null || email.isBlank()) return "ERROR: email is required";
        try {
            String normalized = email.trim().toLowerCase();
            String otp = otpService.generateOtp(normalized, 6, 300);
            String subject = "Your SmartBank verification code";
            String body = "Your verification code for SmartBank is: " + otp + "\nIt will expire in 5 minutes.";
            emailService.sendEmail(normalized, subject, body);

            System.out.println("[DEV] Sent OTP for " + normalized + " -> " + otp);
            return "OK";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR: " + e.getMessage();
        }
    }

    @PostMapping(path = "/register/verify-otp")
    @ResponseBody
    public String verifyOtp(@RequestParam String email, @RequestParam String otp) {
        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {
            return "ERROR: email and otp required";
        }
        boolean ok = otpService.verifyOtp(email.trim().toLowerCase(), otp.trim());
        return ok ? "OK" : "ERROR: invalid-or-expired";
    }
}