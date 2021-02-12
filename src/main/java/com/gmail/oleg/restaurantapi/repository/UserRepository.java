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

    User getUserById(Long id);

    Page<User> findAll(Pageable pageable);

    @Query(value = "select id from user_all u where u.username=:username",
            nativeQuery = true)
    Long findUserId(@Param("username") String username);
}
