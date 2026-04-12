package com.bank_notification.service;

import com.bank_notification.model.FixedDeposit;
import com.bank_notification.repository.FixedDepositRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class FDSchedulerService {

    @Autowired
    private FixedDepositRepository fdRepository;

    @Autowired
    private EmailService emailService;

    @Scheduled(cron = "0 0 9 * * *")
    public void checkFDMaturity() {
        LocalDate today = LocalDate.now();

        List<FixedDeposit> maturingSoon = fdRepository.findByMaturityDate(today);

        for (FixedDeposit fd : maturingSoon) {
            // We use getEmail() and getFullName() from the User object linked to the FD
            String userEmail = fd.getUser().getEmail();
            String userName = fd.getUser().getFullName();

            String subject = "SmartBank: Fixed Deposit Maturity Alert";

            // Simplified message using only basic fields
            String message = "Dear " + userName + ",\n\n" +
                    "Your Fixed Deposit of amount " + fd.getAmount() +
                    " has matured today (" + today + ").\n" +
                    "The maturity proceeds are being processed and will be reflected in your account shortly.\n\n" +
                    "Thank you for choosing SmartBank!";

            emailService.sendEmail(userEmail, subject, message);
        }
    }
}