package com.gmail.oleg.restaurantapi.service;

import com.gmail.oleg.restaurantapi.model.User;
import com.gmail.oleg.restaurantapi.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public Long getUserIdByUsername(String username) {
        return userRepository.findUserId(username);
    }

    public void saveNewUser(User user) throws DataIntegrityViolationException {
        userRepository.save(user);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void addMoney(BigInteger moneyToAdd, String username) {
        User user = findByUsername(username);
        user.setMoneyHave(user.getMoneyHave().add(moneyToAdd));
        saveNewUser(user);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW,
            rollbackFor = BankTransactionException.class)
    public void payTheOrder(BigInteger sum) throws BankTransactionException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (findByUsername(authentication.getName()).getMoneyHave().compareTo(sum) < 0) {
            throw new BankTransactionException(
                    "The money in the account is not enough ("
                            + findByUsername(authentication.getName()).getMoneyHave() + ")");
        }

        addMoney(sum, "admin");
        addMoney(sum.multiply(BigInteger.valueOf(-1)), authentication.getName());
    }
}
