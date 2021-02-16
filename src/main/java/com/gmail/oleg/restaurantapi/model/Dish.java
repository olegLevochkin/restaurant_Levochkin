package com.gmail.oleg.restaurantapi.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.math.BigInteger;
import java.util.List;

@AllArgsConstructor
@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})})
public class Dish implements Comparable<Dish> {

    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany(mappedBy = "dishes", fetch = FetchType.LAZY)
   private List<OrderDish> ordersWithDish;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String fileName;
    @Size(min = 5, max = 25)
    @NotNull(message = "please enter name of dish")
    private String name;
    @Size(min = 5, max = 25)
    @NotNull(message = "please enter name of dish")
    private String nameUkr;
    @NotNull
    @Digits(integer = 5, fraction = 0, message = "please not more then 5 symbols")
    private BigInteger price;
    @OnDelete(action = OnDeleteAction.CASCADE)
    @ManyToMany
    private List<Product> productsForDish;

    @Override
    public int compareTo(Dish o) {
        return this.name.compareTo(o.getName());
    }
}
