package com.auctionuet.server.model;

public class Seller extends User {
    private String bankAccountNumber;

    public Seller() {
        this.role = Role.SELLER;
    }

    public Seller(String id, String fullName, String email, Account account, String bankAccountNumber) {
        super(id, fullName, email, Role.SELLER, account);
        this.bankAccountNumber = bankAccountNumber;
    }

    public String getBankAccountNumber() {
        return bankAccountNumber;
    }
    public void setBankAccountNumber(String bankAccountNumber) {
        this.bankAccountNumber = bankAccountNumber;
    }
}
