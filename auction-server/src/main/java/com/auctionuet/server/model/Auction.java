//thông tin và quản lý về phiên đấu giá

package com.auctionuet.server.model;
import java.time.LocalDateTime;

public class Auction{
    private String id;//Mã phiên (auctionId ở Bid/Transaction trỏ về đây)
    private String productId;//Đấu sản phẩm nào
    private String sellerId;//Ai đăng phiên
    private double startingPrice;//Giá khởi điểm
    private double currentPrice;//Giá cao nhất hiện tại
    private double bidIncrement;//Bước giá tối thiểu
    private String currentLeaderId;//Ai đang dẫn đầu
    private String winnerId;//Ai thắng khi đóng(đang chạy =null)
    private String status;//OPEN / RUNNING / FINISHED / PAID / CANCELED
    private LocalDateTime startTime;// mở
    private LocalDateTime endTime;//đóng

    public Auction(){}
    public Auction(String id,String productId,String sellerId,double startingPrice,double currentPrice,double bidIncrement
            ,String currentLeaderId,String winnerId,String status,LocalDateTime startTime,LocalDateTime endTime){
        this.id = id;
        this.productId = productId;
        this.sellerId = sellerId;
        this.startingPrice = startingPrice;
        this.currentPrice = currentPrice;
        this.bidIncrement = bidIncrement;
        this.currentLeaderId = currentLeaderId;
        this.winnerId = winnerId;
        this.status = status;
        this.startTime = startTime;
        this.endTime = endTime;
    }
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    public String getProductId() {
        return productId;
    }
    public void setProductId(String productId) {
        this.productId = productId;
    }
    public String getSellerId() {
        return sellerId;
    }
    public void setSellerId(String sellerId) {
        this.sellerId = sellerId;
    }
    public double getStartingPrice() {
        return startingPrice;
    }
    public void setStartingPrice(double startingPrice) {
        this.startingPrice = startingPrice;
    }
    public double getCurrentPrice() {
        return currentPrice;
    }
    public void setCurrentPrice(double currentPrice) {
        this.currentPrice = currentPrice;
    }
    public double getBidIncrement() {
        return bidIncrement;
    }
    public void setBidIncrement(double bidIncrement) {
        this.bidIncrement = bidIncrement;
    }
    public String getCurrentLeaderId() {
        return currentLeaderId;
    }
    public void setCurrentLeaderId(String currentLeaderId) {
        this.currentLeaderId = currentLeaderId;
    }
    public String getWinnerId() {
        return winnerId;
    }
    public void setWinnerId(String winnerId) {
        this.winnerId = winnerId;
    }
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public LocalDateTime getStartTime() {
        return startTime;
    }
    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
    public LocalDateTime getEndTime() {
        return endTime;
    }
    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
}