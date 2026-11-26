package com.auctionuet.server.repository.impl;

import com.auctionuet.server.model.User;
import com.auctionuet.server.repository.UserRepository;

import java.util.List;
import java.util.Optional;

// TODO(Người 1): implement đọc/ghi users.json
public class UserRepositoryImpl implements UserRepository {

	@Override
	public User save(User user) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<User> findById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public Optional<User> findByUsername(String username) {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public List<User> findAll() {
		throw new UnsupportedOperationException("Not implemented yet");
	}

	@Override
	public void deleteById(String id) {
		throw new UnsupportedOperationException("Not implemented yet");
	}
}
