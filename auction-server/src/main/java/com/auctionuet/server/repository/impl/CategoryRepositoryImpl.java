package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.Category;
import com.auctionuet.server.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;

// TODO(Người 1): implement đọc/ghi categories.json
public class CategoryRepositoryImpl implements CategoryRepository {

	@Override
	public Category save(Category category) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Category> findById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<Category> findByName(String name) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<Category> findAll() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void deleteById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
