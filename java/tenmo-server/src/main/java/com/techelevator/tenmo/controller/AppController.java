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
import org.springframework.data.relational.core.sql.In;
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

    @RequestMapping(path="/accounts/{accountId}/user", method = RequestMethod.GET)
    public User retrieveUserByAccountId(@PathVariable int accountId) {
        return accountDao.getUserByAccountId(accountId);
    }

    @RequestMapping(path = "/transfer", method = RequestMethod.POST)
    public Transfer createTransfer(@RequestBody Transfer transfer) throws InsufficientFundsException {
        if(!checkFundsAvailable(transfer)){ throw new InsufficientFundsException(); }
        if(transfer.getTransferStatus() == 2) updateAccounts(transfer);
        Transfer createdTransfer = transferDAO.createTransfer(transfer);
        return createdTransfer;
    }

    @RequestMapping(path="/transfer/{id}", method = RequestMethod.GET)
    public Transfer retrieveTransferById(@PathVariable int id) {
        return transferDAO.getTransferById(id);
    }

    @RequestMapping(path = "/transfers/{id}", method = RequestMethod.GET)
    public List<Transfer> retrieveTransfersByUser(Principal principal, @PathVariable("id") int statusId) {
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        int accountId = accountDao.getAccountIdByUserId(userId);
        return transferDAO.getTransfersByAccountId(accountId, statusId);
    }

    @RequestMapping(path = "/user/{userId}/account", method = RequestMethod.GET)
    public int retrieveAccountIdByUserId(@PathVariable int userId){ return accountDao.getAccountIdByUserId(userId); }

    @RequestMapping(path = "/review-request", method = RequestMethod.PUT)
    public String reviewRequest(@RequestBody Transfer transfer, Principal principal) throws InsufficientFundsException{
        if(!checkFundsAvailable(transfer)){ throw new InsufficientFundsException(); }
        String name = principal.getName();
        int userId = userDao.findIdByUsername(name);
        int accountId = accountDao.getAccountIdByUserId(userId);
        if(transfer.getInitiatingAccount() == accountId && transfer.getTransferStatus() == 2){
            transferDAO.updateTransfer(transfer);
            updateAccounts(transfer);
            return "APPROVED!";
        }else if(transfer.getInitiatingAccount() != accountId){
            return "You can't approve your own requests...";
        }
        return "Rejected.";
    }

    private boolean checkFundsAvailable(Transfer transfer){
        if (transfer.getTransferAmount().compareTo(accountDao.retrieveBalance(accountDao.getUserIdByAccountId(transfer.getInitiatingAccount()))) > 0){ return false; }
        else return true;
    }

    private void updateAccounts(Transfer transfer){
        accountDao.updateBalance(transfer.getTransferAmount().multiply(new BigDecimal("-1")), transfer.getInitiatingAccount());
        accountDao.updateBalance(transfer.getTransferAmount(), transfer.getRecipientAccount());
    }
}
