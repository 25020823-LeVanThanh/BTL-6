package com.auctionuet.server.network.controller;

import com.auctionuet.server.exception.AuctionClosedException;
import com.auctionuet.server.exception.InvalidBidException;
import com.auctionuet.server.model.Bid;
import com.auctionuet.server.service.BidService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auctions/{auctionId}/bids")
public class BidController {
    private final BidService bidService;
    public BidController(BidService bidService){
        this.bidService=bidService;
    }
    public record PlaceBidRequest(String bidderId,double amount){}
    @PostMapping
    public ResponseEntity<?> placeBid(@PathVariable String auctionId,@RequestBody PlaceBidRequest req){
        try {
            Bid bid = bidService.placeBid(auctionId,req.bidderId(), req.amount());
            return ResponseEntity.status(HttpStatus.CREATED).body(bid);
        }catch (AuctionClosedException e){
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error",e.getMessage()));
        }catch (InvalidBidException e){
            return ResponseEntity.badRequest().body(Map.of("error",e.getMessage()));
        }
    }
    @GetMapping
    public List<Bid> getHistory(@PathVariable String auctionId){
        return bidService.getBidHistory(auctionId);
    }
}
