package com.techelevator.tenmo.dao;

import java.math.BigDecimal;

public interface AccountDao {

    public BigDecimal retrieveBalance(int userId);
}
