package com.techelevator.tenmo.dao;

import com.techelevator.tenmo.model.Transfer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Component
public class JdbcTransferDAO implements TransferDAO {

    private JdbcTemplate template;

    public JdbcTransferDAO(DataSource dataSource){
        this.template = new JdbcTemplate(dataSource);
    }

    public Transfer createTransfer(Transfer transfer){
        String sql = "INSERT INTO transfers (transfer_type_id, transfer_status_id, account_from, account_to, amount) " +
                "VALUES(?, ?, ?, ?, ?) " +
                "RETURNING transfer_id ";

        int id = template.queryForObject(sql, Integer.class, transfer.getTransferType(), transfer.getTransferStatus(), transfer.getInitiatingAccount(), transfer.getRecipientAccount(), transfer.getTransferAmount());
        return getTransferById(id);
    }

    public Transfer getTransferById(int id){
        String sql = "SELECT * FROM transfers " +
                "WHERE transfer_id = ? ";
        SqlRowSet row = template.queryForRowSet(sql, id);
        if(row.next()) return mapRowsToTransfer(row);
        else return null;
    }

    public List<Transfer> getTransfersByAccountId(int accountId, int statusId) {
        String sql = "SELECT * FROM transfers " +
                "WHERE (account_from = ? OR account_to = ?)  AND transfer_status_id = ? ";
        SqlRowSet results = template.queryForRowSet(sql, accountId, accountId, statusId);

        List<Transfer> transfers = new ArrayList<>();
        while (results.next()) {
            transfers.add(mapRowsToTransfer(results));
        }
        return transfers;
    }

    public void updateTransfer(Transfer transfer){
        String sql = "UPDATE transfers " +
                "SET transfer_status_id = ? " +
                "WHERE transfer_id = ? ";
        template.update(sql, transfer.getTransferStatus(), transfer.getTransferId());
    }

    public Transfer mapRowsToTransfer(SqlRowSet row){
        Transfer t = new Transfer();
        t.setTransferType(row.getInt("transfer_type_id"));
        t.setTransferStatus(row.getInt("transfer_status_id"));
        t.setInitiatingAccount(row.getInt("account_from"));
        t.setRecipientAccount(row.getInt("account_to"));
        t.setTransferAmount(row.getBigDecimal("amount"));
        t.setTransferId(row.getInt("transfer_id"));
        return t;
    }
}
