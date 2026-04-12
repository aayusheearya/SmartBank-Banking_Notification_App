package com.bank_notification.controller;

import com.bank_notification.dto.FdRequest;
import com.bank_notification.dto.InsuranceRequest;
import com.bank_notification.model.FixedDeposit;
import com.bank_notification.model.Insurance;
import com.bank_notification.model.User;
import com.bank_notification.repository.UserRepository;
import com.bank_notification.service.FixedDepositService;
import com.bank_notification.service.InsuranceService;
import com.bank_notification.service.LoanService;
import com.bank_notification.service.TransactionService;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes; // <-- Added Import

import java.math.BigDecimal;
import java.util.stream.Collectors;

@Controller
public class DashboardController {

    private final UserRepository userRepository;
    private final TransactionService transactionService;
    private final LoanService loanService;
    private final FixedDepositService fixedDepositService;
    private final InsuranceService insuranceService;

    public DashboardController(UserRepository userRepository, TransactionService transactionService, LoanService loanService, FixedDepositService fixedDepositService, InsuranceService insuranceService) {
        this.userRepository = userRepository;
        this.transactionService = transactionService;
        this.loanService = loanService;
        this.fixedDepositService = fixedDepositService;
        this.insuranceService = insuranceService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model, @AuthenticationPrincipal UserDetails ud) {
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        model.addAttribute("fullName", user.getFullName());
        model.addAttribute("balance", user.getBalance());
        model.addAttribute("recentTxLimited", transactionService.getTransactionsForUser(user)
                .stream().limit(8).collect(Collectors.toList()));
        return "dashboard";
    }

    @GetMapping("/fixed-deposit/apply")
    public String showFdPage(Model model) {
        model.addAttribute("fdForm", new FdRequest());
        return "fd-apply";
    }

    @GetMapping("/insurance/apply")
    public String showInsurancePage(Model model) {
        model.addAttribute("insuranceForm", new InsuranceRequest());
        return "insurance-apply";
    }

    @PostMapping("/fixed-deposit/apply")
    public String handleFdSubmission(@ModelAttribute("fdForm") FdRequest fdRequest,
                                     @AuthenticationPrincipal UserDetails ud,
                                     RedirectAttributes redirectAttributes) { // <-- Added RedirectAttributes
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();

        try {
            FixedDeposit fdEntity = new FixedDeposit();
            fdEntity.setAmount(BigDecimal.valueOf(fdRequest.getAmount()));
            fdEntity.setTermMonths(fdRequest.getTermMonths());
            fdEntity.setInterestRate(BigDecimal.valueOf(fdRequest.getInterestRate()));

            fixedDepositService.createFixedDepositForUser(user, fdEntity);

            // <-- Replaced old return with Toast Flash Attribute
            redirectAttributes.addFlashAttribute("toastMessage", "Fixed Deposit Opened Successfully! 🎉");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            return "redirect:/fixed-deposit/apply?error=" + e.getMessage();
        }
    }

    @PostMapping("/insurance/apply")
    public String handleInsuranceSubmission(@ModelAttribute("insuranceForm") InsuranceRequest insRequest,
                                            @AuthenticationPrincipal UserDetails ud,
                                            RedirectAttributes redirectAttributes) { // <-- Added RedirectAttributes
        User user = userRepository.findByEmail(ud.getUsername()).orElseThrow();
        try {
            Insurance insuranceEntity = new Insurance();
            insuranceEntity.setProvider(insRequest.getProvider());
            insuranceEntity.setPlanName(insRequest.getPlanName());
            insuranceEntity.setPhone(insRequest.getPhone());
            insuranceEntity.setPolicyNumber(insRequest.getPolicyNumber());

            insuranceService.createInsuranceForUser(user, insuranceEntity);

            // <-- Replaced old return with Toast Flash Attribute
            redirectAttributes.addFlashAttribute("toastMessage", "Insurance Application Received! 🏥");
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            return "redirect:/insurance/apply?error=" + e.getMessage();
        }
    }
}