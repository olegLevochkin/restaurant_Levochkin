package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Dish;
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

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Slf4j
@Controller
@RequestMapping("/")
@RequiredArgsConstructor
public class MenuController {

    private final DishService dishService;
    private final UserService userService;

    @GetMapping
    public String menuPage(Model model, @RequestParam("page") Optional<Integer> page) {
        int currentPage = page.orElse(1);
        int pageSize = 4;
        Page<Dish> dishPage = dishService.getAllDishes(PageRequest.of(currentPage - 1, pageSize));

        model.addAttribute("dishes", dishPage);
        log.info("{}", "Add dishes to menu page ");

//        int totalPages = dishPage.getTotalPages();
//        if (totalPages > 0) {
//            List<Integer> pageNumbers = IntStream.rangeClosed(1, totalPages)
//                    .boxed()
//                    .collect(Collectors.toList());
//            model.addAttribute("pageNumbers", pageNumbers);
//        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (userService.findByUsername(authentication.getName()) != null) {
            model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                    .getMoneyHave());
            model.addAttribute("authorized", "true");
            log.info("{}", "Add user balance to menu page ");
        }

        return "menu/menu.html";
    }

}
