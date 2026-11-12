//đại diện cho lượt trả giá

package com.auctionuet.server.model;
import java.time.LocalDateTime;
public class Bid{
    private String id;//mã lần đặt giá, giúp phân biệt các người tham gia, lưu JSON/ lịch sử, giúp tìm để debug/ test unit
    private String auctionId;//mã phiên đấu giá, được tạo ra để đáp ứng yêu cầu trong  BTL (3.1.3 + 3.1.4)
    private String bidderId;//ai đặt
    private double amount;// đặt bao nhiêu, được tạo ra để đáp ứng yêu cầu trong  BTL (3.1.3 + 3.1.5)
    private LocalDateTime timestamp;// đặt lúc nào, được tạo ra để, sx lịch sử,anti_sniping

    public Bid(){}
    public Bid(String id,String auctionId,String bidderId,double amount,LocalDateTime timestamp){
        this.id=id;
        this.auctionId=auctionId;
        this.bidderId=bidderId;
        this.amount=amount;
        this.timestamp=timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidderId() {
        return bidderId;
    }

    public void setBidderId(String bidderId) {
        this.bidderId = bidderId;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

}