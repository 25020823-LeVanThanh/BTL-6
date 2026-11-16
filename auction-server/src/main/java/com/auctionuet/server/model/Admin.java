package com.auctionuet.server.model;

public class Admin extends User{
    public Admin() {
        this.role = Role.ADMIN;
    }

    public Admin(String id, String fullName, String email, Account account) {
        super(id, fullName, email, Role.ADMIN, account);
    }
}
