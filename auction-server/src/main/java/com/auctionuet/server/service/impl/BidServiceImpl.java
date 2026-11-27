//giải quyết các bài toán kỹ thuật cốt lõi của một hệ thống đấu giá thời gian thực
package com.auctionuet.server.service.impl;

import com.auctionuet.server.exception.AuctionClosedException;
import com.auctionuet.server.exception.InvalidBidException;
import com.auctionuet.server.model.Bid;
import com.auctionuet.server.model.LiveAuction;
import com.auctionuet.server.service.AuctionService;
import com.auctionuet.server.service.BidService;
import com.auctionuet.server.service.WalletService;
import com.auctionuet.server.event.BidEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.context.annotation.Lazy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BidServiceImpl implements BidService {
    private final Map<String,LiveAuction> live=new ConcurrentHashMap<>();
    private final Object lock=new Object();
    private final WalletService walletService;
    private final AuctionService auctionService;
    private final BidEventPublisher publisher;

    public BidServiceImpl(WalletService walletService, @Lazy AuctionService auctionService,BidEventPublisher publisher) {
        this.walletService = walletService;
        this.auctionService = auctionService;
        this.publisher=publisher;
    }

    @Override
    public Bid placeBid(String auctionId,String bidderId, double amount) throws AuctionClosedException, InvalidBidException {
        Bid bid;
        LiveAuction auction;
        synchronized (lock){
            auction=requireRunning(auctionId);
            validateBidder(auction, bidderId);
            validateAmount(auction, amount);
            ensureDeposit(auction, bidderId);
            bid = acceptBid(auction, bidderId, amount);
        }
        applyAntiSniping(auction);// bỏ ra ngoài tránh tình trạng deadlock
        publisher.publishBidPlaced(bid);//Observer
        return bid;
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
    // kiểm tra tiền cọc
    private double resolveDeposit(LiveAuction auction) {
        double deposit = auction.getDepositAmount();
        if (deposit <= 0) {
            throw new IllegalStateException("Phiên chưa cấu hình tiền cọc");
        }
        return deposit;
    }
    // kiểm tra phiên
    private LiveAuction requireRunning(String auctionId)throws AuctionClosedException{
        LiveAuction auction = live.get(auctionId);
        if(auction==null || !"RUNNING".equals(auction.getStatus())){
            throw new AuctionClosedException("Phiên đã đóng hoặc không hợp lệ");
        }
        if(auction.getEndTime() !=null && !LocalDateTime.now().isBefore(auction.getEndTime())){
            throw new AuctionClosedException("Phiên đã hết thời gian.");
        }
        return auction;
    }
    //Kiểm tra tư cách người đặt giá
    private void validateBidder(LiveAuction auction,String bidderId) throws InvalidBidException{
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

    /** Bid trong cửa sổ cuối → cộng extension vào endTime (khớp client 60/120). */
    private void applyAntiSniping(LiveAuction auction){
        int window=auction.getAntiSnipingWindowSeconds();
        int extension=auction.getAntiSnipingExtensionSeconds();
        if (window <= 0 || extension <= 0 || auction.getEndTime() == null) {
            return; // tắt anti-sniping
        }
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime end = auction.getEndTime();
        // còn lại <= window giây?
        if(now.isBefore(end.minusSeconds(window))){
            return; // còn sớm, chưa vào cửa sổ
        }
        LocalDateTime newEnd =end.plusSeconds(extension);
        auction.setEndTime(newEnd);
        auctionService.updateEndTime(auction.getId(),newEnd);
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
