package com.bank_notification.service.impl;

import com.bank_notification.dto.TransactionRequestDto;
import com.bank_notification.model.Transaction;
import com.bank_notification.model.TransferOtp;
import com.bank_notification.model.User;
import com.bank_notification.repository.TransactionRepository;
import com.bank_notification.repository.TransferOtpRepository;
import com.bank_notification.repository.UserRepository;
import com.bank_notification.service.TransactionService;
import com.bank_notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

@Service
public class TransactionServiceImpl implements TransactionService {

    private final Logger log = LoggerFactory.getLogger(TransactionServiceImpl.class);
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;
    private final TransferOtpRepository otpRepository;
    private final EmailService emailService;

    public TransactionServiceImpl(TransactionRepository transactionRepository,
                                  UserRepository userRepository,
                                  TransferOtpRepository otpRepository,
                                  EmailService emailService) {
        this.transactionRepository = transactionRepository;
        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.emailService = emailService;
    }

    @Override
    @Transactional
    public void initiateTransfer(User sender, TransactionRequestDto dto) {
        if (sender.getBalance().compareTo(dto.getAmount()) < 0) {
            throw new IllegalArgumentException("Insufficient funds for transfer");
        }

        String otp = String.format("%06d", new Random().nextInt(999999));
        TransferOtp otpEntry = new TransferOtp(sender.getEmail(), dto.getReceiverEmail(), dto.getAmount(), dto.getDescription(), otp);
        otpRepository.save(otpEntry);

        sendAsyncEmail(sender.getEmail(), "Transfer Verification Code", "Your 2FA code is: " + otp);
    }

    @Override
    @Transactional
    public Transaction confirmTransfer(User sender, String otpCode) {
        TransferOtp otpEntry = otpRepository.findTopBySenderEmailOrderByExpiryTimeDesc(sender.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No pending transfer found"));

        if (otpEntry.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("OTP has expired");
        }
        if (!otpEntry.getOtpCode().equals(otpCode)) {
            throw new IllegalArgumentException("Invalid OTP code");
        }

        Transaction tx = transferFunds(sender, otpEntry.getReceiverEmail(), otpEntry.getAmount(), otpEntry.getDescription());
        otpRepository.delete(otpEntry);
        return tx;
    }

    @Override
    @Transactional
    public Transaction transferFunds(User sender, String receiverEmail, BigDecimal amount, String description) throws IllegalArgumentException {
        User managedSender = userRepository.findById(sender.getId()).orElseThrow();
        User receiver = userRepository.findByEmail(receiverEmail)
                .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

        managedSender.setBalance(managedSender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        userRepository.save(managedSender);
        userRepository.save(receiver);

        Transaction debit = transactionRepository.save(new Transaction(managedSender, "TRANSFER_OUT", amount.negate(), "To: " + receiverEmail + " | " + description));
        transactionRepository.save(new Transaction(receiver, "TRANSFER_IN", amount, "From: " + managedSender.getEmail() + " | " + description));

        sendAsyncEmail(managedSender.getEmail(), "Transfer Sent", "₹" + amount + " sent to " + receiverEmail);
        sendAsyncEmail(receiver.getEmail(), "Transfer Received", "₹" + amount + " received from " + managedSender.getEmail());

        return debit;
    }

    @Override
    @Transactional
    public Transaction createTransactionForUser(User user, TransactionRequestDto dto) throws IllegalArgumentException {
        User mUser = userRepository.findById(user.getId()).orElseThrow();
        BigDecimal amount = dto.getAmount();

        if ("DEPOSIT".equalsIgnoreCase(dto.getType())) {
            mUser.setBalance(mUser.getBalance().add(amount));
            userRepository.save(mUser);
            Transaction tx = transactionRepository.save(new Transaction(mUser, "DEPOSIT", amount, dto.getDescription()));
            sendAsyncEmail(mUser.getEmail(), "Deposit Received", "Amount: ₹" + amount);
            return tx;
        } else {
            if (mUser.getBalance().compareTo(amount) < 0) throw new IllegalArgumentException("Insufficient balance");
            mUser.setBalance(mUser.getBalance().subtract(amount));
            userRepository.save(mUser);
            Transaction tx = transactionRepository.save(new Transaction(mUser, "WITHDRAWAL", amount.negate(), dto.getDescription()));
            sendAsyncEmail(mUser.getEmail(), "Withdrawal Processed", "Amount: ₹" + amount);
            return tx;
        }
    }

    @Override
    public List<Transaction> getTransactionsForUser(User user) {
        return transactionRepository.findByUserOrderByCreatedAtDesc(user);
    }

    private void sendAsyncEmail(String to, String subject, String body) {
        new Thread(() -> {
            try { emailService.sendEmail(to, subject, body); } catch (Exception e) { log.error("Email error", e); }
        }).start();
    }
}