
/*  VAI TRÒ CỦA FILE: ProductServiceImpl.java: Thực hiện các chức năng đó như thế nào?
- Viết cách các phương thức trong file ProductService.java hoạt động.
- Quy trình:
    + ProductController: Nhận yêu cầu tạo, sửa, tìm sản phẩm
    + ProductServiceImpl: Kiểm tra dữ liệu và xử lý quy tắc nghiệp vụ
    + ProductRepository: Truy xuất và lưu trữ sản phẩm
 */
package com.auctionuet.server.service.impl;
import com.auctionuet.server.model.Product;
import com.auctionuet.server.service.ProductService;

import java.util.List;
import java.math.BigDecimal;

public class ProductServiceImpl implements ProductService {
    @Override
    public Product createProduct(Product product) {
        if(product == null) {
            throw new IllegalArgumentException("Invalid product");
        }
        if(product.getItem() == null || product.getItem().getName() == null || product.getItem().getName().isBlank()) {
            throw new IllegalArgumentException("Invalid item");
        }
        if(product.getStartingPrice() == null || product.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid starting price");
        }
        if(product.getCategory() == null) {
            throw new IllegalArgumentException("Invalid category");
        }
        if(product.getCategory().getId() == null || product.getCategory().getId().isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        if(product.getSeller() == null) {
            throw new IllegalArgumentException("Invalid seller");
        }
        if(product.getStatus() == null) {
            throw new IllegalArgumentException("Invalid status");
        }
        return null;
    }

    @Override
    public Product getProductById(String id) {
        return null;
    }

    @Override
    public Product updateProduct(String id, Product product) {
        return null;
    }

    @Override
    public void deleteProduct(String id) {

    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }
}
