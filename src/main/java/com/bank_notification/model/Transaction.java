package com.bank_notification.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDateTime;


@Entity

@Table(name = "transactions")

public class Transaction {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    // link to user

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id", nullable = false)

    private User user;


    // type: "DEPOSIT", "WITHDRAW", "LOAN", "FEE", etc.

    @Column(nullable = false, length = 40)

    private String type;


    @Column(nullable = false, precision = 15, scale = 2)

    private BigDecimal amount;


    @Column(length = 255)

    private String description;


    @Column(name = "created_at", nullable = false)

    private LocalDateTime createdAt;


    public Transaction() {}


    public Transaction(User user, String type, BigDecimal amount, String description) {

        this.user = user;

        this.type = type;

        this.amount = amount;

        this.description = description;

    }


    @PrePersist

    protected void onCreate() {

        this.createdAt = LocalDateTime.now();

    }


    // getters and setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }


    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }


    public String getType() { return type; }

    public void setType(String type) { this.type = type; }


    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }


    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }


    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}

