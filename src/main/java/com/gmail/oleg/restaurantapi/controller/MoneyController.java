package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.dto.BalanceDto;
import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.model.User;
import com.gmail.oleg.restaurantapi.service.BankTransactionException;
import com.gmail.oleg.restaurantapi.service.DishService;
import com.gmail.oleg.restaurantapi.service.OrderService;
import com.gmail.oleg.restaurantapi.service.ProductService;
import com.gmail.oleg.restaurantapi.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.validation.Valid;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@Slf4j
@RequiredArgsConstructor
public class MoneyController {
    private final UserService userService;
    private final OrderService orderService;
    private final DishService dishService;
    private final ProductService productService;

    @GetMapping("/addMoney")
    public String addMoney(Model model) {
        model.addAttribute(new BalanceDto());
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                .getMoneyHave());
        log.info("{}", "Add information about balance");

        return "menu/addBalance.html";
    }

    @PostMapping(value = "/addBalance")
    public String addMoneyBase(@ModelAttribute @Valid BalanceDto balanceDto, Errors errors,
                               Model model) {
        if (errors.hasErrors()) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                    .getMoneyHave());
            log.info("{}", "Error when adding balance ");

            return "menu/addBalance.html";
        }
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(authentication.getName());
        user.setMoneyHave(user.getMoneyHave().add(balanceDto.getMoneyToAdd()));
        userService.saveNewUser(user);
        log.info("{}", "Balance has been successfully replenished: " + user.getMoneyHave());

        return "redirect:/addMoney";
    }

    @GetMapping("/user_confirm")
    public String userConfirm(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("ind",
                orderService.confirmToUser(userService.getUserIdByUsername(authentication.getName())));

        return "menu/PayOrder";
    }

    private Map<String, Integer> prodNeeded(List<String> products) {
        Map<String, Integer> enoughtProducts = new HashMap<>();
        products
                .stream()
                .map(productService::getByProductName)
                .map(product -> enoughtProducts.containsKey(product.getProduct())
                        ? enoughtProducts.put(product.getProduct(), enoughtProducts.get(product.getProduct()) + 1)
                        : enoughtProducts.put(product.getProduct(), 1)).collect(Collectors.toSet());

        return enoughtProducts;
    }

    @GetMapping("/checkOrderUser")
    public String checkOrderUSer(@RequestParam Long ind, Model model) {
        model.addAttribute("index", ind);
        Map<Dish, Long> orderClient = new HashMap<>();

        dishService.findByOrderIDToUSer(ind)
                .stream().distinct().forEach(s -> orderClient.put(s, dishService
                .findByOrderIDToUSer(ind).stream()
                .filter(x -> x.equals(s)).count()));

        List<String> products = productService.getAllProductsFromOrder(ind);
        Map<String, Integer> enoughtProducts = prodNeeded(products);

        if (products.stream().distinct().map(productService::getByProductName)
                .anyMatch(s -> s.getAmountHave() - enoughtProducts.get(s.getProduct()) < 0)) {
            model.addAttribute("notEnought", ind);
        }

        model.addAttribute("orderClient", orderClient);
        model.addAttribute("price", orderService.getByID(ind).get().getPriceAll());

        return "menu/checkPageUser.html";
    }

    @GetMapping("/checkOrderUser/Confirm")
    public String notEnoughtMoney() {
        return "redirect:/user_confirm";
    }

    @PostMapping("/checkOrderUser/Confirm")
    public String confirm(@RequestParam Long ind,
                          @RequestParam BigInteger price, Model model) {
        if (orderService.getByID(ind).get().isPayed()) {
            return "redirect:/user_confirm";
        }

        List<String> products = productService.getAllProductsFromOrder(ind);
        Map<String, Integer> enoughtProducts = prodNeeded(products);
        String prodLike = enoughtProducts.keySet().stream().filter(s -> enoughtProducts.get(s)
                .equals(enoughtProducts
                        .keySet()
                        .stream()
                        .map(enoughtProducts::get)
                        .max(Integer::compareTo)
                        .get()
                )).toString();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userService.findByUsername(authentication.getName());
        user.setProdLikeNow(productService.getByProductName(prodLike));

        userService.saveNewUser(user);

        if (products.stream().distinct().map(productService::getByProductName)
                .anyMatch(s -> s.getAmountHave() - enoughtProducts.get(s.getProduct()) < 0)) {
            model.addAttribute("ind",
                    orderService.confirmToUser(userService.getUserIdByUsername(authentication.getName())));
            model.addAttribute("notEnought", ind);

            return "menu/PayOrder.html";
        }
        try {
            userService.payTheOrder(price);
        } catch (BankTransactionException e) {

            model.addAttribute(new BalanceDto());
            model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                    .getMoneyHave());
            model.addAttribute("notEnoughtMoney", "nEnought");

            return "menu/addBalance.html";
        }
        productService.getAllProductsFromOrder(ind).stream().map(productService::getByProductName)
                .forEach(s -> {
                    s.setAmountHave(s.getAmountHave() - 1);
                    productService.saveProduct(s);
                });

        orderService.payed(ind);
        return "redirect:/";
    }
}

