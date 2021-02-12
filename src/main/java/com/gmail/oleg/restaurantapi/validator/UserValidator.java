package com.gmail.oleg.restaurantapi.validator;

import com.gmail.oleg.restaurantapi.exception.ValidatorException;
import com.gmail.oleg.restaurantapi.model.Role;
import com.gmail.oleg.restaurantapi.model.User;
import com.gmail.oleg.restaurantapi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.Set;

@Component
public class UserValidator {

    private final UserRepository userRepository;

    public UserValidator(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void userRolesUpdateValidator(Long userId, Set<String> roles) {
        for (String role : roles) {
            try {
                Role.valueOf(role);
            } catch (Exception e) {
                throw new ValidatorException(String.format("Not existing role: %s", role));
            }
        }
        if (Objects.isNull(userRepository.getUserById(userId))) {
            throw new ValidatorException(String.format("User with id : %s, not exists", userId));
        }
    }

    public void validateUserPermissions(Long id) {
        final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        final String userName = authentication != null ? authentication.getName() : null;
        final User userInContext = userRepository.findByUsername(userName);
        final boolean isAdmin = userInContext.getRoles().stream().anyMatch(role -> role == Role.ADMIN);

        if (!isAdmin && !id.equals(userInContext.getId())) {
            throw new ValidatorException("You do not have permission to change password");
        }
    }

    public void validateUserExistence(Long id) {
        if (Objects.isNull(userRepository.getUserById(id))) {
            throw new ValidatorException(String.format("User with id : %s, not exists", id));
        }
    }

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
            throw new ValidatorException("Username must be more than 6 characters");
        }
        boolean result = login.matches("(?=.*[a-zA-Z])[0-9a-zA-Z*]{6,}");
        if (!result) {
            throw new ValidatorException("Username must contain only Latin characters");
        }
    }

    public void validateFirstName(String login) {
        boolean result = login.matches("(?=.*[a-zA-Z])[a-zA-Z*]{2,}");
        if (!result) {
            throw new ValidatorException("First name must contain only Latin characters");
        }
    }

    public void validateLastName(String login) {
        boolean result = login.matches("(?=.*[a-zA-Z])[a-zA-Z*]{2,}");
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
