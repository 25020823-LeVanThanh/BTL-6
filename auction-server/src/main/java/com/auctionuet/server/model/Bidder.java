package com.auctionuet.server.model;

public class Bidder extends User {
    private String shippingAddress;

    public Bidder() {
        this.role = Role.BIDDER;
    }

    public Bidder(String id, String fullName, String email, Account account, String shippingAddress) {
        super(id, fullName, email, Role.BIDDER, account);
        this.shippingAddress = shippingAddress;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }
    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }
}
