package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.Product;
import com.auctionuet.server.model.ProductStatus;
import com.auctionuet.server.repository.ProductRepository;

import java.util.List;
import java.util.Optional;

// TODO(Người 1): implement đọc/ghi products.json
public class ProductRepositoryImpl implements ProductRepository {

	@Override
	public Product save(Product product) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Product> findById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Product> findAll() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Product> findBySellerId(String sellerId) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Product> findByStatus(ProductStatus status) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void deleteById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
