package com.gmail.oleg.restaurantapi.dto;

import lombok.Data;

@Data
public class UserDto {

    private Long id;

    private String username;

    private String password;

    private String firstName;

    private String lastName;
}
