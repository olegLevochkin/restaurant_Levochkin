package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Role;
import com.gmail.oleg.restaurantapi.model.User;
import com.gmail.oleg.restaurantapi.service.UserService;
import com.gmail.oleg.restaurantapi.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RegistrationController {
    private final UserService userService;
    private final UserValidator userValidator;

    @GetMapping("/registration")
    public String registration() {
        return "register";
    }

    @PostMapping("/registration")
    public String addUser(User user, Model model) {
        try {
            userValidator.validateUser(user);
            log.info("{}", "Validation: success");
        } catch (Exception e) {
            log.info("{}", "Validation error: " + e.getMessage());
            model.addAttribute("message", e.getMessage());

            return "register.html";
        }

        PasswordEncoder encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();

        user.setActive(true);
        user.setRoles(Collections.singleton(Role.USER));
        user.setPassword(encoder.encode(user.getPassword()));

        try {
            userService.saveNewUser(user);
            log.info("{}", "Save new user: success");
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("message", "User already exists");

            log.info("{}", "Save user error: User already exists");

            return "register.html";
        }

        return "redirect:/login";
    }

}
