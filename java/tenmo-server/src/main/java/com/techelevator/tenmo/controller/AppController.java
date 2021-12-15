package com.techelevator.tenmo.controller;

import com.techelevator.tenmo.dao.AccountDao;
import com.techelevator.tenmo.dao.TransferDAO;
import com.techelevator.tenmo.dao.UserDao;
import com.techelevator.tenmo.exception.InsufficientFundsException;
import com.techelevator.tenmo.model.Balance;
import com.techelevator.tenmo.model.Transfer;
import com.techelevator.tenmo.model.User;
import org.hibernate.validator.constraints.pl.REGON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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

    @Autowired
    TransferDAO transferDAO;

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
    public Transfer createTransfer(@RequestBody Transfer transfer) throws InsufficientFundsException {
       if (transfer.getTransferAmount().getBalance().compareTo(accountDao.retrieveBalance(transfer.getInitiatingAccount())) > 0) {
           throw new InsufficientFundsException();
       }
        accountDao.updateBalance(transfer.getTransferAmount().getBalance().multiply(new BigDecimal("-1")), transfer.getInitiatingAccount());
        accountDao.updateBalance(transfer.getTransferAmount().getBalance(), transfer.getRecipientAccount());
        return transferDAO.createTransfer(transfer);
    }

    @RequestMapping(path="/transfer/{id}", method = RequestMethod.GET)
    public Transfer retrieveTransferById(@PathVariable int id) {
        return transferDAO.getTransferById(id);
    }

    @RequestMapping(path = "/transfers", method = RequestMethod.GET)
    public List<Transfer> retrieveTransfersByUser(Principal principal) {
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        int accountId = accountDao.getAccountIdByUserId(userId);
        return transferDAO.getTransfersByAccountId(accountId);
    }

}
