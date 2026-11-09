// khai báo “ví làm được gì”, chưa viết cách làm → BidService chỉ phụ thuộc interface (dễ test, đúng design).
package com.auctionuet.server.service;

public interface WalletService {
    double getBalance(String userId);//Đọc số dư hiện tại của user
    void deposit(String userId, double amount);//Cộng tiền vào ví (nạp tiền).
    void withdraw(String userId, double amount);//Trừ tiền khỏi ví (rút / chi).
    void holdDeposit(String userId,String auctionId, double amount);//Giữ cọc khi bidder lần đầu tham gia phiên
    void refundDeposit(String userId,String auctionId, double amount);//Hoàn cọc về ví khi user không thắng hoặc phiên bị hủy.
    /*
     * Thanh toán khi thắng: cọc đã giữ được tính vào giá
     * trừ thêm (finalPrice - deposit) nếu còn thiếu.
     */
    void settlePayment(String winnerId,String sellerId,String auctionId,double finalPrice,double depositHeld);//Chốt thanh toán khi có người thắng:
}
