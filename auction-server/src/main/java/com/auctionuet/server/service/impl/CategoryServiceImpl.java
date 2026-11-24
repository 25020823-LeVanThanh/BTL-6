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

import java.util.ArrayList;
import java.util.List;

import com.auctionuet.server.model.Category;
import com.auctionuet.server.service.CategoryService;

public class CategoryServiceImpl implements CategoryService {
    //Tạo một danh sách rỗng để lưu trữ và quản lý các danh mục sản phẩm trong CategoryServiceImpl.java
    private final List<Category> categories = new ArrayList<>();

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
        for(int i = 0; i < categories.size(); i++) {                    // Xét từng phần tử trong categories
            Category existingCategory = categories.get(i);              // Lấy danh mục ở vị trí i và gán cho existingCategory
            if(existingCategory.getId().equals(category.getId())) {     // Nếu ID đã tồn tại từ trước, ném ra ngoại lệ
                throw new IllegalArgumentException("Category ID already existed");
            }
        }
        categories.add(category);       // Thêm đối tượng category vào danh sách lưu trữ.
        return category;                // Trả về tham chiếu đến đối tượng vừa thêm cho nơi gọi phương thức.
    }

    @Override
    public Category getCategoryById(String id) {      // Tìm kiếm Category bằng ID
        if(id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        for(int i = 0; i < categories.size(); i++) {
            Category existingCategoryId = categories.get(i);
            if(existingCategoryId.getId().equals(id)){          // Nếu tìm thấy ID trùng khớp với ID đã có -> Trả về danh mục đó
                return existingCategoryId;
            }
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
        for(int i = 0; i < categories.size(); i++) {
            Category existingCategory = categories.get(i);
            if(existingCategory.getId().equals(id)) {
                categories.set(i, category);
                return category;
            }
        }
        throw new IllegalArgumentException("ID not found");     // Nếu không tìm thấy ID nào trùng thì ném ra ngoại lệ (dùng tạm IllegalArgumentException)
    }

    @Override
    public void deleteCategory(String id) {       // Xóa category
        if(id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        for(int i = 0; i < categories.size(); i++) {
            Category existingCategory = categories.get(i);
            if(existingCategory.getId().equals(id)) {
                categories.remove(i);
                return;
            }
        }
        throw new IllegalArgumentException("ID not found");
    }

    @Override
    public List<Category> getAllCategories() {      // Hiển thị toàn bộ category
        return new ArrayList<>(categories);
    }
}

/* LƯU Ý MỘT SỐ GIỚI HẠN:
   - Danh mục chỉ tồn tại trong RAM. Khi tắt server, dữ liệu sẽ mất.
   - Mỗi đối tượng CategoryServiceImpl mới có một danh sách categories riêng.
   - ArrayList không tự bảo đảm an toàn khi nhiều luồng cùng sửa dữ liệu.
   - getAllCategories() tạo bản sao danh sách, nhưng không sao chép sâu từng đối tượng Category.
*/
