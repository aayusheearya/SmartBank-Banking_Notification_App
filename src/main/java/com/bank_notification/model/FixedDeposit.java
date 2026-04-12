package com.bank_notification.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.time.LocalDateTime;


@Entity

@Table(name = "fixed_deposits")

public class FixedDeposit {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    private BigDecimal amount;

    private Integer termMonths;

    private BigDecimal interestRate; // annual percent, e.g. 6.5

    private LocalDate startDate;

    private LocalDate maturityDate;

    private String status; // e.g. ACTIVE, MATURED, CANCELLED


    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id")

    private User user;


    private LocalDateTime createdAt = LocalDateTime.now();


    public FixedDeposit() {}


    // getters & setters

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }


    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }


    public Integer getTermMonths() { return termMonths; }

    public void setTermMonths(Integer termMonths) { this.termMonths = termMonths; }


    public BigDecimal getInterestRate() { return interestRate; }

    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }


    public LocalDate getStartDate() { return startDate; }

    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }


    public LocalDate getMaturityDate() { return maturityDate; }

    public void setMaturityDate(LocalDate maturityDate) { this.maturityDate = maturityDate; }


    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }


    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }


    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}