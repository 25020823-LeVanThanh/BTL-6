/*  VAI TRÒ FILE:
- Tiếp nhận yêu cầu và chuyển cho service
- ProductController là nhân viên tiếp nhận đơn hàng, còn ProductService là bộ phận xử lý đơn hàng.
 */

package com.auctionuet.server.network.controller;

import java.util.List;
import java.util.Optional;

import com.auctionuet.server.service.ProductService;
import com.auctionuet.server.service.SessionService;
import com.auctionuet.server.model.Product;
import com.auctionuet.server.model.User;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.*;
// RequestMapping, RestController, PathVariable, GetMapping, PostMapping, PutMapping, DeleteMapping, PathVariable, RequestBody

@RestController                         // Đánh dấu đây là lớp Controller xử lý yêu cầu HTTP theo Spring Boot.
@RequestMapping("/api/products")        // Khai báo tiền tố đường dẫn API

public class ProductController {
    private final ProductService productService;
    private final SessionService sessionService;
    public ProductController(ProductService productService, SessionService sessionService) {       // Constructor
        this.productService = productService;
        this.sessionService = sessionService;
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
    public Product updateProduct(
            @PathVariable String id,
            @RequestBody Product product,
            @RequestHeader(value = "Authorization", required = false) String authHeader) {

        if(authHeader == null || authHeader.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        String token;               // Khai báo String token
        if (authHeader.startsWith("Bearer ")) {         // Nếu Header có tiền tố "Bearer", loại bỏ tiền tố để lấy token
            token = authHeader.substring(7).trim();
        }
        else {
            token = authHeader.trim();                  // Tách token từ authHeader
        }
        if (token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication required");
        }

        Optional<User> result = sessionService.getUserByToken(token);       // Gọi phương thức tìm user dựa trên token
        User currentUser;                           // Tạo biến có kiểu dữ liệu User
        if (result.isPresent()) {                   // Kiểm tra xem Optional có chứa được user không (boolean)
            currentUser = result.get();             // Nếu có, lấy user khỏi Optional
        }
        else {
            throw new ResponseStatusException(      // Server từ chối yêu cầu bằng HTTP 401 Unauthorized
                    HttpStatus.UNAUTHORIZED,
                    "Invalid or expired token"
            );
        }
        /* Phân biệt HTTP 401 và 403:
        - 401 Unauthorized: Chưa được xác thực hợp lệ (VD: Token không tồn tại hoặc hết hạn)
        - 403 Forbidden: Đã xác thực nhưng không được phép (VD: Người bán A cố sửa sản phẩm của người bán B)
        */
        String currentUserId = currentUser.getId();

        try {
            return productService.updateProduct(id, product, currentUserId);
        }
        catch (SecurityException e) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage());
        }
    }
}
