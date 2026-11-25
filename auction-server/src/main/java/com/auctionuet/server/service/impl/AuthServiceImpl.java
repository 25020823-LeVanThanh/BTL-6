package com.auctionuet.server.service.impl;
import com.auctionuet.server.model.Account;
import com.auctionuet.server.model.Role;
import com.auctionuet.server.model.Session;
import com.auctionuet.server.model.User;
import com.auctionuet.server.repository.UserRepository;
import com.auctionuet.server.service.AuthService;
import com.auctionuet.server.service.SessionService;
import com.auctionuet.server.util.PasswordUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository; // Dùng để lưu và tìm kiếm tài khoản người dùng
    private final SessionService sessionService; // Dùng để tạo/hủy phiên đăng nhập khi login/logout

    public AuthServiceImpl(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.sessionService = sessionService;
    }

    public User register(String username, String rawPassword, String fullName, String email, Role role) {
        if (username == null || username.isBlank() || rawPassword == null || rawPassword.isBlank()) { // Kiểm tra đầu vào không được để trống
            throw new IllegalArgumentException("Username và mật khẩu không được để trống");
        }
        if (userRepository.findByUsername(username).isPresent()) { // Kiểm tra xem username đã có người đăng ký chưa
            throw new IllegalArgumentException("Username đã được sử dụng");
        }

        String userId = "U_" + UUID.randomUUID().toString().substring(0, 8); // Sinh mã ID cho người dùng mới
        String hashedPassword = PasswordUtils.hashPassword(rawPassword); // Mã hóa mật khẩu bằng SHA-256 trước khi lưu

        Account account = new Account(username.trim(), hashedPassword); // Tạo đối tượng Account chứa username và mật khẩu đã băm
        Role assignedRole = (role != null) ? role : Role.BIDDER; // Nếu không chọn quyền thì mặc định là người mua (BIDDER)
        User newUser = new User(userId, fullName, email, assignedRole, account); // Tạo đối tượng User (tự động tạo kèm ví Wallet 0đ)

        return userRepository.save(newUser); // Lưu người dùng mới xuống file users.json
    }

    public Session login(String username, String rawPassword) {
        User user = userRepository.findByUsername(username) // Tìm user trong file JSON theo username
                .orElseThrow(() -> new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu")); // Báo lỗi chung để tránh lộ thông tin tài khoản tồn tại

        Account account = user.getAccount();
        // Kiểm tra mật khẩu người dùng nhập vào có khớp với chuỗi đã băm trong Account không
        if (account == null || !PasswordUtils.verifyPassword(rawPassword, account.getPasswordHash())) {
            throw new IllegalArgumentException("Sai tên đăng nhập hoặc mật khẩu");
        }

        return sessionService.createSession(user); // Đăng nhập thành công -> tạo phiên mới và trả về Session chứa token
    }

    public void logout(String token) {
        sessionService.invalidateSession(token); // Xóa token khỏi SessionService
    }

    public void changePassword(String userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId) // Tìm người dùng theo ID
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));

        Account account = user.getAccount();
        if (account == null || !PasswordUtils.verifyPassword(oldPassword, account.getPasswordHash())) { // Xác thực mật khẩu cũ
            throw new IllegalArgumentException("Mật khẩu cũ không chính xác");
        }

        account.setPasswordHash(PasswordUtils.hashPassword(newPassword)); // Băm mật khẩu mới và cập nhật vào Account
        userRepository.save(user); // Lưu lại thông tin mới xuống file users.json
    }
}
