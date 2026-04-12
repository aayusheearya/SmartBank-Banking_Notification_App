package com.bank_notification.repository;

import com.bank_notification.model.FixedDeposit;
import com.bank_notification.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate; // Added for the scheduler
import java.util.List;

public interface FixedDepositRepository extends JpaRepository<FixedDeposit, Long> {

    List<FixedDeposit> findByUserOrderByCreatedAtDesc(User user);

    // Added: delete fixed deposits for a user
    void deleteByUser(User user);

    void deleteByUserId(Long userId);

    // Added: fetch latest 5 fixed deposits across all users (for admin recent activity)
    List<FixedDeposit> findTop5ByOrderByCreatedAtDesc();

    // --- NEW METHOD FOR SCHEDULER ---
    /**
     * This allows the scheduler to find FDs by the specific maturity date.
     * Spring Data JPA will automatically turn this into a SQL query:
     * SELECT * FROM fixed_deposits WHERE maturity_date = ?
     */
    List<FixedDeposit> findByMaturityDate(LocalDate date);
}