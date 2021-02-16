package com.gmail.oleg.restaurantapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
public class OrderDish {
    @NotNull
    @ManyToMany
    private List<Dish> dishes;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private boolean checked;
    private boolean toAdmin;
    private boolean payed;
    @Digits(integer = 5, fraction = 0)
    private BigInteger priceAll;
    @ManyToOne
    private User user;

    private boolean completed;

    public void addDish(Dish dish) {
        dishes.add(dish);
    }
}
