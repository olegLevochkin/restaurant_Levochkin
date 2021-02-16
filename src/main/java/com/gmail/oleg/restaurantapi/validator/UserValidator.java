package com.gmail.oleg.restaurantapi.validator;

import com.gmail.oleg.restaurantapi.exception.ValidatorException;
import com.gmail.oleg.restaurantapi.model.User;
import org.springframework.stereotype.Component;

@Component
public class UserValidator {

    public void validatePassword(String password) {
        boolean result = password.matches("(.*[0-9]+.*)");
        if (!result) {
            throw new ValidatorException("Password must contain at least one digit");
        }
        result = password.matches("(.*[A-Z]+.*)");
        if (!result) {
            throw new ValidatorException("Password must contain at least one capital Latin letter");
        }
        result = password.matches("(.*[a-z]+.*)");
        if (!result) {
            throw new ValidatorException("Password must contain at least one small Latin letter");
        }
        if (password.length() < 6) {
            throw new ValidatorException("Password must be more than 6 characters");
        }
        result = password.matches("(?=.*[A-Z])[0-9a-zA-Z*]{6,}");
        if (!result) {
            throw new ValidatorException("Password must contain only Latin characters");
        }
    }

    public void validateLogin(String login) {
        if (login.length() < 6) {
            throw new ValidatorException("Username must be from 6 characters and contain only Latin characters");
        }
        final boolean result = login.matches("(?=.*[a-zA-Z])[0-9a-zA-Z*]{6,}");
        if (!result) {
            throw new ValidatorException("Username must be from 6 characters and contain only Latin characters");
        }
    }

    public void validateFirstName(String login) {
        final boolean result = login.matches("(?=.*[a-zA-Z])[a-zA-Z*]{2,}");
        if (!result) {
            throw new ValidatorException("First name must contain only Latin characters");
        }
    }

    public void validateLastName(String login) {
        final boolean result = login.matches("(?=.*[a-zA-Z])[a-zA-Z*]{2,}");
        if (!result) {
            throw new ValidatorException("Last name must contain only Latin characters");
        }
    }

    public void validateUser(User user) {
        validateLogin(user.getUsername());
        validateFirstName(user.getFirstName());
        validateLastName(user.getLastName());
        validatePassword(user.getPassword());
    }
}
