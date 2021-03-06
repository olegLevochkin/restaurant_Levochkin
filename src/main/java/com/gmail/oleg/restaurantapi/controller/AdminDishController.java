package com.gmail.oleg.restaurantapi.controller;

import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.model.Product;
import com.gmail.oleg.restaurantapi.service.DishService;
import com.gmail.oleg.restaurantapi.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Controller
@PreAuthorize("hasAuthority('ADMIN')")
@RequiredArgsConstructor
public class AdminDishController {
    private final DishService dishService;
    private final ProductService productService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping(value = "/add")
    public String addDish(Model model) {
        model.addAttribute(new Dish());
        model.addAttribute("dishes", dishService.getAllDishes());
        model.addAttribute("products", productService.getAllProducts());

        return "menu/addDish";
    }

    @PostMapping(value = "/add")
    public String processDish(@ModelAttribute @Valid Dish dish,
                              Errors errors,
                              @RequestParam(required = false) Optional<ArrayList<String>> productsNamesList,
                              @RequestParam("file") Optional<MultipartFile> file,
                              Model model) throws IOException {
        if (errors.hasErrors()) {
            model.addAttribute("dishes", dishService.getAllDishes());
            model.addAttribute("products", productService.getAllProducts());
            log.info("Errors occur when adding a dish {}", errors.toString());

            return "menu/addDish";
        }

        if (file.isPresent()) {
            final File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            final String uuidFile = UUID.randomUUID().toString();
            final String resultFilename = uuidFile + "." + file.get().getOriginalFilename();
            file.get().transferTo(new File(uploadPath + "/" + resultFilename));
            dish.setFileName(resultFilename);
            log.info("Image saved successfully {}", resultFilename);
        }

        if (productsNamesList.isPresent()) {
            final List<Product> productsList = new ArrayList<>();
            productsNamesList.get().forEach(product -> productsList.add(productService.getByProductName(product)));
            dish.setProductsForDish(productsList);
            log.info("Products saved successfully {}", productsNamesList.toString());
        }

        try {
            dishService.saveDish(dish);
            log.info("New dish saved successfully {}", dish.getName());
        } catch (DataIntegrityViolationException e) {
            model.addAttribute("error", "Existing dish");
            model.addAttribute(new Dish());
            model.addAttribute("dishes", dishService.getAllDishes());
            model.addAttribute("products", productService.getAllProducts());

            return "menu/addDish";
        }

        return "redirect:/add";
    }

    @PostMapping("/remove")
    public String removeDish(@RequestParam ArrayList<Long> dishesToRemove) {
        dishesToRemove.forEach(dishService::deleteByID);

        return "redirect:/add";
    }
}

