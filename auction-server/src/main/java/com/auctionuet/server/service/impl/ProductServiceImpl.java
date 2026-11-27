/*  VAI TRÒ CỦA FILE: ProductServiceImpl.java: Thực hiện các chức năng đó như thế nào?
- Viết cách các phương thức trong file ProductService.java hoạt động.
- Quy trình:
   + ProductController: Nhận yêu cầu tạo, sửa, tìm sản phẩm
   + ProductServiceImpl: Kiểm tra dữ liệu và xử lý quy tắc nghiệp vụ
   + ProductRepository: Truy xuất và lưu trữ sản phẩm
 */

package com.auctionuet.server.service.impl;

import org.springframework.stereotype.Service;

import com.auctionuet.server.model.Product;
import com.auctionuet.server.service.ProductService;
import com.auctionuet.server.repository.ProductRepository;
import com.auctionuet.server.exception.ProductNotFoundException;
import com.auctionuet.server.model.ProductStatus;

import java.util.List;
import java.math.BigDecimal;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {        // Constructor
        this.productRepository = productRepository;
    }

    @Override
    public Product createProduct(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Invalid product");
        }
        if (product.getItem() == null ||
                product.getItem().getName() == null ||
                product.getItem().getName().isBlank()) {
            throw new IllegalArgumentException("Invalid item");
        }
        if (product.getStartingPrice() == null ||
                product.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid starting price");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Invalid category");
        }
        if (product.getCategory().getId() == null ||
                product.getCategory().getId().isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        if (product.getSeller() == null) {
            throw new IllegalArgumentException("Invalid seller");
        }
        product.setStatus(ProductStatus.PENDING);
        return productRepository.save(product);
    }

    @Override
    public Product getProductById(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        Optional<Product> result = productRepository.findById(id);
        if (result.isPresent()) {
            return result.get();
        }
        throw new ProductNotFoundException("Product not found: " + id);
    }

    @Override
    public Product updateProduct(String id, Product product) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        if (product == null) {
            throw new IllegalArgumentException("Invalid product");
        }
        Product existingProduct = getProductById(id);
        if (product.getItem() == null ||
                product.getItem().getName() == null ||
                product.getItem().getName().isBlank()) {
            throw new IllegalArgumentException("Invalid product");
        }
        if (product.getStartingPrice() == null ||
                product.getStartingPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Invalid starting price");
        }
        if (product.getCategory() == null ||
                product.getCategory().getId() == null ||
                product.getCategory().getId().isBlank()) {
            throw new IllegalArgumentException("Invalid category");
        }

        existingProduct.setItem(product.getItem());
        existingProduct.setStartingPrice(product.getStartingPrice());
        existingProduct.setCategory(product.getCategory());

        return productRepository.save(existingProduct);
    }

    @Override
    public void deleteProduct(String id) {
        if (id == null || id.isBlank()) {
            throw new IllegalArgumentException("Invalid ID");
        }
        getProductById(id);
        productRepository.deleteById(id);
    }

    @Override
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Product approveProduct(String id) {
        Product product = getProductById(id);
        if(product.getStatus() != ProductStatus.PENDING) {
            throw new IllegalStateException("Product is not pending");
        }
        product.setStatus(ProductStatus.ACTIVE);
        return productRepository.save(product);
    }
}
