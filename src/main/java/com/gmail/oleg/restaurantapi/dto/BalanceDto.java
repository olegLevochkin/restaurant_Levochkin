package com.gmail.oleg.restaurantapi.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Digits;
import java.math.BigInteger;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BalanceDto {
    @Digits(integer = 5, fraction = 0, message = "please not more then 5 symbols")
    private BigInteger moneyToAdd;

}
