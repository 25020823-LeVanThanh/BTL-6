package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.Bid;
import com.auctionuet.server.repository.BidRepository;

import java.util.List;
import java.util.Optional;

// TODO(Người 1): implement đọc/ghi bids.json
public class BidRepositoryImpl implements BidRepository {

	@Override
	public Bid save(Bid bid) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Bid> findById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Bid> findByAuctionId(String auctionId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Bid> findByBidderId(String bidderId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Bid> findHighestBidByAuctionId(String auctionId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
