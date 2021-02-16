package com.gmail.oleg.restaurantapi.exception;

public class ValidatorException extends IllegalArgumentException {

    public ValidatorException(String errorMessage) {
        super(errorMessage);
    }

}

