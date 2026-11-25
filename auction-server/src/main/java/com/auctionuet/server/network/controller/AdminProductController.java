package com.auctionuet.server.network.controller;

import com.auctionuet.server.service.ProductService;
import com.auctionuet.server.model.Product;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {
    private final ProductService productService;
    public AdminProductController(ProductService productService) {     // Constructor
        this.productService = productService;
    }

    // Xóa sản phẩm
    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable String id) {
        productService.deleteProduct(id);
    }

    @PutMapping("/{id}/approve")    // PutMapping: Nhận yêu cầu HTTP PUT.
    public Product approveProduct(@PathVariable String id) {
        return productService.approveProduct(id);
    }
}
