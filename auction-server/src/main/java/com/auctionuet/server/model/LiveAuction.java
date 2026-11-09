// quản lý phiên đấu giá nào đang dược đấu giá (live)

package com.auctionuet.server.model;

import java.util.ArrayList;
import java.util.List;

public class LiveAuction extends Auction {
    private List<Bid> bidHistory;//Danh sách bid hợp lệ trong phiên (realtime / biểu đồ)
    private int totalBids;//Đếm nhanh số lượt bid

    public LiveAuction() {
        super();
        this.bidHistory = new ArrayList<>();
        this.totalBids = 0;
    }
    public LiveAuction(Auction auction) {// khi phiên chuyển OPEN → RUNNING, bọc Auction thành LiveAuction
        super(auction.getId(),
                auction.getProductId(),
                auction.getSellerId(),
                auction.getStartingPrice(),
                auction.getCurrentPrice(),
                auction.getBidIncrement(),
                auction.getCurrentLeaderId(),
                auction.getWinnerId(),
                auction.getStatus(),
                auction.getStartTime(),
                auction.getEndTime());
        this.bidHistory = new ArrayList<>();
        this.totalBids = 0;
        setCurrentPrice(auction.getCurrentPrice());
        setCurrentLeaderId(auction.getCurrentLeaderId());
        setWinnerId(auction.getWinnerId());
        setStatus(auction.getStatus());
    }
    public void addBid(Bid bid){
        bidHistory.add(bid);
        totalBids++;
        setCurrentPrice(bid.getAmount());
        setCurrentLeaderId(bid.getBidderId());
    }

    public List<Bid> getBidHistory() {
        return bidHistory;
    }

    public void setBidHistory(List<Bid> bidHistory) {
        this.bidHistory = bidHistory;
    }

    public int getTotalBids() {
        return totalBids;
    }

    public void setTotalBids(int totalBids) {
        this.totalBids = totalBids;
    }
}