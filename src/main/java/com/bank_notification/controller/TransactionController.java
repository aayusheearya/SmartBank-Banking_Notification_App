package com.bank_notification.controller;

import com.bank_notification.dto.TransactionRequestDto;
import com.bank_notification.model.Transaction;
import com.bank_notification.model.User;
import com.bank_notification.repository.UserRepository;
import com.bank_notification.service.TransactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    public TransactionController(TransactionService transactionService, UserRepository userRepository) {
        this.transactionService = transactionService;
        this.userRepository = userRepository;
    }

    @PostMapping("/transfer/initiate")
    public ResponseEntity<?> initiateTransfer(@RequestBody TransactionRequestDto dto, @AuthenticationPrincipal UserDetails ud) {
        try {
            User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
            transactionService.initiateTransfer(user, dto);
            return ResponseEntity.ok(Map.of("message", "OTP Sent"));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/transfer/confirm")
    public ResponseEntity<?> confirmTransfer(@RequestBody Map<String, String> body, @AuthenticationPrincipal UserDetails ud) {
        try {
            User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
            Transaction tx = transactionService.confirmTransfer(user, body.get("otpCode"));
            return ResponseEntity.ok(tx);
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> deposit(@RequestBody TransactionRequestDto dto, @AuthenticationPrincipal UserDetails ud) {
        try {
            dto.setType("DEPOSIT");
            User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
            return ResponseEntity.ok(transactionService.createTransactionForUser(user, dto));
        } catch (Exception e) { return ResponseEntity.badRequest().body(e.getMessage()); }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdraw(@RequestBody TransactionRequestDto dto, @AuthenticationPrincipal UserDetails ud) {
        try {
            dto.setType("WITHDRAW");
            User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
            return ResponseEntity.ok(transactionService.createTransactionForUser(user, dto));
        } catch (Exception e) {
            // Catches "Insufficient balance" and returns it as a message
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}