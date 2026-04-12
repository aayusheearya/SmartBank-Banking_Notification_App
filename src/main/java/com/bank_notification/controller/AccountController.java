package com.bank_notification.controller;

import com.bank_notification.model.User;

import com.bank_notification.repository.FixedDepositRepository;

import com.bank_notification.repository.InsuranceRepository;

import com.bank_notification.repository.LoanRepository;

import com.bank_notification.repository.TransactionRepository;

import com.bank_notification.repository.UserRepository;

import com.bank_notification.service.EmailService;

import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.stereotype.Controller;

import org.springframework.transaction.annotation.Transactional;

import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;


import jakarta.servlet.ServletException;

import jakarta.servlet.http.HttpServletRequest;

import java.security.Principal;

import java.util.Optional;


/**

* Single controller class that handles user account deletion.

*

* Important: Make sure there are no other controllers mapping POST /account/delete

* anywhere in the project (or you will get an ambiguous mapping error).

*/

@Controller

@RequestMapping("/account")

public class AccountController {


    private final UserRepository userRepository;

    private final TransactionRepository transactionRepository;

    private final LoanRepository loanRepository;

    private final FixedDepositRepository fixedDepositRepository;

    private final InsuranceRepository insuranceRepository;

    private final EmailService emailService;


    public AccountController(UserRepository userRepository,

                             TransactionRepository transactionRepository,

                             LoanRepository loanRepository,

                             FixedDepositRepository fixedDepositRepository,

                             InsuranceRepository insuranceRepository,

                             EmailService emailService) {

        this.userRepository = userRepository;

        this.transactionRepository = transactionRepository;

        this.loanRepository = loanRepository;

        this.fixedDepositRepository = fixedDepositRepository;

        this.insuranceRepository = insuranceRepository;

        this.emailService = emailService;

    }


    /**

     * POST /account/delete

     * Deletes the currently authenticated user's data and the user account itself.

     * Sends a final "account deleted" email (best-effort), invalidates session, clears security context,

     * and redirects to home with ?deleted=true on success.

     */

    @PostMapping("/delete")

    @Transactional

    public String deleteCurrentUserAccount(Principal principal,

                                           HttpServletRequest request,

                                           RedirectAttributes redirectAttributes) {

        if (principal == null) {

            // Not authenticated - redirect to login (or home)

            redirectAttributes.addFlashAttribute("error", "You must be logged in to delete your account.");

            return "redirect:/login";

        }


        final String email = principal.getName();

        Optional<User> maybeUser = userRepository.findByEmail(email);

        if (maybeUser.isEmpty()) {

            redirectAttributes.addFlashAttribute("error", "User not found.");

            return "redirect:/dashboard";

        }


        User user = maybeUser.get();


        try {

            // 1) delete dependent entities (best-effort)

            try {

                transactionRepository.findByUserOrderByCreatedAtDesc(user).forEach(transactionRepository::delete);

            } catch (Exception ignored) {}


            try {

                loanRepository.findByUserOrderByCreatedAtDesc(user).forEach(loanRepository::delete);

            } catch (Exception ignored) {}


            try {

                fixedDepositRepository.findByUserOrderByCreatedAtDesc(user).forEach(fixedDepositRepository::delete);

            } catch (Exception ignored) {}


            try {

                insuranceRepository.findByUserOrderByCreatedAtDesc(user).forEach(insuranceRepository::delete);

            } catch (Exception ignored) {}


            // 2) delete user entity

            userRepository.delete(user);


            // 3) send final email (best-effort)

            try {

                final String subject = "SmartBank — Your account has been deleted";

                final StringBuilder body = new StringBuilder();

                body.append("Hello ").append(user.getFullName() == null ? "" : user.getFullName()).append(",\n\n");

                body.append("Your SmartBank account (").append(email).append(") has been permanently deleted from our system.\n\n");

                body.append("If you did not request this or think this is an error, contact support immediately: support@smartbank.example\n\n");

                body.append("Regards,\nSmartBank Team");


                // Adjust if your EmailService uses a different method signature

                emailService.sendEmail(email, subject, body.toString());

            } catch (Exception e) {

                // do not fail deletion if email sending fails - log to stderr (or use logger)

                System.err.println("Failed to send account-deleted email: " + e.getMessage());

            }


            // 4) invalidate session + logout + clear security context

            try {

                if (request.getSession(false) != null) {

                    request.getSession(false).invalidate();

                }

                try {

                    request.logout();

                } catch (ServletException ignored) {}

            } catch (Exception ignored) {}


            SecurityContextHolder.clearContext();


            // Success

            redirectAttributes.addFlashAttribute("info", "Your account has been deleted.");

            return "redirect:/?deleted=true";


        } catch (Exception ex) {

            // Any exception will cause transaction rollback

            redirectAttributes.addFlashAttribute("error", "Failed to delete account: " + ex.getMessage());

            return "redirect:/dashboard";

        }

    }

}