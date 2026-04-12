package com.bank_notification.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

import java.time.LocalDate;

import java.time.LocalDateTime;


@Entity

@Table(name = "loans")

public class Loan {

    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    // who applied

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id")

    private User user;


    @Column(nullable = false, length = 40)

    private String type; // PERSONAL, HOME, AUTO, EDUCATION


    @Column(nullable = false, precision = 15, scale = 2)

    private BigDecimal principal;


    @Column(name = "tenure_months", nullable = false)

    private Integer tenureMonths;


    @Column(name = "monthly_income", precision = 15, scale = 2)

    private BigDecimal monthlyIncome;


    @Column(columnDefinition = "TEXT")

    private String description;


    @Column(nullable = false, length = 20)

    private String status = "PENDING"; // PENDING, APPROVED, REJECTED


    private LocalDateTime createdAt = LocalDateTime.now();


    private LocalDate nextDueDate;


    public Loan() {}


    // --- getters & setters ---

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }


    public User getUser() { return user; }

    public void setUser(User user) { this.user = user; }


    public String getType() { return type; }

    public void setType(String type) { this.type = type; }


    public BigDecimal getPrincipal() { return principal; }

    public void setPrincipal(BigDecimal principal) { this.principal = principal; }


    public Integer getTenureMonths() { return tenureMonths; }

    public void setTenureMonths(Integer tenureMonths) { this.tenureMonths = tenureMonths; }


    public BigDecimal getMonthlyIncome() { return monthlyIncome; }

    public void setMonthlyIncome(BigDecimal monthlyIncome) { this.monthlyIncome = monthlyIncome; }


    public String getDescription() { return description; }

    public void setDescription(String description) { this.description = description; }


    public String getStatus() { return status; }

    public void setStatus(String status) { this.status = status; }


    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }


    public LocalDate getNextDueDate() { return nextDueDate; }

    public void setNextDueDate(LocalDate nextDueDate) { this.nextDueDate = nextDueDate; }

}

