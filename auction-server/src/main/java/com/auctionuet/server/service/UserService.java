package com.auctionuet.server.service;
import com.auctionuet.server.model.User;
import java.util.List;
import java.util.Optional;
public interface UserService {
    User createUser(User user);
    Optional<User> getUserById(String id);
    Optional<User> getUserByUsername(String username);
    List<User> getAllUsers();
    User updateUser(User user);
    void deleteUser(String id);
}
