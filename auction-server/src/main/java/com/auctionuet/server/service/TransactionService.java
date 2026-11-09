//định nghĩa các phương thức xử lý logic nghiệp vụ liên quan đến quản lý giao dịch ra giá (BidTransaction)
package com.auctionuet.server.service;

import com.auctionuet.server.model.BidTransaction;
import java.util.List;

public interface TransactionService {
    BidTransaction record(String bidId,String userId,String auctionId,double amount,String type);//Ghi nhận và tạo mới một lịch sử giao dịch ra giá vào hệ thống.
    List<BidTransaction> findByUser(String userId);//Tìm kiếm và lấy ra lịch sử tất cả giao dịch của một người dùng cụ thể.
    List<BidTransaction> findByAuctionId(String auctionId);//Lấy danh sách tất cả lượt ra giá thuộc về một phiên đấu giá cụ thể.
    List<BidTransaction> listAll();//Trả về danh sách toàn bộ giao dịch đấu giá hiện có trên hệ thống.


}
