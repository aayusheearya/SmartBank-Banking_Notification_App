package com.bank_notification.service.impl;

import com.bank_notification.model.FixedDeposit;

import com.bank_notification.model.User;

import com.bank_notification.repository.FixedDepositRepository;

import com.bank_notification.service.EmailService;
import com.bank_notification.service.FixedDepositService;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;

import java.time.LocalDate;


@Service

public class FixedDepositServiceImpl implements FixedDepositService {


    private final FixedDepositRepository fixedDepositRepository;
    private final EmailService emailService;


    public FixedDepositServiceImpl(FixedDepositRepository fixedDepositRepository, EmailService emailService) {

        this.fixedDepositRepository = fixedDepositRepository;

        this.emailService = emailService;
    }


    @Override

    @Transactional

    public FixedDeposit createFixedDepositForUser(User user, FixedDeposit fd) throws IllegalArgumentException {

        if (user == null) {

            throw new IllegalArgumentException("User must be provided");

        }

        if (fd == null) {

            throw new IllegalArgumentException("FixedDeposit data is required");

        }


        BigDecimal amount = fd.getAmount();

        Integer termMonths = fd.getTermMonths();


        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new IllegalArgumentException("Amount must be greater than zero.");

        }

        if (termMonths == null || termMonths <= 0) {

            throw new IllegalArgumentException("Term (months) must be a positive integer.");

        }


        // Determine start date and maturity date if not provided

        LocalDate startDate = fd.getStartDate() != null ? fd.getStartDate() : LocalDate.now();

        LocalDate maturityDate = startDate.plusMonths(termMonths);


        // Populate defaults

        FixedDeposit entity = new FixedDeposit();

        entity.setUser(user);

        entity.setAmount(amount);

        entity.setTermMonths(termMonths);

        entity.setInterestRate(fd.getInterestRate());

        entity.setStartDate(startDate);

        entity.setMaturityDate(maturityDate);

        entity.setStatus("ACTIVE");

        if (entity.getCreatedAt() == null) {

            entity.setCreatedAt(java.time.LocalDateTime.now());

        }


        // Save and return

        FixedDeposit savedFd = fixedDepositRepository.save(entity);

        // 2. Trigger Email Notification
        try {
            String subject = "Fixed Deposit Opened — SmartBank";
            String body = "Hello " + user.getFullName() + ",\n\n" +
                    "Your Fixed Deposit of ₹" + savedFd.getAmount() + " has been successfully opened.\n" +
                    "Term: " + savedFd.getTermMonths() + " Months\n" +
                    "Maturity Date: " + savedFd.getMaturityDate();
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            System.err.println("FD Email failed: " + ex.getMessage());
        }

        return savedFd;

    }

}


