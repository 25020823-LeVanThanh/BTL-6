/*
 * Design Pattern: Observer — Subject
 * - add/remove listener
 * - publishBidPlaced / publishAuctionFinished
 * - BidServiceImpl / AuctionServiceImpl gọi publish sau khi xử lý xong
 */
package com.auctionuet.server.event;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.Bid;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
/*
 * Subject (Observer pattern): giữ danh sách listener, phát sự kiện.
 * CopyOnWriteArrayList: an toàn khi vừa duyệt vừa add/remove.
 */
@Component //Để Spring tạo 1 bean và inject vào BidServiceImpl / AuctionServiceImpl.
public class BidEventPublisher {
    private final List<BidEventListener> listeners= new CopyOnWriteArrayList<>();
    public BidEventPublisher(List<BidEventListener> listeners){
        if(listeners != null){
            this.listeners.addAll(listeners);
        }
    }
    public void addListener(BidEventListener listener) {
        if (listener != null) listeners.add(listener);
    }
    public void removeListener(BidEventListener listener) {
        listeners.remove(listener);
    }
    public void publishBidPlaced(Bid bid) {
        for (BidEventListener l : listeners) {
            l.onBidPlaced(bid);
        }
    }
    public void publishAuctionFinished(Auction auction) {
        for (BidEventListener l : listeners) {
            l.onAuctionFinished(auction);
        }
    }
}
