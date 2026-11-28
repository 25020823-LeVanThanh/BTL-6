package com.auctionuet.server.network.controller;
import com.auctionuet.server.model.Role;
import com.auctionuet.server.model.Session;
import com.auctionuet.server.model.User;
import com.auctionuet.server.service.AuthService;
import com.auctionuet.server.service.SessionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
@RestController
@RequestMapping("/api")
public class AuthController {
    private final AuthService authService; // Gọi tầng nghiệp vụ xử lý đăng ký, đăng nhập, đổi mật khẩu
    private final SessionService sessionService; // Gọi tầng quản lý phiên để kiểm tra token

    // Constructor Injection: Spring tự động tiêm AuthServiceImpl và SessionServiceImpl vào đây
    public AuthController(AuthService authService, SessionService sessionService) {
        this.authService = authService;
        this.sessionService = sessionService;
    }

    // 1. ĐĂNG KÝ TÀI KHOẢN: POST /api/auth/register
    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username"); // Lấy tên đăng nhập từ JSON client gửi
            String password = request.get("password"); // Lấy mật khẩu thô
            String fullName = request.get("fullName"); // Lấy họ tên
            String email = request.get("email"); // Lấy email
            String roleStr = request.get("role"); // Lấy chuỗi vai trò ("BIDDER" hoặc "SELLER")

            // Chuyển chuỗi role sang enum Role (mặc định là BIDDER nếu không truyền)
            Role role = (roleStr != null && !roleStr.isBlank())
                    ? Role.valueOf(roleStr.toUpperCase())
                    : Role.BIDDER;

            // Gọi AuthService để kiểm tra trùng lặp, băm mật khẩu SHA-256 và lưu User mới xuống file JSON
            User createdUser = authService.register(username, password, fullName, email, role);

            // Đóng gói thông tin phản hồi trả về cho Client
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng ký tài khoản thành công");
            response.put("userId", createdUser.getId());
            response.put("username", createdUser.getAccount().getUsername());
            response.put("role", createdUser.getRole());

            return ResponseEntity.status(HttpStatus.CREATED).body(response); // Trả về HTTP 201 Created
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage())); // Lỗi dữ liệu -> HTTP 400
        }
    }

    // 2. ĐĂNG NHẬP: POST /api/auth/login
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        try {
            String username = request.get("username");
            String password = request.get("password");

            // Gọi AuthService xác thực mật khẩu; đúng thì tạo Session mới chứa token
            Session session = authService.login(username, password);

            Map<String, Object> response = new HashMap<>();
            response.put("message", "Đăng nhập thành công");
            response.put("token", session.getToken());
            response.put("userId", session.getUserId());
            response.put("expiresAt", session.getExpiresAt());

            return ResponseEntity.ok(response); // Trả về HTTP 200 OK
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", e.getMessage())); // Sai tài khoản/mật khẩu -> HTTP 401
        }
    }

    // 3. ĐĂNG XUẤT: POST /api/auth/logout
    @PostMapping("/auth/logout")
    public ResponseEntity<?> logout(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader); // Tách mã token từ Header "Authorization: Bearer <token>"
        if (token == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Thiếu token xác thực"));
        }

        authService.logout(token); // Xóa token khỏi RAM trong SessionService
        return ResponseEntity.ok(Map.of("message", "Đăng xuất thành công"));
    }

    // 4. LẤY THÔNG TIN USER HIỆN TẠI TỪ TOKEN: GET /api/auth/me
    @GetMapping("/auth/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        String token = extractToken(authHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Chưa đăng nhập"));
        }

        // Tìm User tương ứng với token còn hạn; nếu hợp lệ trả về User, hết hạn báo lỗi 401
        return sessionService.getUserByToken(token)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("error", "Phiên đăng nhập không hợp lệ hoặc đã hết hạn")));
    }

    // 5. ĐỔI MẬT KHẨU: POST /api/auth/change-password
    @PostMapping("/auth/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @RequestBody Map<String, String> request) {
        try {
            String token = extractToken(authHeader);
            User currentUser = sessionService.getUserByToken(token)
                    .orElseThrow(() -> new IllegalArgumentException("Phiên đăng nhập không hợp lệ"));

            String oldPassword = request.get("oldPassword");
            String newPassword = request.get("newPassword");

            authService.changePassword(currentUser.getId(), oldPassword, newPassword);
            return ResponseEntity.ok(Map.of("message", "Đổi mật khẩu thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    // Hàm phụ trợ: Cắt bỏ tiền tố "Bearer " nếu Client gửi token theo chuẩn HTTP Header
    private String extractToken(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        if (authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7).trim();
        }
        return authHeader.trim();
    }

}
