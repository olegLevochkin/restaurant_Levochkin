package com.gmail.oleg.restaurantapi.service;

import com.gmail.oleg.restaurantapi.model.OrderDish;
import com.gmail.oleg.restaurantapi.repository.OrderDishRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@AllArgsConstructor
public class OrderService {

    private final OrderDishRepository orderDishRepository;

    public Optional<Long> getUnCompletedForUser(String username) {
        return orderDishRepository.findUnCompletedForUser(username);
    }

    public void saveOrder(OrderDish orderDish) {
        orderDishRepository.save(orderDish);
    }

    public Optional<OrderDish> getByID(Long id) {
        return orderDishRepository.findById(id);
    }

    public List<Long> confirmToAdmin() {
        return orderDishRepository.findOrderUncheckedIndex();
    }

    public List<Long> confirmToUser(Long id) {
        return orderDishRepository.findUnConfirmed(id);
    }

    public void confirm(Long indOrder) {
        orderDishRepository.setChecked(indOrder);
    }

    public void payed(Long indOrder) {
        orderDishRepository.setPayed(indOrder);
    }

}
