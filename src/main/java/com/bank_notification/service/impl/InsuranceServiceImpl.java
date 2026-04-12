package com.bank_notification.service.impl;

import com.bank_notification.model.Insurance;

import com.bank_notification.model.User;

import com.bank_notification.repository.InsuranceRepository;

import com.bank_notification.service.EmailService;
import com.bank_notification.service.InsuranceService;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;

import java.util.regex.Pattern;


@Service

public class InsuranceServiceImpl implements InsuranceService {


    private final InsuranceRepository insuranceRepository;
    private final EmailService emailService;


    public InsuranceServiceImpl(InsuranceRepository insuranceRepository, EmailService emailService) {

        this.insuranceRepository = insuranceRepository;

        this.emailService = emailService;
    }


    @Override

    @Transactional

    public Insurance createInsuranceForUser(User user, Insurance insurance) throws IllegalArgumentException {

        if (user == null) throw new IllegalArgumentException("User is required");

        if (insurance == null) throw new IllegalArgumentException("Insurance data is required");


        // Minimal required checks: provider and phone

        String provider = insurance.getProvider();

        String phone = insurance.getPhone();


        if (provider == null || provider.trim().isEmpty()) {

            throw new IllegalArgumentException("Provider is required");

        }

        if (phone == null || phone.trim().isEmpty()) {

            throw new IllegalArgumentException("Phone is required");

        }


        // Optional basic phone pattern sanity check (allow + digits, spaces, hyphen)

        Pattern p = Pattern.compile("^\\+?[0-9\\s\\-]{7,20}$");

        if (!p.matcher(phone.trim()).matches()) {

            throw new IllegalArgumentException("Phone number is invalid");

        }


        insurance.setUser(user);

        if (insurance.getCreatedAt() == null) {

            insurance.setCreatedAt(LocalDateTime.now());

        }


        Insurance savedInsurance = insuranceRepository.save(insurance);

        // 2. Trigger Email Notification
        try {
            String subject = "Insurance Application Received — SmartBank";
            String body = "Hello " + user.getFullName() + ",\n\n" +
                    "We have received your application for " + savedInsurance.getPlanName() + ".\n" +
                    "Provider: " + savedInsurance.getProvider() + "\n" +
                    "Our representative will contact you on " + savedInsurance.getPhone() + " shortly.";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            System.err.println("Insurance Email failed: " + ex.getMessage());
        }

        return savedInsurance;
    }

}

