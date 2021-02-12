package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.User;
import com.gmail.oleg.restaurantapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.IntStream;

@Controller
@RequestMapping("/user")
@Slf4j
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class UsersController {

    private final UserRepository userRepository;

    @GetMapping
    public String userList(Model model,
                           @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {
        Page<User> pageUsers = userRepository.findAll(PageRequest.of(page, 3));

        model.addAttribute("usersPage", pageUsers);
        model.addAttribute("numbers", IntStream.range(0, pageUsers.getTotalPages()).toArray());
        log.info("{}", "Add information about users");

        return "allUsers";
    }
}
