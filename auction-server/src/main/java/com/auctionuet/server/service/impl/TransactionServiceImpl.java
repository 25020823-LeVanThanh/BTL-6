// thực hiện các phương thức xử lý logic nghiệp vụ liên quan đến quản lý giao dịch ra giá (BidTransaction)
package com.auctionuet.server.service.impl;

import com.auctionuet.server.service.TransactionService;
import com.auctionuet.server.model.BidTransaction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

public class TransactionServiceImpl implements TransactionService {
    // tạm RAM — sau gắn repository Người 1
    private static final Set<String> ALLOWED_TYPES=Set.of("DEPOSIT", "WITHDRAW", "HOLD", "REFUND", "PAYMENT");// chỉ cho phép type thuộc 1 trong các kiểu này
    private final List<BidTransaction> store = new CopyOnWriteArrayList<>();//store là chỗ chứa tạm mọi BidTransaction trong RAM

    @Override
    public BidTransaction record(String bidId, String userId, String auctionId, double amount, String type) {
        requireUser(userId);
        requireType(type);
        requirePositive(amount);
        BidTransaction tx= new BidTransaction(UUID.randomUUID().toString(),bidId,userId,auctionId,amount,type,LocalDateTime.now());
        store.add(tx);
        return tx;
    }

    @Override
    public List<BidTransaction> findByUser(String userId) {
        requireUser(userId);
        List<BidTransaction> result = new ArrayList<>();
        for (BidTransaction tx : store) {
            if (userId.equals(tx.getUserId())) {
                result.add(tx);
            }
        }
        return result;
    }

    @Override
    public List<BidTransaction> findByAuctionId(String auctionId) {
        requireAuctionId(auctionId);
        List<BidTransaction> result = new ArrayList<>();
        for (BidTransaction tx : store) {
            if (auctionId.equals(tx.getAuctionId())) {
                result.add(tx);
            }
        }
        return result;
    }

    @Override
    public List<BidTransaction> listAll() {
        return List.copyOf(store);
    }

    private void requireAuctionId(String auctionId) {
        if (auctionId == null || auctionId.isBlank()) {
            throw new IllegalArgumentException("auctionId không hợp lệ");
        }
    }
    private void requireUser(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException("userId không hợp lệ");
        }
    }
    private void requireType(String type) {
        if (type == null || type.isBlank()) {
            throw new IllegalArgumentException("type không hợp lệ");
        }
        if(!ALLOWED_TYPES.contains(type)) {
            throw new IllegalArgumentException("type không được hỗ trợ:" + type);
        }
    }
    private void requirePositive(double amount) {
        if (amount <= 0) {
            throw new IllegalArgumentException("amount phải lớn hơn 0");
        }
    }
}
