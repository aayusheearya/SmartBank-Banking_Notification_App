package com.bank_notification.service.impl;

import com.bank_notification.service.EmailService;

import org.springframework.mail.javamail.JavaMailSender;

import org.springframework.context.annotation.Primary;

import org.springframework.mail.SimpleMailMessage;

import org.springframework.stereotype.Service;

 

@Service

@Primary  //  tells Spring: use this implementation by default

public class EmailServiceImpl implements EmailService {


    private final JavaMailSender mailSender;


    public EmailServiceImpl(JavaMailSender mailSender) {

        this.mailSender = mailSender;

    }


    @Override

    public void sendEmail(String to, String subject, String body) {

        SimpleMailMessage msg = new SimpleMailMessage();

        msg.setTo(to);

        msg.setSubject(subject);

        msg.setText(body);

        mailSender.send(msg);

    }

}


