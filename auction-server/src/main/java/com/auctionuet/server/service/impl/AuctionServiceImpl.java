//Cách làm thật: đổi status, gọi BidService.registerLive / unregisterLive, chọn winner
package com.auctionuet.server.service.impl;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.Bid;
import com.auctionuet.server.model.LiveAuction;
import com.auctionuet.server.service.AuctionService;
import com.auctionuet.server.service.BidService;
import com.auctionuet.server.service.WalletService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service //thêm annotation @Service lên các class *Impl đã có, để Spring tạo bean và Timer chạy được.

public class AuctionServiceImpl implements AuctionService {
    private final Map<String, Auction> store=new ConcurrentHashMap<>();
    private final Map<String, LiveAuction> liveMap=new ConcurrentHashMap<>();
    private final BidService  bidService;
    private final WalletService walletService;
    private final Object lock=new Object();
    public  AuctionServiceImpl(BidService bidService,WalletService walletService){
        this.bidService=bidService;
        this.walletService= walletService;
    }

    @Override
    public Auction create(Auction auction) {
        validateCreate(auction);
        if(auction.getId()==null|| auction.getId().isBlank()){
            auction.setId(UUID.randomUUID().toString());
        }
        auction.setStatus("OPEN");
        auction.setCurrentPrice(auction.getStartingPrice());
        auction.setCurrentLeaderId(null);
        auction.setWinnerId(null);
        if (store.containsKey(auction.getId())) {
            throw new IllegalArgumentException("auctionId đã tồn tại: " + auction.getId());
        }
        store.put(auction.getId(),auction);
        return auction;
    }
    private void validateCreate(Auction auction){
        if(auction==null) throw new IllegalArgumentException("auction không tồn tại");
        if(auction.getStartingPrice()<=0) throw new IllegalArgumentException("giá khởi điểm của phiên(auction) đang nhỏ hơn hoặc bằng 0");
        if(auction.getBidIncrement()<=0) throw new IllegalArgumentException("bước giá đang nhỏ hơn hoặc bằng 0");
        if(auction.getStartTime()==null|| auction.getEndTime()==null)
            throw new IllegalArgumentException("Start/End time không hợp lệ");
        if(!auction.getStartTime().isBefore(auction.getEndTime()))
            throw new IllegalArgumentException("Start time phải trước End Time");
    }
    @Override
    public void start(Auction auction){
        requireAuctionArg(auction);
        synchronized (lock) {
            Auction stored= requireStore(auction.getId());
            if(!"OPEN".equals(stored.getStatus())){
                throw new IllegalStateException("chỉ Start được phiên đang OPEN");
            }
            stored.setStatus("RUNNING");
            LiveAuction live=new LiveAuction(stored);
            live.setStatus("RUNNING");
            liveMap.put(live.getId(),live);
            bidService.registerLive(live);// cùng objact với liveMap
        }
    }
    @Override
    public void finish(Auction auction){
        requireAuctionArg(auction);
        synchronized (lock) {
            Auction stored= requireStore(auction.getId());
            if(!"RUNNING".equals(stored.getStatus())){
                throw new IllegalStateException("chỉ finish được phiên RUNNING");
            }
            LiveAuction live= liveMap.get(stored.getId());
            String winner= (live!=null)? live.getCurrentLeaderId() : null;
            double finalPrice=(live!=null)? live.getCurrentPrice(): stored.getCurrentPrice();
            List<Bid> bids= bidService.getBidHistory(stored.getId());

            stored.setWinnerId(winner);
            stored.setCurrentLeaderId(winner);
            stored.setCurrentPrice(finalPrice);
            stored.setStatus("FINISHED");
            refundAll(stored.getId(),bids,winner);
            if(winner!=null){
                settleWinner(stored,winner,finalPrice);
            }

            bidService.unregisterLive(stored.getId());
            liveMap.remove(stored.getId());
        }
    }
    @Override
    public void cancel(Auction auction){
        requireAuctionArg(auction);
        synchronized (lock) {
            Auction stored= requireStore(auction.getId());
            if("FINISHED".equals(stored.getStatus())||"PAID".equals(stored.getStatus())){
                throw new IllegalStateException("không cancel được phiên đã kết thúc");
            }
            List<Bid> bids=bidService.getBidHistory(stored.getId());
            stored.setStatus("CANCELED");
            refundAll(stored.getId(),bids,null);
            if(liveMap.containsKey(stored.getId())){
                bidService.unregisterLive(stored.getId());
                liveMap.remove(stored.getId());
            }
        }
    }
    //ai đã bid thì chắc chắn đã bị giữ cọc
    private Set<String> bidderIds(List<Bid> bids) {
        Set<String> ids= new HashSet<>();
        for(Bid b:bids) ids.add(b.getBidderId());
        return ids;
    }
    //hoàn cọc cho mọi bidder của phiên, trừ exceptUserId(người thắng; null nếu hủy phiên)
    private void refundAll(String auctionId,List<Bid> bids,String exceptUserId){
        for(String userId:bidderIds(bids)){
            if(userId.equals(exceptUserId)) continue;
            try{
                walletService.refundDeposit(userId,auctionId,0);
            } catch (RuntimeException e) {
                System.out.println("[Auction] hoàn cọc lỗi " + userId + ": " + e.getMessage());
            }
        }
    }
    //thanh toán cho người thắng: cọc được tính vào giá, trừ phần thiếu,seller nhận finalprice
    private void settleWinner(Auction stored, String winner, double finalPrice){
        try{
            walletService.settlePayment(winner,stored.getSellerId(),stored.getId(),finalPrice,0);// 0: impl lấy cọc thật trong holds
            stored.setStatus("PAID");
        } catch (RuntimeException e) {
            System.out.println("[Auction] thanh toán lỗi " + stored.getId() + ": " + e.getMessage());
        }
    }
    private void requireAuctionArg(Auction auction){
        if(auction==null||auction.getId()==null||auction.getId().isBlank()){
            throw new IllegalArgumentException("auction/id không hợp lệ");
        }
    }
    private Auction requireStore(String auctionId){
        Auction a=store.get(auctionId);
        if(a==null) throw new IllegalArgumentException("Không tìm thấy phiên: " + auctionId);
        return a;
    }
    @Override
    public Optional<Auction> findById(String auctionId){
        return Optional.ofNullable(store.get(auctionId));
    }
    @Override
    public List<Auction> listAll(){
        return new ArrayList<>(store.values());
    }
    @Override
    public Optional<LiveAuction> getLive(String auctionId){
        return Optional.ofNullable(liveMap.get(auctionId));
    }
}
