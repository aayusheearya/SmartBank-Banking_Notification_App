package com.bank_notification.controller;

import com.bank_notification.service.EmailService;
import com.bank_notification.service.OtpService;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestParam;

import org.springframework.web.bind.annotation.RestController;

 

/**

* Public OTP endpoints that live under /api/public/** so they are available to anonymous users.

* This keeps your SecurityConfig unchanged.

*/

@RestController

public class OtpPublicController {

 

    private final OtpService otpService;

    private final EmailService emailService;

 

    public OtpPublicController(OtpService otpService, EmailService emailService) {

        this.otpService = otpService;
        this.emailService = emailService;

    }

 

    /**

     * POST /api/public/register/send-otp

     * form param: email

     * returns plain text "OK" or "ERROR: message"

     */

    @PostMapping(path = "/api/public/register/send-otp")

    public String sendOtpPublic(@RequestParam String email) {

        if (email == null || email.isBlank()) {

            return "ERROR: email is required";

        }

        try {

            String normalized = email.trim().toLowerCase();

            // generate 6-digit OTP valid for 5 minutes (300s)

            String otp = otpService.generateOtp(normalized, 6, 300);

            String subject = "Your SmartBank verification code";

            String body = "Your verification code for SmartBank is: " + otp + "\nIt will expire in 5 minutes.";

            // send (NotificationService is robust and swallows/logs exceptions)

            emailService.sendEmail(normalized, subject, body);

 

            // helpful debug log (not required)

            System.out.println("[DEV] OTP sent to " + normalized + " -> " + otp);

 

            return "OK";

        } catch (Exception e) {

            e.printStackTrace();

            return "ERROR: " + e.getMessage();

        }

    }

 

    /**

     * POST /api/public/register/verify-otp

     * form params: email, otp

     * returns plain text "OK" or "ERROR: message"

     */

    @PostMapping(path = "/api/public/register/verify-otp")

    public String verifyOtpPublic(@RequestParam String email, @RequestParam String otp) {

        if (email == null || email.isBlank() || otp == null || otp.isBlank()) {

            return "ERROR: email and otp required";

        }

        boolean ok = otpService.verifyOtp(email.trim().toLowerCase(), otp.trim());

        return ok ? "OK" : "ERROR: invalid-or-expired";

    }

}