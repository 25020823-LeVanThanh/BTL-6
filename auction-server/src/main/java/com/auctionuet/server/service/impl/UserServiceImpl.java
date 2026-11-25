package com.auctionuet.server.service.impl;
import com.auctionuet.server.model.User;
import com.auctionuet.server.repository.UserRepository;
import com.auctionuet.server.service.UserService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User createUser(User user) {
        if (user == null || user.getAccount() == null) {
            throw new IllegalArgumentException("Thông tin người dùng hoặc tài khoản không hợp lệ");
        }
        if (userRepository.findByUsername(user.getAccount().getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
        }
        return userRepository.save(user);
    }

    public Optional<User> getUserById(String id) {
        return userRepository.findById(id);
    }

    public Optional<User> getUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("Khong duoc de trong ID nguoi dung!");
        }
        if (userRepository.findById(user.getId()).isEmpty()) {
            throw new IllegalArgumentException("Khong tim thay nguoi dung: " + user.getId());
        }
        return userRepository.save(user);
    }

    public void deleteUser(String id) {
        userRepository.deleteById(id);
    }

}
