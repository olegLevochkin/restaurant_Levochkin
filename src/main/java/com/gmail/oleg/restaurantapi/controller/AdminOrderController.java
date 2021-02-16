package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.model.Product;
import com.gmail.oleg.restaurantapi.service.DishService;
import com.gmail.oleg.restaurantapi.service.OrderService;
import com.gmail.oleg.restaurantapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminOrderController {
    private final DishService dishService;
    private final OrderService orderService;
    private final ProductService productService;

    @GetMapping("/adminOrder")
    public String adminOrder(Model model) {
        model.addAttribute("orderID", orderService.confirmToAdmin());
        model.addAttribute("products", productService.getAllProducts());

        if (productService.getAllProducts().stream()
                .anyMatch(product -> product.getAmountHave() < product.getMinAmount())) {
            return replenish();
        }
        log.info("Information for admin about orders");

        return "menu/adminOrder";
    }

    @PostMapping("/replenish_stock_of_products")
    public String replenish() {
        final List<Product> products = productService.getAllProducts();
        products.forEach(product -> {
            product.setAmountHave(product.getAmountHave() + ((product.getMaxAmount() - product.getAmountHave())));
            productService.saveProduct(product);
            log.info("The amount of product {} is filled to the maximum", product.getProduct());
        });

        return "redirect:/adminOrder";
    }

    @GetMapping("/checkOrder")
    public String checkOrder(@RequestParam Long orderID,
                             Model model) {
        model.addAttribute("orderID", orderID);

        final Map<Dish, Long> orderClient = new HashMap<>();
        dishService.findByOrderID(orderID)
                .stream()
                .distinct()
                .forEach(dish -> orderClient.put(dish, dishService.findByOrderID(orderID).stream()
                        .filter(dishToCount -> dishToCount.equals(dish))
                        .count()));
        model.addAttribute("orderClient", orderClient);

        final Map<String, Integer> needProductsToOrder = new HashMap<>();

        productService.getAllProductsFromOrder(orderID).forEach(product -> {
            if (needProductsToOrder.containsKey(product)) {
                needProductsToOrder.put(product, needProductsToOrder.get(product) + 1);
            } else {
                needProductsToOrder.put(product, 1);
            }
        });

        model.addAttribute("products", needProductsToOrder);

        return "menu/checkPage";
    }

    @PostMapping("/checkOrder/Confirm")
    public String confirm(@RequestParam Long orderID) {
        orderService.confirm(orderID);

        return "redirect:/adminOrder";
    }
}

