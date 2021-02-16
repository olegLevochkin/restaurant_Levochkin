package com.gmail.oleg.restaurantapi.repository;

import com.gmail.oleg.restaurantapi.model.OrderDish;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
@Transactional
public interface OrderDishRepository extends JpaRepository<OrderDish, Long> {

    @Query(value = "select d.id from OrderDish d where d.checked=false AND d.toAdmin=true ")
    List<Long> findOrderUncheckedIndex();

    @Query(value = "select d.id from OrderDish d where d.checked=true AND d.payed=false AND d.user=:id")
    List<Long> findUnConfirmed(@Param("id") Long longn);

    @Modifying
    @Query(value = "update OrderDish o set o.checked = 1 where o.id=:id")
    void setChecked(@Param("id") Long longn);

    @Modifying
    @Query(value = "update OrderDish o set o.payed = 1, o.completed=1 where o.id=:id")
    void setPayed(@Param("id") Long longn);

    @Query(value = "select od.id from order_dish od where user_id = (select id from user_all where username=:username) and od.completed = 0",
            nativeQuery = true)
    Optional<Long> findUnCompletedForUser(@Param("username") String username);

}
