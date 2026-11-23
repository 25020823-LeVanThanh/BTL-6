
/*  VAI TRÒ CỦA FILE:
- Tìm và lọc ra các sản phẩm theo nhu cầu
- Lọc sản phẩm theo tên, theo danh mục, theo khoảng giá
 */

package com.auctionuet.server.service.impl;

import com.auctionuet.server.model.Category;
import com.auctionuet.server.model.Product;
import com.auctionuet.server.service.ProductSearchService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/*  THUẬT TOÁN TÌM KIẾM SẢN PHẨM:
1.  Lấy danh sách sản phẩm từ nơi lưu trữ.
2.  Duyệt lần lượt từng sản phẩm.
3.  Lấy tên sản phẩm bằng product.getItem().getName().
4.  Kiểm tra tên có chứa từ khóa không.
5.  Đưa sản phẩm phù hợp vào danh sách kết quả.
 */


public class ProductSearchServiceImpl implements ProductSearchService {
    // Sử dụng final để đảm bảo sau khi constructor gán danh sách, thuộc tính không được gán lại cho danh sách khác
    private final List<Product> products;

    // Constructor
    public ProductSearchServiceImpl (List<Product> products) {
        if (products == null) {
            throw new IllegalArgumentException("Invalid product list");
        }
        else {
            this.products = products;
        }
    }

    // Tìm kiếm sản phẩm theo tên
    @Override
    public List<Product> searchByName(String keyword) {
        if(keyword == null || keyword.isBlank()) {          // Nếu tên sản phẩm không hợp lệ, ném ra ngoại lệ
            throw new IllegalArgumentException("Invalid search keyword");
        }
        List<Product> results = new ArrayList<>();          // Tạo danh sách chứa kết quả có kiểu dữ liệu Product
        for(int i = 0; i < products.size(); i++) {
            Product product = products.get(i);              // Khai báo biến tham chiếu product kiểu Product để tham chiếu đến products[i]
            if(product == null || product.getItem() == null || product.getItem().getName() == null) {
                continue;
            }
            String productName = product.getItem().getName();   // Lấy đối tượng Item của sản phẩm, sau đó lấy tên của Item, lưu vào productName
            // Nếu tên sản phẩm (productName) có chứa từ đang tìm kiếm (keyword), thêm sản phẩm vào danh sách results
            if(productName.toLowerCase().contains(keyword.toLowerCase()) ) {
                results.add(product);
            }
        }
        return results;
    }

    // Tìm kiếm sản phẩm theo danh mục
    @Override
    public List<Product> searchByCategory(Category category) {
        if(category == null || category.getId() == null || category.getId().isBlank()) {
            throw new IllegalArgumentException("Invalid category");
        }
        List<Product> results = new ArrayList<>();
        for(int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if(product == null) {continue;}
            Category productCategory = product.getCategory();
            if(productCategory != null                                         // Sản phẩm phải có danh mục
                    && productCategory.getId() != null                         // Danh mục sản phẩm phải có ID
                    && !productCategory.getId().isBlank()                      // ID danh mục không được là chuỗi rỗng
                    && productCategory.getId().equals(category.getId())){      // 2 danh mục phải có cùng ID
                results.add(product);                                           // Thêm sản phẩm vào danh sách kết quả
            }
            /* Ở đây ta sử dụng toán tử equals để so sánh ID thay vì == là vì biến id (trong file Category.java)
            đang khai báo dưới kiểu dữ liệu String. Toán tử == chỉ kiểm tra xem 2 đối tượng String có tham chiếu đến cùng 1 đối tượng không.
             */
        }
        return results;
    }

    // Tìm kiếm các sản phẩm nằm trong mức giá tùy chọn
    @Override
    public List<Product> searchByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if(minPrice == null || maxPrice == null) {          // Kiểm tra xem giá min và max có hợp lệ không
            throw new IllegalArgumentException("Invalid price range");
        }
        /* Tại sao phải sử dụng toán tử compareTo?
        - Do minPrice và maxPrice đang là kiểu dữ liệu BigDecimal do đó không thể so sánh bằng toán tử <, >, =
        - Nếu:
            + minPrice < maxPrice => minPrice.compareTo(maxPrice) < 0
            + minPrice = maxPrice => minPrice.compareTo(maxPrice) = 0
            + minPrice > maxPrice => minPrice.compareTo(maxPrice) > 0
         - LƯU Ý: Chấp nhận trường hợp max = min
         */
        if(minPrice.compareTo(maxPrice) > 0) {              // Kiểm tra xem giá min có nhỏ hơn giá max không
            throw new IllegalArgumentException("Min price must not be greater than max price");
        }
        if(minPrice.compareTo(BigDecimal.ZERO) < 0 || maxPrice.compareTo(BigDecimal.ZERO) < 0){
            throw new IllegalArgumentException("Price must not be negative");
        }
        List<Product> results = new ArrayList<>();

        for(int i = 0; i < products.size(); i++) {
            Product product = products.get(i);
            if (product == null) {
                continue;
            }
            BigDecimal price = product.getStartingPrice();
            if (price != null                               // Kiểm tra sản phẩm để tránh NullPointerException không
                    && price.compareTo(maxPrice) <= 0           // Tìm các sản phẩm nằm trong khoảng giá
                    && price.compareTo(minPrice) >= 0) {
                results.add(product);                       // Với mỗi lần tìm thấy, thêm sản phẩm vào results
            }
            /**
             Trong trường hợp không tìm thấy sản phẩm nằm trong tầm giá thì results.size() = 0
             =>  Phương thức trả về danh sách rỗng (không phải trả về null)
             */
        }
        return results;
    }
}
