/*  VAI TRÒ FILE:   ProductService.java: Có thể thực hiện những chức năng gì với sản phẩm?
- Định nghĩa các chức năng quản lý sản phẩm
- Các chức năng bao gồm tạo, tìm kiếm theo ID, cập nhật, xóa và lấy danh sách sản phẩm.
 */

package com.auctionuet.server.service;          // Khai báo file ProductService.java thuộc package com.auctionuet.server.service
import com.auctionuet.server.model.Product;     // Import Product để Java biết đang sử dụng Product.java thuộc package com.auctionuet.server.model
import java.util.List;                          // Thư viện về List

public interface ProductService {       // Lớp triển khai ProductService phải cung cấp chức năng được khai báo trong interface này
    /*  4 thao tác CRUD:
    - C - Create: Thêm sản phẩm
    - R - Read: Đọc sản phẩm
    - U - Update: Cập nhật thông tin
    - D - Delete: Xóa sản phẩm
     */
    Product createProduct(Product product);                 // Phương thức giúp tạo sản phẩm mới
    Product getProductById(Long id);                        // Phương thức giúp tìm 1 sản phẩm đã có
    Product updateProduct(Long id, Product product);        // Phương thức giúp sửa thông tin của 1 sản phẩm đã có sẵn
    void deleteProduct(Long id);                     // Phương thức giúp xóa sản phẩm đã có, sử dụng void do ta không cần trả về giá trị nào

    List<Product> getAllProducts();                         // Phương thức giúp lấy danh sách các sản phẩm, trả về các phần tử kiểu Product

}
