package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface AccountDao {

    public BigDecimal retrieveBalance(int userId);
    public void updateBalance(BigDecimal amount, int accountId);
    public int getUserIdByAccountId(int accountId);
    public int getAccountIdByUserId(int userId);
}
