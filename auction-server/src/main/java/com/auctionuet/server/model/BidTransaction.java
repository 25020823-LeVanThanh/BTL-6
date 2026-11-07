// quán lý dòng tiền đi đâu, vì lý do gì

package com.auctionuet.server.model;
import java.time.LocalDateTime;

public class BidTransaction{
    private String id;//Mã giao dịc
    private String bidId;//Liên kết tới Bid.id (giao dịch phát sinh từ bid nào; có thể null nếu chỉ nạp/hoàn)
    private String userId;//Ví của ai bị cộng/trừ
    private String auctionId;//Liên quan phiên nào
    private double amount;//Số tiền giao dịch
    private String type;//Loại: ví dụ "DEPOSIT", "REFUND", "PAYMENT"
    private LocalDateTime timestamp;//Lúc giao dịch xảy ra

    public BidTransaction(){}
    public BidTransaction(String id,String bidId,String userId,String auctionId,double amount,String type,LocalDateTime timestamp){
        this.id=id;
        this.bidId=bidId;
        this.userId=userId;
        this.auctionId=auctionId;
        this.amount=amount;
        this.type=type;
        this.timestamp=timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBidId() {
        return bidId;
    }

    public void setBidId(String bidId) {
        this.bidId = bidId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}