package com.techelevator.tenmo.model;

import java.math.BigDecimal;

public class Transfer {
    private Balance transferAmount;

    private int transferType;
    private int transferStatus;
    private int recipientAccount;
    private int initiatingAccount;
    private int transferId;

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

    public Balance getTransferAmount() {
        return transferAmount;
    }

    public void setTransferAmount(BigDecimal input){
        this.transferAmount.setBalance(input);
    }

    public void setTransferAmount(Balance transferAmount) {
        this.transferAmount = transferAmount;
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
