// Observer: hợp đồng listener — nhận sự kiện bid mới / phiên kết thúc
package com.auctionuet.server.event;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.Bid;

// Observer: bất kỳ class nào muốn được báo khi có sự kiện đấu giá thì implements interface này
public interface BidEventListener {
    void onBidPlaced(Bid bid);// có lượt đặt giá mới
    void onAuctionFinished(Auction auction);// phiên đã kết thúc (có/không người thắng)
}
