// Observer mẫu: in log khi có bid/phiên xong — dùng test; sau thay bằng push socket (Người 4)
package com.auctionuet.server.event;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.Bid;
import org.springframework.stereotype.Component;

@Component
public class ConsoleBidListener implements BidEventListener {
    @Override
    public void onBidPlaced(Bid bid){
        System.out.println("[Observer] bid mới auction=" + bid.getAuctionId()
                + " bidder=" + bid.getBidderId()
                + " amount=" + bid.getAmount());
    }

    @Override
    public void onAuctionFinished(Auction auction) {
        System.out.println("[Observer] phiên xong id=" + auction.getId()
                + " winner=" + auction.getWinnerId()
                + " price=" + auction.getCurrentPrice());
    }
}
