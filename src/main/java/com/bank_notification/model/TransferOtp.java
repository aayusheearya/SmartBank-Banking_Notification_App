package com.bank_notification.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "transfer_otps")
public class TransferOtp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String senderEmail;
    private String receiverEmail;
    private BigDecimal amount;
    private String description;
    private String otpCode;
    private LocalDateTime expiryTime;

    public TransferOtp() {}

    public TransferOtp(String senderEmail, String receiverEmail, BigDecimal amount, String description, String otpCode) {
        this.senderEmail = senderEmail;
        this.receiverEmail = receiverEmail;
        this.amount = amount;
        this.description = description;
        this.otpCode = otpCode;
        this.expiryTime = LocalDateTime.now().plusMinutes(5); // 5 minute expiry
    }

    // Getters
    public Long getId() { return id; }
    public String getSenderEmail() { return senderEmail; }
    public String getReceiverEmail() { return receiverEmail; }
    public BigDecimal getAmount() { return amount; }
    public String getDescription() { return description; }
    public String getOtpCode() { return otpCode; }
    public LocalDateTime getExpiryTime() { return expiryTime; }
}