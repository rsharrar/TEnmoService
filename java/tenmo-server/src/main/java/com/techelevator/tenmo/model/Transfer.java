package com.techelevator.tenmo.model;

import javax.validation.constraints.Positive;
import java.math.BigDecimal;

public class Transfer {
    @Positive(message="Transfer amount must be greater than zero.")
    private BigDecimal transferAmount;

    private int transferType;
    private int transferStatus;
    private int recipientAccount;
    private int initiatingAccount;
    private int transferId;

    public Transfer(@Positive(message = "Transfer amount must be greater than zero.") BigDecimal transferAmount, int transferType, int transferStatus, int recipientAccount, int initiatingAccount) {
        this.transferAmount = transferAmount;
        this.transferType = transferType;
        this.transferStatus = transferStatus;
        this.recipientAccount = recipientAccount;
        this.initiatingAccount = initiatingAccount;
    }

    public Transfer() {}

    public int getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(int recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public int getInitiatingAccount() {
        return initiatingAccount;
    }

    public void setInitiatingAccount(int initiatingAccount) {
        this.initiatingAccount = initiatingAccount;
    }

    public BigDecimal getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal input){
        this.transferAmount = input;
    }

    public int getTransferType() { return transferType; }

    public void setTransferType(int transferType) {
        this.transferType = transferType;
    }

    public int getTransferStatus() {
        return transferStatus;
    }

    public void setTransferStatus(int transferStatus) {
        this.transferStatus = transferStatus;
    }

    public int getTransferId() {
        return transferId;
    }

    public void setTransferId(int transferId) {
        this.transferId = transferId;
    }
}
