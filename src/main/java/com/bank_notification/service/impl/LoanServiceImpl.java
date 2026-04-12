package com.bank_notification.service.impl;

import com.bank_notification.model.Loan;
import com.bank_notification.model.User;
import com.bank_notification.repository.LoanRepository;
import com.bank_notification.service.LoanService;
import com.bank_notification.service.EmailService;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanServiceImpl implements LoanService {

    private final LoanRepository loanRepository;
    private final EmailService emailService;

    public LoanServiceImpl(LoanRepository loanRepository, EmailService emailService) {
        this.loanRepository = loanRepository;
        this.emailService = emailService;
    }

    @Override
    public Loan createLoanForUser(User user, Loan loan) {
        loan.setUser(user);
        loan.setCreatedAt(LocalDateTime.now());
        if (loan.getStatus() == null) loan.setStatus("PENDING");

        Loan saved = loanRepository.save(loan);

        try {
            String subject = "Loan application received — SmartBank";
            String body = "Hello " + user.getFullName() + ",\n\n" +
                    "Your loan application for ₹" + saved.getPrincipal() + " has been received.\n" +
                    "Status: PENDING";
            emailService.sendEmail(user.getEmail(), subject, body);
        } catch (Exception ex) {
            System.err.println("Email failed: " + ex.getMessage());
        }

        return saved;
    }

    @Override public List<Loan> getLoansForUser(User user) { return loanRepository.findByUserOrderByCreatedAtDesc(user); }
    @Override public List<Loan> getAllLoans() { return loanRepository.findAll(); }
    @Override public Loan findById(Long id) { return loanRepository.findById(id).orElse(null); }
    @Override public Loan updateLoan(Loan loan) { return loanRepository.save(loan); }
}