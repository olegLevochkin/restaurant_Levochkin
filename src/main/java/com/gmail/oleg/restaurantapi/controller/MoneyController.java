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
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        model.addAttribute(new BalanceDto());
        model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                .getMoneyHave());

        return "menu/addBalance";
    }

    @PostMapping(value = "/addBalance")
    public String addMoneyBase(@ModelAttribute @Valid BalanceDto balanceDto,
                               Errors errors,
                               Model model) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (errors.hasErrors()) {
            model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                    .getMoneyHave());
            log.info("Error when adding balance");

            return "menu/addBalance";
        }

        final User user = userService.findByUsername(authentication.getName());
        user.setMoneyHave(user.getMoneyHave().add(balanceDto.getMoneyToAdd()));
        userService.saveNewUser(user);
        log.info("Balance has been successfully replenished: {}", user.getMoneyHave());

        return "redirect:/addMoney";
    }

    @GetMapping("/user_confirm")
    public String userConfirm(Model model) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        model.addAttribute("ind",
                orderService.confirmToUser(userService.getUserIdByUsername(authentication.getName())));

        return "menu/PayOrder";
    }

    private Map<String, Integer> prodNeeded(List<String> products) {
        final Map<String, Integer> needProductsToOrder = new HashMap<>();
        products.stream()
                .map(productService::getByProductName)
                .forEach(product -> {
                    if (needProductsToOrder.containsKey(product.getProduct())) {
                        needProductsToOrder.put(product.getProduct(), needProductsToOrder.get(product.getProduct()) + 1);
                    } else {
                        needProductsToOrder.put(product.getProduct(), 1);
                    }
                });

        return needProductsToOrder;
    }

    @GetMapping("/checkOrderUser")
    public String checkOrderUSer(@RequestParam Long index, Model model) {
        model.addAttribute("index", index);
        final Map<Dish, Long> orderClient = new HashMap<>();

        dishService.findByOrderIDToUSer(index).stream()
                .distinct()
                .forEach(dish -> orderClient.put(dish, dishService
                        .findByOrderIDToUSer(index).stream()
                        .filter(dishToCount -> dishToCount.equals(dish)).count()));

        final List<String> products = productService.getAllProductsFromOrder(index);

        final Map<String, Integer> enoughProducts = prodNeeded(products);

        final boolean isEnoughProducts = products.stream()
                .distinct()
                .map(productService::getByProductName)
                .anyMatch(product -> product.getAmountHave() - enoughProducts.get(product.getProduct()) < 0);

        if (isEnoughProducts) {
            model.addAttribute("isEnough", index);
        }

        model.addAttribute("orderClient", orderClient);
        model.addAttribute("price", orderService.getByID(index).get().getPriceAll());

        return "menu/checkPageUser";
    }

    @GetMapping("/checkOrderUser/Confirm")
    public String notEnoughMoney() {
        return "redirect:/user_confirm";
    }

    @PostMapping("/checkOrderUser/Confirm")
    public String confirm(@RequestParam Long index,
                          @RequestParam BigInteger price,
                          Model model) {
        if (orderService.getByID(index).get().isPayed()) {
            return "redirect:/user_confirm";
        }

        final List<String> products = productService.getAllProductsFromOrder(index);
        final Map<String, Integer> enoughProducts = prodNeeded(products);
        final String prodLike = enoughProducts.keySet().stream()
                .filter(productName -> enoughProducts.get(productName)
                        .equals(enoughProducts
                                .keySet().stream()
                                .map(enoughProducts::get)
                                .max(Integer::compareTo)
                                .get()
                        )).toString();

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final User user = userService.findByUsername(authentication.getName());
        user.setProdLikeNow(productService.getByProductName(prodLike));

        userService.saveNewUser(user);

        final boolean isEnoughProducts = products.stream()
                .distinct()
                .map(productService::getByProductName)
                .anyMatch(product -> product.getAmountHave() - enoughProducts.get(product.getProduct()) < 0);

        if (isEnoughProducts) {
            model.addAttribute("ind",
                    orderService.confirmToUser(userService.getUserIdByUsername(authentication.getName())));
            model.addAttribute("notEnough", index);

            return "menu/PayOrder";
        }
        try {
            userService.payTheOrder(price);
        } catch (BankTransactionException e) {

            model.addAttribute(new BalanceDto());
            model.addAttribute("moneyBalance", userService.findByUsername(authentication.getName())
                    .getMoneyHave());
            model.addAttribute("notEnoughMoney", "not enough");

            return "menu/addBalance";
        }
        productService.getAllProductsFromOrder(index).stream()
                .map(productService::getByProductName)
                .forEach(product -> {
                    product.setAmountHave(product.getAmountHave() - 1);
                    productService.saveProduct(product);
                });

        orderService.payed(index);

        return "redirect:/";
    }
}

