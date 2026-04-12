package com.bank_notification.controller;

import com.bank_notification.model.FixedDeposit;

import com.bank_notification.model.Insurance;

import com.bank_notification.model.Loan;

import com.bank_notification.model.User;

import com.bank_notification.repository.FixedDepositRepository;

import com.bank_notification.repository.InsuranceRepository;

import com.bank_notification.repository.LoanRepository;

import com.bank_notification.repository.UserRepository;

import com.bank_notification.service.EmailService;

import com.bank_notification.service.LoanService;

import org.slf4j.Logger;

import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Sort;

import org.springframework.http.ResponseEntity;

import org.springframework.stereotype.Controller;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.*;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

 

import java.time.LocalDateTime;

import java.util.*;

import java.util.stream.Collectors;

 

@Controller

@RequestMapping("/admin")

public class AdminController {

    private static final Logger log = LoggerFactory.getLogger(AdminController.class);

 

    private final LoanService loanService;

    private final UserRepository userRepository;

    private final LoanRepository loanRepository;

    private final FixedDepositRepository fdRepository;

    private final InsuranceRepository insuranceRepository;

    private final EmailService emailService;

 

    public AdminController(LoanService loanService,

                           UserRepository userRepository,

                           LoanRepository loanRepository,

                           FixedDepositRepository fdRepository,

                           InsuranceRepository insuranceRepository,

                           EmailService emailService) {

        this.loanService = loanService;

        this.userRepository = userRepository;

        this.loanRepository = loanRepository;

        this.fdRepository = fdRepository;

        this.insuranceRepository = insuranceRepository;

        this.emailService = emailService;

    }

 

    // dashboard

    @GetMapping({"", "/", "/dashboard", "/overview"})

    public String adminDashboard(Model model) {

        log.info("adminDashboard()");

        try {

            long totalUsers = userRepository.count();

            long totalLoanRequests = loanRepository.count();

            long totalFD = fdRepository.count();

            long totalIns = insuranceRepository.count();

 

            model.addAttribute("totalUsers", totalUsers);

            model.addAttribute("totalLoanRequests", totalLoanRequests);

            model.addAttribute("totalFD", totalFD);

            model.addAttribute("totalIns", totalIns);

 

            List<RecentItem> items = new ArrayList<>();

 

            // recent loans

            try {

                List<Loan> recentLoans = loanRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

                        .stream().limit(10).collect(Collectors.toList());

                for (Loan l : recentLoans) {

                    String userEmail = l.getUser() != null ? l.getUser().getEmail() : "unknown";

                    String info = "Loan — " + (l.getPrincipal() != null ? l.getPrincipal() : "-");

                    items.add(new RecentItem("Loan", userEmail, info, l.getCreatedAt() == null ? LocalDateTime.MIN : l.getCreatedAt()));

                }

            } catch (Exception ex) {

                log.warn("could not fetch recent loans: {}", ex.getMessage());

            }

 

            // fds

            try {

                List<FixedDeposit> recentFds = fdRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

                        .stream().limit(10).collect(Collectors.toList());

                for (FixedDeposit fd : recentFds) {

                    String userEmail = fd.getUser() != null ? fd.getUser().getEmail() : "unknown";

                    String info = "FD — " + (fd.getAmount() != null ? fd.getAmount() : "-");

                    items.add(new RecentItem("FD", userEmail, info, fd.getCreatedAt() == null ? LocalDateTime.MIN : fd.getCreatedAt()));

                }

            } catch (Exception ex) {

                log.warn("could not fetch recent fds: {}", ex.getMessage());

            }

 

            // ins

            try {

                List<Insurance> recentIns = insuranceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))

                        .stream().limit(10).collect(Collectors.toList());

                for (Insurance ins : recentIns) {

                    String userEmail = ins.getUser() != null ? ins.getUser().getEmail() : "unknown";

                    String info = ins.getPlanName() != null ? ins.getPlanName() : "Insurance";

                    items.add(new RecentItem("Insurance", userEmail, info, ins.getCreatedAt() == null ? LocalDateTime.MIN : ins.getCreatedAt()));

                }

            } catch (Exception ex) {

                log.warn("could not fetch recent insurances: {}", ex.getMessage());

            }

 

            List<RecentItem> recent = items.stream()

                    .sorted(Comparator.comparing(RecentItem::getCreatedAt).reversed())

                    .limit(5)

                    .collect(Collectors.toList());

 

