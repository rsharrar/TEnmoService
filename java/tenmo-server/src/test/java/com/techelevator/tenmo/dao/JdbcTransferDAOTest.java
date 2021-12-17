package com.techelevator.tenmo.dao;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import java.sql.SQLException;

import static org.junit.Assert.*;

public class JdbcTransferDAOTest {
    public static SingleConnectionDataSource ds;
    private JdbcTransferDAO dao;

    @BeforeClass
    public void beforeEverything() {
        ds.setUrl("jdbc:postgresql://localhost:5432/tenmo");
        ds.setUsername("postgres");
        ds.setPassword("postgres1");
        ds.setAutoCommit(false);
    }

    @AfterClass
    public void afterEverything(){
      ds.destroy();
    }

    @Before
    public void beforeEach(){
        this.dao = new JdbcTransferDAO(this.ds);
        loadData();
    }

    @After
    public void afterEach() throws SQLException {
        ds.getConnection().rollback();
    }

    public void loadData(){
        String sql1 = "INSERT INTO users(user_id, username, password_hash) VALUES (9001, 'simon', '1234567890')";
        String sql2 = "INSERT INTO accounts(account_id, user_id, balance) VALUES (7777, 9001, 1000)";
        String sql3 = "INSERT INTO transfers() VALUES ()";

    }

}