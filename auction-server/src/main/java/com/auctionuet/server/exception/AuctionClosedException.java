//Báo lỗi khi người dùng cố gắng tương tác với một phiên đấu giá hiện không cho phép đặt giá

package com.auctionuet.server.exception;

public class AuctionClosedException extends Exception {
    public AuctionClosedException(String message) {
        super(message);
    }
}
