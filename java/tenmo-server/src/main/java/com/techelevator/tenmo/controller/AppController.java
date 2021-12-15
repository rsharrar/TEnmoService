package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.User;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@RestController
@PreAuthorize("isAuthenticated()")
public class AppController {

    @Autowired
    AccountDao accountDao;

    @Autowired
    UserDao userDao;

    @RequestMapping(path = "/balance", method = RequestMethod.GET)
    public Balance obtainBalance(Principal principal) {

        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);

        BigDecimal balance = accountDao.retrieveBalance(userId);

        Balance balanceObject = new Balance();
        balanceObject.setBalance(balance);

        return balanceObject;
    }

    @RequestMapping(path="/users", method = RequestMethod.GET)
    public List<User> retrieveAllUsers() { return userDao.findAll(); }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public

}
