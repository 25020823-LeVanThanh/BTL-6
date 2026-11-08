//Cách làm thật: đổi status, gọi BidService.registerLive / unregisterLive, chọn winner
package com.auctionuet.server.service.impl;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.LiveAuction;
import com.auctionuet.server.service.AuctionService;
import com.auctionuet.server.service.BidService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class AuctionServiceImpl implements AuctionService {
    private final Map<String, Auction> store=new ConcurrentHashMap<>();
    private final Map<String, LiveAuction> liveMap=new ConcurrentHashMap<>();
    private final BidService  bidService;
    private final Object lock=new Object();
    public  AuctionServiceImpl(BidService bidService){
        this.bidService=bidService;
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

            stored.setWinnerId(winner);
            stored.setCurrentLeaderId(winner);
            stored.setCurrentPrice(finalPrice);
            stored.setStatus("FINISHED");

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
            stored.setStatus("CANCELED");
            if(liveMap.containsKey(stored.getId())){
                bidService.unregisterLive(stored.getId());
                liveMap.remove(stored.getId());
            }
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
