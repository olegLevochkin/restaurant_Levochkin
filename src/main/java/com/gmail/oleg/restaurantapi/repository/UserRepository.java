package com.gmail.oleg.restaurantapi.repository;

import com.gmail.oleg.restaurantapi.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    Page<User> findAll(Pageable pageable);

    @Query(value = "select u.id from User u where u.username=:username")
    Long findUserId(@Param("username") String username);
}
