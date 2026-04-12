package com.bank_notification.repository;

import com.bank_notification.model.TransferOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TransferOtpRepository extends JpaRepository<TransferOtp, Long> {
    // This finds the latest OTP sent to the user
    Optional<TransferOtp> findTopBySenderEmailOrderByExpiryTimeDesc(String senderEmail);
}