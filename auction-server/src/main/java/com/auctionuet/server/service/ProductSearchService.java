/*  VAI TRÒ FILE:
- Định nghĩa các chức năng tìm kiếm và lọc sản phẩm
    Ví dụ, người dùng muốn tìm sản phẩm có tên chứa "iPhone":
- Lọc sản phẩm theo danh mục "Electronics" và có từ khóa iPhone
- Tìm sản phẩm có giá khởi điểm trong một khoảng nhất định.
 */

package com.auctionuet.server.service;
import com.auctionuet.server.model.Product;
import com.auctionuet.server.model.Category;

import java.math.BigDecimal;
import java.util.List;

public interface ProductSearchService {
    // Sử dụng kiểu trả về là List <Product> để khi ta sử dụng phương thức sẽ trả về tất cả các sản phẩm phù hợp.
    List<Product> searchByName(String keyword);
    List<Product> searchByCategory(Category category);     // Trả về List <Product> vì 1 danh mục có thể chứa nhiều sản phẩm
    List<Product> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);
}
