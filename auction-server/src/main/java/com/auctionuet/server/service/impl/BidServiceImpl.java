//giải quyết các bài toán kỹ thuật cốt lõi của một hệ thống đấu giá thời gian thực
package com.auctionuet.server.service.impl;

import com.auctionuet.server.exception.AuctionClosedException;
import com.auctionuet.server.exception.InvalidBidException;
import com.auctionuet.server.model.Bid;
import com.auctionuet.server.model.LiveAuction;
import com.auctionuet.server.service.BidService;
import com.auctionuet.server.service.WalletService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BidServiceImpl implements BidService {
    private final Map<String,LiveAuction> live=new ConcurrentHashMap<>();
    private final Object lock=new Object();
    private final WalletService walletService;
    public BidServiceImpl(WalletService walletService) {
        this.walletService = walletService;
    }

    @Override
    public Bid placeBid(String auctionId,String bidderId, double amount) throws AuctionClosedException, InvalidBidException {
        synchronized (lock){
            return doPlaceBid( auctionId, bidderId, amount);
        }
    }
    private Bid doPlaceBid(String auctionId, String bidderId, double amount) throws AuctionClosedException, InvalidBidException {
        LiveAuction auction = requireRunning(auctionId);//kiểm tra phiên mang Id này(auctionId) có đang chạy không;
        valitdateBidder(auction,bidderId);//kiểm tra người đặt mang id này(bidderId)
        validateAmount(auction, amount);//kiểm tra số tiền khi đặt vào
        ensureDeposit(auction,bidderId);//giữ cọc trước khi nhận bid
        return acceptBid(auction,bidderId,amount);
    }
    //Thực hiện nghiệp vụ phong tỏa tiền cọc và xử lý các tình huống ngoại lệ từ ví
    private void ensureDeposit(LiveAuction auction,String bidderId) throws InvalidBidException {
        double deposit=resolveDeposit(auction);
        try{
            walletService.holdDeposit(bidderId,auction.getId(),deposit);
        }catch (IllegalStateException e){
            if("Đã giữ cọc cho phiên này".equals(e.getMessage())){
                return; // đã cọc rồi → OK
            }
            throw new InvalidBidException(e.getMessage());// ví dụ: số dư không đủ
        }
    }

    //Tạm: 10% startingPrice, tối thiểu 1. Sau thay bằng auction.getDepositAmount()
    private double resolveDeposit(LiveAuction auction){
        double deposit=auction.getStartingPrice()*0.1;
        return deposit<1 ? 1:deposit;
    }
    // kiểm tra phiên
    private LiveAuction requireRunning(String auctionId)throws AuctionClosedException{
        LiveAuction auction = live.get(auctionId);
        if(auction==null || !"RUNNING".equals(auction.getStatus())){
            throw new AuctionClosedException("Phiên đã đóng hoặc không hợp lệ");
        }
        return auction;
    }
    //Kiểm tra tư cách người đặt giá
    private void valitdateBidder(LiveAuction auction,String bidderId) throws InvalidBidException{
        if(bidderId==null || bidderId.isBlank()){
            throw new InvalidBidException("bidderId không hợp lệ");
        }
        if(bidderId.equals(auction.getSellerId())){
            throw new InvalidBidException("Seller không được tự đặt giá");
        }
    }
    //Tính toán và kiểm tra mức giá tối thiểu
    private void validateAmount(LiveAuction auction,double amount) throws InvalidBidException{
        double min= minAllowed(auction);
        if(amount<min){
            throw new InvalidBidException("Giá phải >= " + min);
        }
    }
    private double minAllowed(LiveAuction auction){
        if(auction.getTotalBids()==0){
            return auction.getStartingPrice();// nếu chưa có ai đặt giá thì lấy giá min bằng giá khởi điểm
        }
        return auction.getCurrentPrice()+ auction.getBidIncrement();
    }
    private Bid acceptBid(LiveAuction auction,String bidderId,double amount) throws AuctionClosedException,InvalidBidException{
        Bid bid= new Bid(UUID.randomUUID().toString(),auction.getId(),bidderId,amount,LocalDateTime.now());
        auction.addBid(bid);
        return bid;
    }
    @Override
    public List<Bid> getBidHistory(String auctionId){
        synchronized (lock) {
            LiveAuction auction = live.get(auctionId);
            if (auction == null) {
                return List.of();//nếu phiên đấu giá này đang không LIVE hoặc k tồn tại thì trả về rỗng
            }
            return List.copyOf(auction.getBidHistory());
        }
    }
    @Override
    public void registerLive(LiveAuction auction){
        if(auction==null||auction.getId()==null||auction.getId().isBlank()){
            throw new IllegalArgumentException("auction không hợp lệ");
        }
        synchronized (lock) {
            live.put(auction.getId(), auction);
        }
    }
    @Override
    public void unregisterLive(String auctionId){
        synchronized (lock) {
            live.remove(auctionId);
        }
    }
}
