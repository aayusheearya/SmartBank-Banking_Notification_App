package com.bank_notification.service;

import com.bank_notification.dto.TransactionRequestDto;
import com.bank_notification.model.Transaction;
import com.bank_notification.model.User;
import java.util.List;
import java.math.BigDecimal;

public interface TransactionService {
    Transaction createTransactionForUser(User user, TransactionRequestDto dto) throws IllegalArgumentException;
    Transaction transferFunds(User sender, String receiverEmail, BigDecimal amount, String description) throws IllegalArgumentException;
    List<Transaction> getTransactionsForUser(User user);

    // 2FA Methods
    void initiateTransfer(User sender, TransactionRequestDto dto);
    Transaction confirmTransfer(User sender, String otpCode);
}