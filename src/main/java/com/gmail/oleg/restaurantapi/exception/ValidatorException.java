package com.gmail.oleg.restaurantapi.exception;

public class ValidatorException extends IllegalArgumentException {

    public ValidatorException(String errorMessage) {
        super(errorMessage);
    }

    public ValidatorException(String message, Throwable cause) {
        super(message, cause);
    }
}

