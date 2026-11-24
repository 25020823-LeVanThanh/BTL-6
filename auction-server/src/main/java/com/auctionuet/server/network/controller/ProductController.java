
/*  VAI TRÒ FILE:
- Tiếp nhận yêu cầu và chuyển cho service
- ProductController là nhân viên tiếp nhận đơn hàng, còn ProductService là bộ phận xử lý đơn hàng.
 */

package com.auctionuet.server.network.controller;

import com.auctionuet.server.service.ProductService;
import com.auctionuet.server.model.Product;

import java.util.List;

import org.springframework.web.bind.annotation.*;
/*
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
*/

@RestController                         // Đánh dấu đây là lớp Controller xử lý yêu cầu HTTP theo Spring Boot.
@RequestMapping("/api/products")        // Khai báo tiền tố đường dẫn API

public class ProductController {
    private final ProductService productService;
    public ProductController(ProductService productService) {       // Constructor
        this.productService = productService;
    }

    // Lấy toàn bộ sản phẩm
    @GetMapping
    public List<Product> getAllProducts(){
        return productService.getAllProducts();
    }

    //  Tìm sản phẩm theo ID
    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        // Gọi ProductService để tìm sản phẩm theo id
        return productService.getProductById(id);
    }

    /*  QUY TRÌNH KHI NGƯỜI BÁN ĐĂNG SẢN PHẨM MỚI:
    1.  Client: Gửi POST /api/products cùng dữ liệu sản phẩm
    2.  ProductController: @RequestBody nhận dữ liệu thành đối tượng Product
    3.  ProductService: createProduct(product) xử lý việc tạo sản phẩm
     */

    // @PostMapping là annotation của Spring Boot dùng để đánh dấu phương thức xử lý yêu cầu HTTP POST.
    // Tạo sản phẩm mới
    @PostMapping
    public Product createProduct(@RequestBody Product product) {    // @RequestBody: Nhận dữ liệu sản phẩm từ HTTP request
        return productService.createProduct(product);
    }

    // Cập nhật hoặc thay thế thông tin sản phẩm theo thiết kế API
    @PutMapping("/{id}")
    public Product updateProduct(@PathVariable String id, @RequestBody Product product) {
        return productService.updateProduct(id, product);
    }
}
