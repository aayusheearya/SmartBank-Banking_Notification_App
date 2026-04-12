package com.bank_notification.controller;

import com.bank_notification.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
public class ContactController {

    private final EmailService emailService;
    private final String supportEmail;

    public ContactController(EmailService emailService,
                             @Value("${app.support.email:support@smartbank.example}") String supportEmail) {
        this.emailService = emailService;
        this.supportEmail = supportEmail;
    }

    /**
     * POST /contact/send
     * Accepts the contact form and sends:
     * - a confirmation email to the user
     * - a notification email to support
     */
    @PostMapping("/contact/send")
    public String submitContact(@RequestParam String name,
                                @RequestParam String email,
                                @RequestParam String message) {

        // 1. Send confirmation to the user
        try {
            String subjUser = "Thanks for contacting SmartBank";
            String bodyUser = "Hi " + (name == null || name.isBlank() ? "there" : name) + ",\n\n"
                    + "Thanks for reaching out — we received your message and will get back to you shortly.\n\n"
                    + "Your message recorded:\n" + message + "\n\n— SmartBank Support";

            emailService.sendEmail(email, subjUser, bodyUser);
        } catch (Exception e) {
            // Log error but continue so the user isn't blocked by email failure
            e.printStackTrace();
        }

        // 2. Send notification to support team
        try {
            String subjSupport = "New Contact Form: " + name + " <" + email + ">";
            String bodySupport = "You have a new contact form submission:\n\n"
                    + "Name: " + name + "\n"
                    + "Email: " + email + "\n\n"
                    + "Message:\n" + message + "\n";

            emailService.sendEmail(supportEmail, subjSupport, bodySupport);
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3. Redirect back to contact page with the success flag
        // This triggers the ?success=true logic in your HTML to show the popup
        return "redirect:/contact?success=true";
    }
}