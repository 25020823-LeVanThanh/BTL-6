/*  VAI TRÒ FILE:
- Phân biệt các file Category:
    + Category.java: Mô tả thông tin của một danh mục, gồm id, name, description.
    + CategoryService.java: Quy định các chức năng quản lý danh mục.
    + CategoryServiceImpl.java: Nơi viết code thực hiện những chức năng đó.
 */

package com.auctionuet.server.service;

import java.util.List;
import com.auctionuet.server.model.Category;

public interface CategoryService {
    Category createCategory(Category category);             // Tạo phương thức createCategory
    Category getCategoryById(Long id);                      // Tìm kiếm Category
    Category updateCategory(Long id, Category category);    // Phương thức cập nhật Category (Long id là sản phẩm cần cập nhật, Category category là thông tin cập nhật)
    void deleteCategory(Long id);                           // Xóa Category đã có sẵn
    List<Category> getAllCategories();                      // Trả về toàn bộ danh mục

}
