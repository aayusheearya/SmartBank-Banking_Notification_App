package com.bank_notification.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;


@Entity

@Table(name = "insurances")

public class Insurance {


    @Id

    @GeneratedValue(strategy = GenerationType.IDENTITY)

    private Long id;


    // simple policy fields

    private String provider;

    private String policyNumber;

    private String planName;

    private String phone;

    private String notes;


    // STATUS: PENDING, APPROVED, REJECTED

    @Column(nullable = false, length = 20)

    private String status = "PENDING";


    // link to user

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "user_id")

    private User user;


    @Column(name = "created_at", nullable = false)

    private LocalDateTime createdAt;


    public Insurance() { }


    @PrePersist

    protected void onCreate() {

        if (this.createdAt == null) {

            this.createdAt = LocalDateTime.now();

        }

        if (this.status == null) {

            this.status = "PENDING";

        }

    }


    // getters & setters

    public Long getId() {

        return id;

    }


    public void setId(Long id) {

        this.id = id;

    }


    public String getProvider() {

        return provider;

    }


    public void setProvider(String provider) {

        this.provider = provider;

    }


    public String getPolicyNumber() {

        return policyNumber;

    }


    public void setPolicyNumber(String policyNumber) {

        this.policyNumber = policyNumber;

    }


    public String getPlanName() {

        return planName;

    }


    public void setPlanName(String planName) {

        this.planName = planName;

    }


    public String getPhone() {

        return phone;

    }


    public void setPhone(String phone) {

        this.phone = phone;

    }


    public String getNotes() {

        return notes;

    }


    public void setNotes(String notes) {

        this.notes = notes;

    }


    public String getStatus() {

        return status;

    }


    public void setStatus(String status) {

        this.status = status;

    }


    public User getUser() {

        return user;

    }


    public void setUser(User user) {

        this.user = user;

    }


    public LocalDateTime getCreatedAt() {

        return createdAt;

    }


    public void setCreatedAt(LocalDateTime createdAt) {

        this.createdAt = createdAt;

    }

}


