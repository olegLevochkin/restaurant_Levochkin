package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {
    private final UserService userService;

    @GetMapping("/login")
    public String login(Model model,
                        @RequestParam(value = "error", defaultValue = "false") String error) {
        if (error.equals("true")) {
            model.addAttribute("loginErrorMessage", "error");
            log.info("Authentication failed");

            return "login";
        }

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (userService.findByUsername(authentication.getName()) != null) {
            return "redirect:/logout";
        }

        return "login";
    }

    @RequestMapping("/logout")
    public String logout(HttpServletRequest request) throws ServletException {
        log.info("Session is over");
        request.logout();

        return "redirect:/login";
    }
}
