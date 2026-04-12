package com.bank_notification.service;

import com.bank_notification.model.Loan;
import com.bank_notification.model.User;
import java.util.List;

public interface LoanService {
    // This will now trigger both the DB save and the RecentActivity update
    Loan createLoanForUser(User user, Loan loan);

    List<Loan> getLoansForUser(User user);

    List<Loan> getAllLoans();

    Loan findById(Long id);

    Loan updateLoan(Loan loan);
}