package com.bank_notification.controller;

import com.bank_notification.model.Loan;
import com.bank_notification.model.User;
import com.bank_notification.repository.UserRepository;
import com.bank_notification.service.LoanService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- Added Import

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/loans")
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    public LoanController(LoanService loanService, UserRepository userRepository) {
        this.loanService = loanService;
        this.userRepository = userRepository;
    }

    @PostMapping("/apply")
    public String applyLoan(@AuthenticationPrincipal UserDetails currentUser,
                            @RequestParam("type") String type,
                            @RequestParam("principal") BigDecimal principal,
                            @RequestParam("monthlyIncome") BigDecimal monthlyIncome,
                            @RequestParam("tenureMonths") Integer tenureMonths,
                            @RequestParam("description") String description,
                            RedirectAttributes redirectAttributes) { // <-- Added RedirectAttributes

        if (currentUser == null) return "redirect:/login";

        Optional<User> uOpt = userRepository.findByEmail(currentUser.getUsername());
        if (uOpt.isPresent()) {
            Loan loan = new Loan();
            loan.setType(type);
            loan.setPrincipal(principal);
            loan.setMonthlyIncome(monthlyIncome);
            loan.setTenureMonths(tenureMonths);
            loan.setDescription(description);
            loan.setStatus("PENDING");

            loanService.createLoanForUser(uOpt.get(), loan);
        }

        // <-- Replaced old return with Toast Flash Attribute
        redirectAttributes.addFlashAttribute("toastMessage", "Loan Request Submitted! 💸");
        return "redirect:/dashboard";
    }
}