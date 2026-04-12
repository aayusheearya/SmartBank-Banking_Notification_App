package com.bank_notification.repository;

import com.bank_notification.model.Insurance;

import com.bank_notification.model.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface InsuranceRepository extends JpaRepository<Insurance, Long> {

    List<Insurance> findByUserOrderByCreatedAtDesc(User user);


    // Added: delete insurance entries for a user

    void deleteByUser(User user);

    void deleteByUserId(Long userId);


    // Added: fetch latest 5 insurances across all users (for admin recent activity)

    List<Insurance> findTop5ByOrderByCreatedAtDesc();
}