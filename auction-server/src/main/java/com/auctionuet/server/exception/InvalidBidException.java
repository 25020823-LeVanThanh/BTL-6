//Dùng khi: giá thấp hơn min, bidder không hợp lệ, seller tự bid.
package com.auctionuet.server.exception;

public class InvalidBidException extends Exception {
    public InvalidBidException(String message) {
        super(message);
    }
}
