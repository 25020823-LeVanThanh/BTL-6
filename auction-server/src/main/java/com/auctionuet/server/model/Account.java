package com.auctionuet.server.model;
import java.io.Serializable;
public class Account implements Serializable {
    private String username;
    private String passwordHash; // Luôn lưu password dạng hash (SHA-256 / BCrypt)
    private boolean active;

    public Account() {}

    public Account(String username, String passwordHash) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.active = true;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }
}

