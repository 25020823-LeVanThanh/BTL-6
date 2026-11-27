/*
 * Design Pattern: Singleton (AuctionManager)
 * - private constructor + getInstance() → chỉ 1 object
 * - Giữ store (mọi phiên) và liveMap (phiên RUNNING)
 * - AuctionServiceImpl gọi AuctionManager.getInstance() thay vì tự giữ Map
 */
package com.auctionuet.server.manager;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.LiveAuction;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class AuctionManager {
    private static final AuctionManager INSTANCE= new AuctionManager();

    private final Map<String,Auction> store=new ConcurrentHashMap<>();
    private final Map<String,LiveAuction> liveMap= new ConcurrentHashMap<>();
    private final Object lock=new Object();

    private AuctionManager(){}
    public static AuctionManager getInstance(){
        return INSTANCE;
    }
    public Object getLock(){return lock;}
    public Map<String, Auction> getStore() { return store; }
    public Map<String, LiveAuction> getLiveMap() { return liveMap; }

    public Optional<Auction> findById(String id){
        return Optional.ofNullable(store.get(id));
    }

    public Collection<Auction> listAll(){
        return store.values();
    }

}
