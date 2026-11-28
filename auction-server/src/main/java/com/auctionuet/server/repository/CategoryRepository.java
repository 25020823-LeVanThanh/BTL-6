package com.auctionuet.server.repository;
import com.auctionuet.server.model.Category; // Enum Category
import java.util.List;
import java.util.Optional;
public interface CategoryRepository {
    Category save(Category category); // Lưu/cập nhật Category

    Optional<Category> findById(String id); // Tìm Category theo id (hoặc mã danh mục)

    Optional<Category> findByName(String name); // Tìm Category theo tên

    List<Category> findAll(); // Lấy toàn bộ danh mục

    void deleteById(String id); // Xóa Category theo id
}
