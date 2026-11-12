// thật sự đổi số dư; khóa theo user để tránh 2 bid cùng lúc trừ sai tiền.
package com.auctionuet.server.service.impl;

import com.auctionuet.server.service.TransactionService;
import com.auctionuet.server.service.WalletService;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WalletServiceImpl implements WalletService {
    //tại Ram-sau đổi sang gọi repository của người 1
    private final Map<String,Double> balances= new ConcurrentHashMap<>();// Map chứa số dư
    private final Map<String,Double> holds= new ConcurrentHashMap<>();// Map chứa số tiền đã cọc vào phiên nào
    private final Object lock = new Object();
    private final TransactionService transactionService;

    public WalletServiceImpl(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @Override
    public double getBalance(String userId) {
        requireUser(userId);
        synchronized (lock) {
            return balances.getOrDefault(userId, 0.0);
        }
    }

    @Override
    public void deposit(String userId, double amount) {
        requireUser(userId);
        requirePositive(amount);
        synchronized (lock) {
            addBalance(userId,amount);
        }
        transactionService.record( null,userId,null,amount,"DEPOSIT");
    }
    @Override
    public void withdraw(String userId, double amount) {
        requireUser(userId);
        requirePositive(amount);
        synchronized (lock) {
            subtractBalance(userId, amount);
        }
        transactionService.record( null,userId,null,amount,"WITHDRAW");
    }

    @Override
    public void holdDeposit(String userId, String auctionId, double amount) {
        requireUser(userId);
        requireAuction(auctionId);
        requirePositive(amount);
        String key= holdKey(userId,auctionId);
        synchronized (lock){
            if(holds.containsKey(key)){
                throw new IllegalStateException("Đã giữ cọc cho phiên này");
            }
            subtractBalance(userId, amount);
            holds.put(key,amount);
        }
        transactionService.record( null,userId,auctionId,amount,"HOLD");
    }

    @Override
    public void refundDeposit(String userId, String auctionId, double amount) {
        requireUser(userId);
        requireAuction(auctionId);
        String key= holdKey(userId,auctionId);
        double refunded;
        synchronized (lock){
            Double held=holds.get(key);
            if(held==null){
                throw new IllegalStateException("Không có cọc để hoàn");
            }
            holds.remove(key);
            addBalance(userId,held);
            refunded=held;
        }
        transactionService.record( null,userId,auctionId,refunded,"REFUND");
    }

    @Override
    public void settlePayment(String winnerId, String sellerId, String auctionId, double finalPrice, double depositHeld) {
        requireUser(winnerId);
        requireUser(sellerId);
        requireAuction(auctionId);
        requirePositive(finalPrice);
        if(depositHeld<0){
            throw new IllegalArgumentException("depositHeld không hợp lệ");
        }
        String key= holdKey(winnerId,auctionId);
        double held;
        double refundExtra=0;
        double remainPay=0;
        synchronized (lock){
            held=holds.getOrDefault(key,depositHeld);
            holds.remove(key);
            if(held>finalPrice){
                refundExtra=held-finalPrice;
                addBalance(winnerId,refundExtra);//hoàn dư cọc
            }else if(held<finalPrice){
                remainPay=finalPrice-held;
                subtractBalance(winnerId,remainPay);// trừ phần còn lại
            }
            addBalance(sellerId,finalPrice);
        }
        if(refundExtra>0){
            transactionService.record( null,winnerId,auctionId,refundExtra,"REFUND");
        }
        if(remainPay>0){
            transactionService.record( null,winnerId,auctionId,remainPay,"PAYMENT");
        }
        transactionService.record( null,sellerId,auctionId,finalPrice,"PAYMENT");
    }
    private void addBalance(String userId,double amount){
        balances.put(userId,balances.getOrDefault(userId,0.0)+amount);
    }
    private void subtractBalance(String userId,double amount){
        double bal=balances.getOrDefault(userId,0.0);
        if(bal<amount){
            throw new IllegalStateException("Số dư không đủ");
        }
        balances.put(userId,bal-amount);
    }
    private String holdKey(String userId,String auctionId){
        return userId + "|" + auctionId;
    }
    private void requireAuction(String auctionId){
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("auctionId không hợp lệ");
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