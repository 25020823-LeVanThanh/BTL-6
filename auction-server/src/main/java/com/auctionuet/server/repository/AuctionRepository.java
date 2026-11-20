package com.auctionuet.server.repository;
import com.auctionuet.server.model.Auction; // Model Auction
import java.util.List;
import java.util.Optional;
public interface AuctionRepository {

    Auction save(Auction auction); // Lưu mới hoặc cập nhật phiên đấu giá

    Optional<Auction> findById(String id); // Tìm phiên đấu giá theo id

    Optional<Auction> findByProductId(String productId); // Tìm phiên đấu giá theo sản phẩm

    List<Auction> findAll(); // Lấy toàn bộ phiên đấu giá

    void deleteById(String id); // Xóa phiên đấu giá theo id
}
