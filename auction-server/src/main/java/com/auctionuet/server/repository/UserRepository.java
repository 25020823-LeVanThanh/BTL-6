package com.auctionuet.server.repository;
import com.auctionuet.server.model.User; // Model User
import java.util.List;
import java.util.Optional;
public interface UserRepository {
    User save(User user); // Thêm mới hoặc cập nhật User (nếu trùng id thì ghi đè)
    Optional<User> findById(String id); // Tìm User theo id
    Optional<User> findByUsername(String username); // Tìm User theo username trong Account[cite: 3, 4, 5]
    List<User> findAll(); // Lấy toàn bộ danh sách User
    void deleteById(String id); // Xóa User theo id
}
