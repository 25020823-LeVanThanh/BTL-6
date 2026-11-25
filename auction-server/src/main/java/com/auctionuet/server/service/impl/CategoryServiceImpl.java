/*  VAI TRÒ FILE:
- Phân biệt các file Category:
    + Category.java: Mô tả thông tin của một danh mục, gồm id, name, description.
    + CategoryService.java: Quy định các chức năng quản lý danh mục.
    + CategoryServiceImpl.java: Nơi viết code thực hiện những chức năng đó.
- File CategoryServiceImpl.java là lớp triển khai interface CategoryService,
  có nhiệm vụ xử lý nghiệp vụ quản lý danh mục sản phẩm trong hệ thống đấu giá.
- Lớp này thực hiện các chức năng tạo, tìm kiếm, cập nhật, xóa và lấy danh sách danh mục.
 */

package com.auctionuet.server.service.impl;

import java.util.List;
import java.util.Optional;

import com.auctionuet.server.model.Category;
import com.auctionuet.server.service.CategoryService;
import com.auctionuet.server.repository.CategoryRepository;

public class CategoryServiceImpl implements CategoryService {
    // Repository phụ trách truy cập và lưu trữ dữ liệu danh mục
    private final CategoryRepository categoryRepository;

    // Constructor
    public CategoryServiceImpl(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Override
    public Category createCategory(Category category) {
        if(category == null) {
            throw new IllegalArgumentException("Invalid category");
        }
        if(category.getName() == null || category.getName().isBlank()) {
            throw new IllegalArgumentException("Invalid category name");
        }
        if(category.getId() == null || category.getId().isBlank()) {              // Kiểm tra danh mục có ID hợp lệ hay không
            throw new IllegalArgumentException("Invalid category ID");
        }

        Optional<Category> existingCategory = categoryRepository.findById(category.getId());    // Tìm danh mục theo ID trong Respository
        if(existingCategory.isPresent()) {                                        // Nếu ID đã tồn tại từ trước, ném ra ngoại lệ
            throw new IllegalArgumentException("Category ID already existed");
        }

        return categoryRepository.save(category);

        /*  Khi gọi save(category):
        1. Service gửi danh mục cần lưu cho Repository.
        2. Repository thực hiện thao tác lưu theo cách được triển khai.
        3. Repository trả về một đối tượng Category.
        4. Từ khóa return chuyển đối tượng đó về nơi đã gọi createCategory().
         */
    }

    @Override
    public Category getCategoryById(String id) {      // Tìm kiếm Category bằng ID
        if(id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        Optional<Category> existingCategory = categoryRepository.findById(id);
        if(existingCategory.isPresent()) {                                // Nếu tìm thấy danh mục theo ID thì trả về danh mục đó
            return existingCategory.get();
        }
        throw new IllegalArgumentException("ID not found");     // Nếu không tìm thấy ID nào trùng thì ném ra ngoại lệ (dùng tạm IllegalArgumentException)
    }

    @Override
    public Category updateCategory(String id, Category category) {    // Cập nhật Category
        if(id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        if(category == null) {
            throw new IllegalArgumentException("Invalid category");
        }
        if(category.getName() == null || category.getName().isBlank()){
            throw new IllegalArgumentException("Invalid category name");
        }
        if(!id.equals(category.getId())) {
            throw new IllegalArgumentException("Different ID");
        }
        getCategoryById(id);    // Kiểm tra xem danh mục cần cập nhật có tồn tại không. Nếu không tồn tại, phương thức sẽ ném ngoại lệ và dừng việc cập nhật.
        return categoryRepository.save(category);   // Yêu cầu Repository lưu danh mục mới và trả kết quả về nơi gọi updateCategory().
    }

    @Override
    public void deleteCategory(String id) {       // Xóa category
        if(id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        getCategoryById(id);
        categoryRepository.deleteById(id);
    }

    @Override
    public List<Category> getAllCategories() {      // Hiển thị toàn bộ category
        return categoryRepository.findAll();
    }
}

/* LƯU Ý:
 * - CategoryServiceImpl xử lý nghiệp vụ quản lý danh mục.
 * - Dữ liệu được truy cập thông qua CategoryRepository.
 * - Việc lưu trữ bằng RAM, JSON hay Database phụ thuộc
 *   vào lớp triển khai CategoryRepository.
 * - Các chức năng lưu trữ thực tế cần được kiểm thử
 *   sau khi Repository được triển khai.
 */