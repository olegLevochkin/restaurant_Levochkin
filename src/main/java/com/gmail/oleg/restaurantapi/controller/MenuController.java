package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.model.User;
import com.gmail.oleg.restaurantapi.service.DishService;
import com.gmail.oleg.restaurantapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.stream.IntStream;

@Slf4j
@Controller
@RequiredArgsConstructor
public class MenuController {

    public static final int DEFAULT_PAGE_SIZE = 3;
    private final DishService dishService;
    private final UserService userService;

    @GetMapping
    public String menuPage(Model model,
                           @RequestParam(value = "page", required = false, defaultValue = "0") Integer page) {
        final Page<Dish> dishPage = dishService.getAllDishes(PageRequest.of(page, DEFAULT_PAGE_SIZE));

        model.addAttribute("dishes", dishPage);
        model.addAttribute("numbers", IntStream.range(0, dishPage.getTotalPages()).toArray());

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userService.findByUsername(authentication.getName()) != null) {
            final User user = userService.findByUsername(authentication.getName());
            model.addAttribute("moneyBalance", user.getMoneyHave());
            model.addAttribute("authorized", "true");
            model.addAttribute("isAdmin", user.getFirstName().equals("admin"));
        }

        return "menu/menu";
    }
}
