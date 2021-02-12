package com.gmail.oleg.restaurantapi.service;

import com.gmail.oleg.restaurantapi.model.Product;
import com.gmail.oleg.restaurantapi.repository.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;

    public Product getByProductName(String product) {
        return productRepository.findByProduct(product);
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<String> getAllProductsFromOrder(Long orderID) {
        return productRepository.getProductsFromOrder(orderID);
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }
}
