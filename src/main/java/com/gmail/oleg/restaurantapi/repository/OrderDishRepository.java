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

    @Query(value = "select id,checked,price_all,to_admin,user_id,payed from order_dish where id=:id",
            nativeQuery = true)
    OrderDish getByOrderID(@Param("id") Long longn);

    @Query(value = "select id from order_dish d where d.checked=0&&d.to_admin=1",
            nativeQuery = true)
    List<Long> findOrderUncheckedIndex();

    @Query(value = "select id from order_dish d where d.checked=1&&d.payed=0&&d.user_id=:id",
            nativeQuery = true)
    List<Long> findUnConfirmed(@Param("id") Long longn);

    @Modifying
    @Query(value = "update order_dish o set checked = 1 where o.id=:id",
            nativeQuery = true)
    void setChecked(@Param("id") Long longn);

    @Modifying
    @Query(value = "update order_dish o set payed = 1, completed=1 where o.id=:id",
            nativeQuery = true)
    void setPayed(@Param("id") Long longn);

    @Query(value = "select od.id from order_dish od where user_id = (select id from user_all where username=:username) and od.completed = 0",
            nativeQuery = true)
    Optional<Long> findUnCompletedForUser(@Param("username") String username);

}
