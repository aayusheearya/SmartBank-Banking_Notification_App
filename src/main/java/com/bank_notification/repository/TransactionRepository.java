package com.bank_notification.repository;

import com.bank_notification.model.Transaction;

import com.bank_notification.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    List<Transaction> findByUserOrderByCreatedAtDesc(User user);

    List<Transaction> findByUserIdOrderByCreatedAtDesc(Long userId);


    // Added: delete transactions for a user

    void deleteByUser(User user);

    void deleteByUserId(Long userId);


    // Added: fetch latest 5 transactions across all users (for admin recent activity)

    List<Transaction> findTop5ByOrderByCreatedAtDesc();

}

