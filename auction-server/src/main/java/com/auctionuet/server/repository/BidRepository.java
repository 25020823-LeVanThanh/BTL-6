package com.auctionuet.server.repository;
import com.auctionuet.server.model.Bid; // Model Bid
import java.util.List;
import java.util.Optional;
public interface BidRepository {

    Bid save(Bid bid); // Lưu lượt đặt giá mới

    Optional<Bid> findById(String id); // Tìm lượt đặt giá theo id

    List<Bid> findByAuctionId(String auctionId); // Lấy tất cả lượt đặt giá của một phiên đấu giá

    List<Bid> findByBidderId(String bidderId); // Lấy tất cả lượt đặt giá của một người mua (Bidder)

    Optional<Bid> findHighestBidByAuctionId(String auctionId); // Lấy lượt đặt giá cao nhất của phiên đấu giá
}
