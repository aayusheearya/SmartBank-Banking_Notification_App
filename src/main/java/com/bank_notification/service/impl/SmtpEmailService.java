package com.bank_notification.service.impl;

import com.bank_notification.service.EmailService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.mail.MailException;

import org.springframework.mail.SimpleMailMessage;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.stereotype.Service;


/**

* Simple email sender implementation using JavaMailSender.

* Sends plain text emails. This is sufficient for notifications and tests.

*/

@Service

public class SmtpEmailService implements EmailService {


    private final Logger log = LoggerFactory.getLogger(SmtpEmailService.class);

    private final JavaMailSender mailSender;

    private final String fromAddress;


    public SmtpEmailService(JavaMailSender mailSender) {

        this.mailSender = mailSender;

        // default "from" — you can override with property injection if desired later

        this.fromAddress = "no-reply@smartbank.example";

    }


    @Override

    public void sendEmail(String to, String subject, String body) {

        if (to == null || to.isBlank()) {

            log.warn("Attempt to send email with empty recipient (subject={})", subject);

            return;

        }

        try {

            SimpleMailMessage msg = new SimpleMailMessage();

            msg.setTo(to);

            msg.setFrom(fromAddress);

            msg.setSubject(subject);

            msg.setText(body);

            mailSender.send(msg);

            log.info("Sent email to {} (subject={})", to, subject);

        } catch (MailException ex) {

            // Log and rethrow as runtime if you want to fail

            log.warn("Failed to send email to {}: {}", to, ex.getMessage());

        } catch (Exception ex) {

            log.warn("Unexpected error while sending email to {}: {}", to, ex.getMessage());

        }

    }

}