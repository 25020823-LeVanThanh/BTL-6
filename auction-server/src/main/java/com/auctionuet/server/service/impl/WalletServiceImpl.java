// thật sự đổi số dư; khóa theo user để tránh 2 bid cùng lúc trừ sai tiền.
package com.auctionuet.server.service.impl;

import com.auctionuet.server.service.WalletService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WalletServiceImpl implements WalletService {
    //tại Ram-sau đổi sang gọi repository của người 1
    private final Map<String,Double> balances= new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @Override
    public double getBalance(String userId) {
        requireUser(userId);
        return balances.getOrDefault(userId,0.0);
    }

    @Override
    public void deposit(String userId, double amount) {
        requireUser(userId);
        requirePositive(amount);
        synchronized (lock) {
            balances.put(userId,getBalance(userId)+amount);
        }
    }
    @Override
    public void withdraw(String userId, double amount) {
        requireUser(userId);
        requirePositive(amount);
        synchronized (lock) {
            double bal= getBalance(userId);
            if(bal<amount){
                throw new IllegalStateException("số dư không đủ");
            }
            balances.put(userId,bal-amount);
        }
    }

    @Override
    public void holdDeposit(String userId, String auctionId, double amount) {
        withdraw(userId,amount);
    }

    @Override
    public void refundDeposit(String userId, String auctionId, double amount) {
        deposit(userId,amount);
    }

    @Override
    public void settlePayment(String winnerId, String sellerId, String auctionId, double finalPrice, double depositHeld) {
        requireUser(winnerId);
        requireUser(sellerId);
        requirePositive(finalPrice);
        double remain=finalPrice-depositHeld;
        synchronized (lock) {
            if(remain>0){
                withdraw(winnerId,remain);
            }
            deposit(sellerId,finalPrice);
        }
    }
    private void requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId không hợp lệ");
        }
    }
    private void requirePositive(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount phải lớn hơn 0");
        }
    }
}
