//"Hệ thống có thể làm được những gì" liên quan đến việc trả giá mà không cần quan tâm bên trong xử lý ra sao".
package com.auctionuet.server.service;

import com.auctionuet.server.exception.AuctionClosedException;
import com.auctionuet.server.exception.InvalidBidException;
import com.auctionuet.server.model.Bid;
import com.auctionuet.server.model.LiveAuction;
import java.util.List;

public interface BidService {
    Bid placeBid(String auctionId,String bidderId, double amount) throws AuctionClosedException, InvalidBidException;//Nhận yêu cầu đặt mức giá mới từ người dùng cho một phiên đấu giá cụ thể

    List<Bid> getBidHistory(String auctionId);//Truy xuất danh sách toàn bộ lịch sử các lượt trả giá của phiên đó để hiển thị lên màn hình.

    void registerLive(LiveAuction auction);//Đưa một phiên đấu giá vừa bắt đầu (start) vào danh sách đang hoạt động trên bộ nhớ (RAM) để sẵn sàng nhận lượt trả giá.

    void unregisterLive(String auctionId);//Gỡ phiên đấu giá khỏi danh sách đang chạy khi phiên kết thúc (finish) hoặc bị hủy (cancel).
}
