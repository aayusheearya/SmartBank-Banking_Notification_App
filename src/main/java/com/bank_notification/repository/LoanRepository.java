package com.bank_notification.repository;

import com.bank_notification.model.Loan;

import com.bank_notification.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

import java.util.List;


public interface LoanRepository extends JpaRepository<Loan, Long> {

    List<Loan> findByUserOrderByCreatedAtDesc(User user);

    List<Loan> findByNextDueDateLessThanEqualAndStatus(LocalDate date, String status);


    // Added: delete loans for a user

    void deleteByUser(User user);

    void deleteByUserId(Long userId);


    // Added: fetch latest 5 loans across all users (for admin recent activity)

    List<Loan> findTop5ByOrderByCreatedAtDesc();

}