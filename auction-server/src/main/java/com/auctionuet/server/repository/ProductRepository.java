package com.auctionuet.server.repository;
import com.auctionuet.server.model.Product; // Model Product
import com.auctionuet.server.model.ProductStatus; // Enum ProductStatus
import java.util.List;
import java.util.Optional;
public interface ProductRepository {
    Product save(Product product); // Thêm mới hoặc cập nhật Product

    Optional<Product> findById(String id); // Tìm Product theo id

    List<Product> findAll(); // Lấy tất cả sản phẩm

    List<Product> findBySellerId(String sellerId); // Lấy sản phẩm theo người bán (Seller)

    List<Product> findByStatus(ProductStatus status); // Lọc sản phẩm theo trạng thái duyệt/bán

    void deleteById(String id); // Xóa Product theo id
}
