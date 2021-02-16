package com.gmail.oleg.restaurantapi.service;

import com.gmail.oleg.restaurantapi.model.Dish;
import com.gmail.oleg.restaurantapi.repository.DishRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DishService {

    private final DishRepository dishRepository;

    public List<Dish> getAllDishes() {
        return dishRepository.findAll();
    }

    public Page<Dish> getAllDishes(Pageable pageable) {
        final int pageSize = pageable.getPageSize();
        final int currentPage = pageable.getPageNumber();
        final int startItem = currentPage * pageSize;
        final List<Dish> list;
        final List<Dish> dishes = dishRepository.findAll();

        if (dishes.size() < startItem) {
            list = Collections.emptyList();
        } else {
            int toIndex = Math.min(startItem + pageSize, dishes.size());
            list = dishes.subList(startItem, toIndex);
        }

        return new PageImpl<>(list, PageRequest.of(currentPage, pageSize), dishes.size());
    }

    public void saveDish(Dish dish) {
        dishRepository.save(dish);
    }

    public void deleteByID(Long id) {
        dishRepository.deleteById(id);
    }

    public List<Dish> findByOrderID(Long id) {
        return dishRepository.findByOrder(id);
    }

    public List<Dish> findByOrderIDToUSer(Long id) {
        return dishRepository.findByOrderToUser(id);
    }

    public Dish findByDishName(String dishName) {
        return dishRepository.findByName(dishName);

    }
}
