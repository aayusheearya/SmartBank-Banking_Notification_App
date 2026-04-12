package com.bank_notification.controller;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;

 

import jakarta.servlet.http.HttpServletRequest;


@Controller

public class HomeController {


    @GetMapping("/")

    public String index(Model model) {

        model.addAttribute("title", "SmartBank — Notifications");

        return "index";

    }


    @GetMapping("/features")

    public String features(Model model) {

        model.addAttribute("title", "Features — SmartBank");

        return "features";

    }


    @GetMapping("/about")

    public String about(Model model) {

        model.addAttribute("title", "About — SmartBank");

        return "about";

    }


    @GetMapping("/contact")

    public String contact(Model model) {

        model.addAttribute("title", "Contact — SmartBank");

        return "contact";

    }


    @GetMapping("/login")

    public String login(Model model, HttpServletRequest request) {

        model.addAttribute("request", request);

        model.addAttribute("title", "Login — SmartBank");

        return "login";

    }


    @GetMapping("/register")

    public String register(Model model) {

        model.addAttribute("title", "Create account — SmartBank");

        return "register";

    }

}

