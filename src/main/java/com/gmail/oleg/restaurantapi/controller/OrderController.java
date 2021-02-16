package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.model.OrderDish;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigInteger;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.IntStream;

@Slf4j
@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {
    private final DishService dishService;
    private final UserService userService;
    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping
    public String orderPage(Model model) {

        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final Optional<Long> notCompletedOrderId = orderService.getUnCompletedForUser(username);
        final OrderDish orderDish;

        if (notCompletedOrderId.isPresent()) {
            orderDish = orderService.getByID(notCompletedOrderId.get()).get();
        } else {
            return "redirect:/";
        }

        final Map<Dish, Long> orderClient = new HashMap<>();
        dishService.findByOrderID(orderDish.getId()).stream()
                .distinct().forEach(dish -> orderClient.put(dish, dishService
                .findByOrderID(orderDish.getId()).stream()
                .filter(dishName -> dishName.equals(dish)).count()));

        model.addAttribute("map", orderClient);
        log.info("Add client order to page");

        model.addAttribute("amount", dishService.findByOrderID(orderDish.getId()).stream()
                .map(Dish::getPrice)
                .mapToInt(BigInteger::intValue)
                .sum());
        log.info("Add total information about order");

        final List<String> products = productService.getAllProductsFromOrder(orderDish.getId());
        final Map<String, Integer> enoughProducts = getProductNeededForOrder(products);

        if (products.stream()
                .distinct()
                .map(productService::getByProductName)
                .anyMatch(product -> (product.getAmountHave() - enoughProducts.get(product.getProduct())) < 0)) {
            model.addAttribute("notEnough", "we dont have enough products");
            log.info("Error : not enough products for dishes");
        }

        return "menu/order";
    }

    private Map<String, Integer> getProductNeededForOrder(List<String> products) {
        final Map<String, Integer> enoughProducts = new HashMap<>();
        products.stream()
                .map(productService::getByProductName)
                .forEach(s -> {
                    if (enoughProducts.containsKey(s.getProduct())) {
                        enoughProducts.put(s.getProduct(), enoughProducts.get(s.getProduct()) + 1);
                    } else {
                        enoughProducts.put(s.getProduct(), 1);
                    }
                });

        return enoughProducts;
    }

    @PostMapping("/removeD")
    public String deleteDishFromOrder(@RequestParam String name) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final Optional<Long> notCompletedOrderId = orderService.getUnCompletedForUser(username);
        final OrderDish orderDish;

        if (notCompletedOrderId.isPresent()) {
            orderDish = orderService.getByID(notCompletedOrderId.get()).get();
        } else {
            return "redirect:/";
        }

        IntStream.range(0, orderDish.getDishes().size())
                .filter(dish -> orderDish.getDishes().get(dish).getName().equals(name)).limit(1)
                .forEach(dishItem -> {
                    orderDish.setPriceAll(orderDish.getPriceAll().subtract(orderDish.getDishes()
                            .get(dishItem).getPrice()));
                    orderDish.getDishes().remove(dishItem);
                });
        log.info("Delete from order dish:  {}", name);

        orderService.saveOrder(orderDish);
        log.info("Updated the order after deleting the dish");

        return "redirect:/order";
    }

    @PostMapping("/addToCard")
    public String addOrder(@RequestParam String dish) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final Optional<Long> notCompletedOrderId = orderService.getUnCompletedForUser(username);
        final OrderDish orderDish;

        if (notCompletedOrderId.isPresent()) {
            orderDish = orderService.getByID(notCompletedOrderId.get()).get();
            orderDish.addDish(dishService.findByDishName(dish));
        } else {
            orderDish = new OrderDish();
            orderDish.setDishes(Collections.singletonList(dishService.findByDishName(dish)));
            orderDish.setPayed(false);
            orderDish.setChecked(false);
            orderDish.setToAdmin(false);
            orderDish.setCompleted(false);
            orderDish.setUser(userService.findByUsername(username));
        }

        orderDish.setPriceAll(BigInteger.valueOf(orderDish.getDishes().stream()
                .map(Dish::getPrice)
                .mapToInt(BigInteger::intValue)
                .sum()));

        orderService.saveOrder(orderDish);
        log.info("Updated the order after adding the dish");

        return "redirect:/";
    }

    @PostMapping("/addedOrder")
    public String addToAdmin() {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        final String username = authentication.getName();
        final Optional<Long> notCompletedOrderId = orderService.getUnCompletedForUser(username);
        final OrderDish orderDish;

        if (notCompletedOrderId.isPresent()) {
            orderDish = orderService.getByID(notCompletedOrderId.get()).get();
        } else {
            return "redirect:/";
        }

        final List<String> products = productService.getAllProductsFromOrder(notCompletedOrderId.get());
        final Map<String, Integer> enoughProducts = getProductNeededForOrder(products);

        final boolean isEnoughProducts = products .stream()
                .distinct()
                .map(productService::getByProductName)
                .anyMatch(s -> s.getAmountHave() - enoughProducts.get(s.getProduct()) < 0);

        if (isEnoughProducts) {
            return "redirect:/order";
        }
        orderDish.setToAdmin(true);
        orderDish.setCompleted(true);
        orderService.saveOrder(orderDish);
        log.info("Updated order after users check");

        return "redirect:/";
    }

}

