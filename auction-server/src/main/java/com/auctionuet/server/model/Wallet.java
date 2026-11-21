package com.auctionuet.server.model;
import java.io.Serializable;
public class Wallet implements Serializable{
    private String walletId;
    private double balance;

    public Wallet() {
        this.balance = 0.0;
    }

    public Wallet(String walletId, double balance) {
        this.walletId = walletId;
        this.balance = balance;
    }

    public String getWalletId() {
        return walletId;
    }
    public void setWalletId(String walletId) {
        this.walletId = walletId;
    }

    public double getBalance() {
        return balance;
    }
    public void setBalance(double balance) {
        this.balance = balance;
    }
}
