package com.auctionuet.server.model;
import java.io.Serializable;
public class User implements Serializable {
    protected String id;
    protected String fullName;
    protected String email;
    protected Role role;
    protected Account account;
    protected Wallet wallet;

    public User() {}

    public User(String id, String fullName, String email, Role role, Account account) {
        this.id = id;
        this.fullName = fullName;
        this.email = email;
        this.role = role;
        this.account = account;
        this.wallet = new Wallet("W_" + id, 0.0);
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }
    public void setRole(Role role) {
        this.role = role;
    }

    public Account getAccount() {
        return account;
    }
    public void setAccount(Account account) {
        this.account = account;
    }

    public Wallet getWallet() {
        return wallet;
    }
    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

}
