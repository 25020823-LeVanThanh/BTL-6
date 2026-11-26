package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.Auction;
import com.auctionuet.server.repository.AuctionRepository;

import java.util.List;
import java.util.Optional;

// TODO(Người 1): implement đọc/ghi auctions.json
public class AuctionRepositoryImpl implements AuctionRepository {

	@Override
	public Auction save(Auction auction) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Auction> findById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Auction> findByProductId(String productId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Auction> findAll() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void deleteById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