            model.addAttribute("recentActivity", recent);

 

        } catch (Exception ex) {

            log.error("error preparing admin dashboard", ex);

            model.addAttribute("totalUsers", 0);

            model.addAttribute("totalLoanRequests", 0);

            model.addAttribute("totalFD", 0);

            model.addAttribute("totalIns", 0);

            model.addAttribute("recentActivity", Collections.emptyList());

        }

        return "admin/dashboard";

    }

 

    // debug quick counts

    @GetMapping("/debug-counts")

    @ResponseBody

    public ResponseEntity<String> debugCounts() {

        try {

            long totalUsers = userRepository.count();

            long totalLoanRequests = loanRepository.count();

            long totalFD = fdRepository.count();

            long totalIns = insuranceRepository.count();

            String body = String.format("users=%d, loans=%d, fds=%d, ins=%d", totalUsers, totalLoanRequests, totalFD, totalIns);

            log.info("debugCounts -> {}", body);

            return ResponseEntity.ok(body);

        } catch (Exception ex) {

            log.error("debugCounts failed", ex);

            return ResponseEntity.status(500).body("error: " + ex.getMessage());

        }

    }

 

    // Requests listing, filter by ?type=loan|fd|ins (default = loan)

    @GetMapping("/requests")

    public String requestsSummary(@RequestParam(name = "type", required = false) String type, Model model) {

        try {

            if ("fd".equalsIgnoreCase(type)) {

                List<FixedDeposit> fds = fdRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

                model.addAttribute("fds", fds);

                model.addAttribute("type", "fd");

            } else if ("ins".equalsIgnoreCase(type) || "insurance".equalsIgnoreCase(type)) {

                List<Insurance> ins = insuranceRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

                model.addAttribute("insurances", ins);

                model.addAttribute("type", "ins");

            } else {

                List<Loan> loans;

                try {

                    loans = loanService.getAllLoans();

                } catch (Exception ex) {

                    loans = loanRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

                }

                model.addAttribute("loans", loans);

                model.addAttribute("type", "loan");

            }

        } catch (Exception ex) {

            log.error("error preparing requests summary", ex);

            model.addAttribute("loans", Collections.emptyList());

            model.addAttribute("fds", Collections.emptyList());

            model.addAttribute("insurances", Collections.emptyList());

        }

        return "admin/requests";

    }

 

    // Users list

    @GetMapping("/users")

    public String viewUsers(Model model) {

        try {

            List<User> users = userRepository.findAll();

            model.addAttribute("users", users);

        } catch (Exception ex) {

            log.error("error loading users", ex);

            model.addAttribute("users", Collections.emptyList());

        }

        return "admin/users";

    }

 

    // Approve/reject loans (server-side guard: only PENDING allowed)

    @PostMapping("/requests/{id}/approve")

    @Transactional

    public String approveLoan(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            Loan loan = loanService.findById(id);

            if (loan == null) {

                redirectAttributes.addFlashAttribute("error", "Loan not found");

                return "redirect:/admin/requests";

            }

 

            String cur = loan.getStatus() == null ? "PENDING" : loan.getStatus();

            if (!"PENDING".equalsIgnoreCase(cur)) {

                redirectAttributes.addFlashAttribute("error", "Loan already processed (status=" + cur + ")");

                return "redirect:/admin/requests";

            }

 

            loan.setStatus("APPROVED");

            loanService.updateLoan(loan);

 

            try {

                if (loan.getUser() != null && loan.getUser().getEmail() != null) {

                    String body = "Dear " + (loan.getUser().getFullName() == null ? "" : loan.getUser().getFullName()) + ",\n\n" +

                            "We are pleased to inform you that your loan application has been approved by our underwriting team. " +

                            "You will shortly receive details about the disbursement schedule and next steps in a follow-up message. " +

                            "Please ensure your account details are up-to-date so the funds can be transferred promptly. If you have any questions, reply to this email or contact our support team during business hours.\n\n" +

                            "Thank you for choosing SmartBank.\n\nSincerely,\nSmartBank Lending Team";

                    emailService.sendEmail(loan.getUser().getEmail(),

                            "Loan application approved",

                            body);

                }

            } catch (Exception e) {

                log.warn("email send failed for loan approve {}: {}", id, e.getMessage());

            }

 

            redirectAttributes.addFlashAttribute("info", "Loan approved");

        } catch (Exception ex) {

            log.error("approveLoan failed", ex);

            redirectAttributes.addFlashAttribute("error", "Failed to approve loan");

        }

        return "redirect:/admin/requests";

    }

 

    @PostMapping("/requests/{id}/reject")

    @Transactional

    public String rejectLoan(@PathVariable Long id,

                             RedirectAttributes redirectAttributes) {

        try {

            Loan loan = loanService.findById(id);

            if (loan == null) {

                redirectAttributes.addFlashAttribute("error", "Loan not found");

                return "redirect:/admin/requests";

            }

 

            String cur = loan.getStatus() == null ? "PENDING" : loan.getStatus();

            if (!"PENDING".equalsIgnoreCase(cur)) {

                redirectAttributes.addFlashAttribute("error", "Loan already processed (status=" + cur + ")");

                return "redirect:/admin/requests";

            }

 

            loan.setStatus("REJECTED");

            loanService.updateLoan(loan);

 

            try {

                if (loan.getUser() != null && loan.getUser().getEmail() != null) {

                   

                    String body = "Dear " + (loan.getUser().getFullName() == null ? "" : loan.getUser().getFullName()) + ",\n\n" +

                            "Thank you for your loan application. After careful review, we regret to inform you that your application is not approved at this time. " +

                            "This decision was based on our assessment of current underwriting criteria. We encourage you to review your credit and income information and reapply after addressing any issues. " +

                            "If you'd like further clarification, please contact our loan support team and reference your application details — we will be happy to advise on next steps.\n\n" +

                            "Sincerely,\nSmartBank Lending Team";

                    emailService.sendEmail(loan.getUser().getEmail(),

                            "Loan application decision",

                            body);

                }

            } catch (Exception e) {

                log.warn("email send failed for loan reject {}: {}", id, e.getMessage());

            }

 

            redirectAttributes.addFlashAttribute("info", "Loan rejected");

        } catch (Exception ex) {

            log.error("rejectLoan failed", ex);

            redirectAttributes.addFlashAttribute("error", "Failed to reject loan");

        }

        return "redirect:/admin/requests";

    }

 

    // Approve / reject FDs (server-side guard)

    @PostMapping("/fds/{id}/approve")

    @Transactional

    public String approveFd(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            Optional<FixedDeposit> maybe = fdRepository.findById(id);

            if (maybe.isEmpty()) {

                redirectAttributes.addFlashAttribute("error", "FD not found");

                return "redirect:/admin/requests?type=fd";

            }

            FixedDeposit fd = maybe.get();

 

            String cur = fd.getStatus() == null ? "PENDING" : fd.getStatus();

            if (!"PENDING".equalsIgnoreCase(cur)) {

                redirectAttributes.addFlashAttribute("error", "FD already processed (status=" + cur + ")");

                return "redirect:/admin/requests?type=fd";

            }

 

            fd.setStatus("APPROVED");

            fdRepository.save(fd);

 

            try {

                if (fd.getUser() != null && fd.getUser().getEmail() != null) {

                    String body = "Dear " + (fd.getUser().getFullName() == null ? "" : fd.getUser().getFullName()) + ",\n\n" +

                            "We are pleased to confirm your fixed deposit application has been approved. Your deposit is now scheduled and will begin accruing interest as per the terms you selected. You will receive the official confirmation and schedule in a follow-up email within the next business day.\n\n" +

                            "If you need to view or change any details, please visit your dashboard or get in touch with our support team.\n\n" +

                            "Warm regards,\nSmartBank Deposits Team";

                    emailService.sendEmail(fd.getUser().getEmail(),

                            "Fixed deposit approved",

                            body);

                }

            } catch (Exception e) {

                log.warn("email send failed for fd approve {}: {}", id, e.getMessage());

            }

 

            redirectAttributes.addFlashAttribute("info", "FD approved");

        } catch (Exception ex) {

            log.error("approveFd failed", ex);

            redirectAttributes.addFlashAttribute("error", "Failed to approve FD");

        }

        return "redirect:/admin/requests?type=fd";

    }

 

    @PostMapping("/fds/{id}/reject")

    @Transactional

    public String rejectFd(@PathVariable Long id,

                           RedirectAttributes redirectAttributes) {

        try {

            Optional<FixedDeposit> maybe = fdRepository.findById(id);

            if (maybe.isEmpty()) {

                redirectAttributes.addFlashAttribute("error", "FD not found");

                return "redirect:/admin/requests?type=fd";

            }

            FixedDeposit fd = maybe.get();

 

            String cur = fd.getStatus() == null ? "PENDING" : fd.getStatus();

            if (!"PENDING".equalsIgnoreCase(cur)) {

                redirectAttributes.addFlashAttribute("error", "FD already processed (status=" + cur + ")");

                return "redirect:/admin/requests?type=fd";

            }

 

            fd.setStatus("REJECTED");

            fdRepository.save(fd);

 

            try {

                if (fd.getUser() != null && fd.getUser().getEmail() != null) {

                    String body = "Dear " + (fd.getUser().getFullName() == null ? "" : fd.getUser().getFullName()) + ",\n\n" +

                            "Thank you for your fixed deposit application. After review, we are unable to approve this application. If you would like more information about the reason for this decision or how to reapply, please contact our deposits support team and we will be happy to assist.\n\n" +

                            "Regards,\nSmartBank Deposits Team";

                    emailService.sendEmail(fd.getUser().getEmail(),

                            "Fixed deposit application update",

                            body);

                }

            } catch (Exception e) {

                log.warn("email send failed for fd reject {}: {}", id, e.getMessage());

            }

 

            redirectAttributes.addFlashAttribute("info", "FD rejected");

        } catch (Exception ex) {

            log.error("rejectFd failed", ex);

            redirectAttributes.addFlashAttribute("error", "Failed to reject FD");

        }

        return "redirect:/admin/requests?type=fd";

    }

 

    // Approve / reject Insurances (server-side guard)

    @PostMapping("/ins/{id}/approve")

    @Transactional

    public String approveIns(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        try {

            Optional<Insurance> maybe = insuranceRepository.findById(id);

            if (maybe.isEmpty()) {

                redirectAttributes.addFlashAttribute("error", "Insurance not found");

                return "redirect:/admin/requests?type=ins";

            }

            Insurance ins = maybe.get();

 

            String cur = ins.getStatus() == null ? "PENDING" : ins.getStatus();

            if (!"PENDING".equalsIgnoreCase(cur)) {

                redirectAttributes.addFlashAttribute("error", "Insurance already processed (status=" + cur + ")");

                return "redirect:/admin/requests?type=ins";

            }

 

            ins.setStatus("APPROVED");

            insuranceRepository.save(ins);

 

            try {

                if (ins.getUser() != null && ins.getUser().getEmail() != null) {

                    String body = "Dear " + (ins.getUser().getFullName() == null ? "" : ins.getUser().getFullName()) + ",\n\n" +

                            "We are pleased to inform you that your insurance application has been approved. Your policy documentation and further instructions will be sent to you shortly. Please ensure your contact details are correct to avoid delays.\n\n" +

                            "Kind regards,\nSmartBank Insurance Team";

                    emailService.sendEmail(ins.getUser().getEmail(),

                            "Insurance application approved",

                            body);

                }

            } catch (Exception e) {

                log.warn("email send failed for ins approve {}: {}", id, e.getMessage());

            }

 

            redirectAttributes.addFlashAttribute("info", "Insurance approved");

        } catch (Exception ex) {

            log.error("approveIns failed", ex);

            redirectAttributes.addFlashAttribute("error", "Failed to approve insurance");

        }

        return "redirect:/admin/requests?type=ins";

    }

 

    @PostMapping("/ins/{id}/reject")

    @Transactional

    public String rejectIns(@PathVariable Long id,

                            RedirectAttributes redirectAttributes) {

        try {

            Optional<Insurance> maybe = insuranceRepository.findById(id);

            if (maybe.isEmpty()) {

                redirectAttributes.addFlashAttribute("error", "Insurance not found");

                return "redirect:/admin/requests?type=ins";

            }

            Insurance ins = maybe.get();

 

            String cur = ins.getStatus() == null ? "PENDING" : ins.getStatus();

            if (!"PENDING".equalsIgnoreCase(cur)) {

                redirectAttributes.addFlashAttribute("error", "Insurance already processed (status=" + cur + ")");

                return "redirect:/admin/requests?type=ins";

            }

 

            ins.setStatus("REJECTED");

            insuranceRepository.save(ins);

 

            try {

                if (ins.getUser() != null && ins.getUser().getEmail() != null) {

                    String body = "Dear " + (ins.getUser().getFullName() == null ? "" : ins.getUser().getFullName()) + ",\n\n" +

                            "Thank you for applying for insurance with us. After careful review, we are unable to approve your application at this time. If you'd like details on the decision or how to reapply, please contact our insurance support team.\n\n" +

                            "Regards,\nSmartBank Insurance Team";

                    emailService.sendEmail(ins.getUser().getEmail(),

                            "Insurance application update",

                            body);

                }

            } catch (Exception e) {

                log.warn("email send failed for ins reject {}: {}", id, e.getMessage());

            }

 

            redirectAttributes.addFlashAttribute("info", "Insurance rejected");

        } catch (Exception ex) {

            log.error("rejectIns failed", ex);

            redirectAttributes.addFlashAttribute("error", "Failed to reject insurance");

        }

        return "redirect:/admin/requests?type=ins";

    }

 

    // small POJO used in dashboard recentActivity

    public static class RecentItem {

        private final String type;

        private final String userEmail;

        private final String info;

        private final LocalDateTime createdAt;

 

        public RecentItem(String type, String userEmail, String info, LocalDateTime createdAt) {

            this.type = type;

            this.userEmail = userEmail;

            this.info = info;

            this.createdAt = createdAt;

        }

 

        public String getType() { return type; }

        public String getUserEmail() { return userEmail; }

        public String getInfo() { return info; }

        public LocalDateTime getCreatedAt() { return createdAt; }

    }

}