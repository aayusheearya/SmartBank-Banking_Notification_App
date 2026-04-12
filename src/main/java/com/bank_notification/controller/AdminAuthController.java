package com.bank_notification.controller;

import org.springframework.stereotype.Controller;

import org.springframework.web.bind.annotation.GetMapping;


@Controller

public class AdminAuthController {


    /**

     * GET /admin/login

     * Serves the admin login page (admin-login.html in /templates/)

     */

    @GetMapping("/admin/login")

    public String adminLogin() {

        // looks for src/main/resources/templates/admin-login.html

        return "admin-login";

    }

}