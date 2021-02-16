package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.service.DishService;
import com.gmail.oleg.restaurantapi.service.ProductService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@Slf4j
@ControllerAdvice
@AllArgsConstructor
public class FileUploadExceptionAdvice {
    private final DishService dishService;
    private final ProductService productService;

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String handleMaxSizeException(Model model) {
        model.addAttribute("error1", "To huge data of image");
        model.addAttribute(new Dish());
        model.addAttribute("dishes", dishService.getAllDishes());
        model.addAttribute("products", productService.getAllProducts());
        log.info("Error: the input file is huge");

        return "menu/addDish.html";
    }
}
