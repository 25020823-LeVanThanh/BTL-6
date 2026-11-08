//Interface: hệ thống làm được gì với phiên (tạo / mở / đóng / hủy / xem)
package com.auctionuet.server.service;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.model.LiveAuction;

import java.util.List;
import java.util.Optional;// tác dụng chính là kiểm tra null tránh sập chương trình

public interface AuctionService {
    Auction create(Auction auction);//tạo phiên (status OPEN)
    void start(Auction auction);// OPEN → RUNNING + bidService.registerLive
    void finish(Auction auction);// RUNNING → FINISHED, chọn winner, unregisterLive
    void cancel(Auction auction);// hủy + unregister nếu đang live
    Optional<Auction> findById(String auctionId);
    List<Auction> listAll();// hiện toàn bộ các phiên để hiện lên tranh chủ
    Optional<LiveAuction> getLive(String auctionId);// Empty nếu k live
}