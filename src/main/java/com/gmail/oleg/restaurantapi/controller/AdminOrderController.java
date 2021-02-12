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
import java.util.stream.Collectors;

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
        log.info("{}", "Data on unconfirmed orders was transmitted ");

        model.addAttribute("products", productService.getAllProducts());
        log.info("{}", "product data transferred ");

        if (productService.getAllProducts().stream().anyMatch(s -> s.getAmountHave() < s.getMinAmount())) {
            return replenish();
        }

        return "menu/adminOrder.html";
    }

    @PostMapping("/replenish_stock_of_products")
    public String replenish() {
        List<Product> products = productService.getAllProducts();
        products.forEach(product -> {
            product.setAmountHave(product.getAmountHave()
                    + product.getProductInBox() * ((product.getMaxAmount() - product.getAmountHave()) / product.getProductInBox()));

            productService.saveProduct(product);
            log.info("{}", "Product replenishment was successful ");
        });

        return "redirect:/adminOrder";
    }

    @GetMapping("/checkOrder")
    public String checkorder(@RequestParam Long orderID, Model model) {
        model.addAttribute("orderID", orderID);
        log.info("{}", "Order number: " + orderID);

        Map<Dish, Long> orderClient = new HashMap<>();
        dishService.findByOrderID(orderID)
                .stream().distinct().forEach(s -> orderClient.put(s, dishService
                .findByOrderID(orderID).stream()
                .filter(x -> x.equals(s)).count()));
        model.addAttribute("orderClient", orderClient);

        Map<String, Long> neededProducts = new HashMap<>();
        productService.getAllProductsFromOrder(orderID).stream().map(s -> neededProducts.containsKey(s)
                ? neededProducts.put(s, neededProducts.get(s) + 1L)
                : neededProducts.put(s, 1L)).collect(Collectors.toList());

        model.addAttribute("products", neededProducts);

        return "menu/checkPage.html";
    }

    @PostMapping("/checkOrder/Confirm")
    public String confirm(@RequestParam Long orderID) {
        orderService.confirm(orderID);

        return "redirect:/adminOrder";
    }
}

