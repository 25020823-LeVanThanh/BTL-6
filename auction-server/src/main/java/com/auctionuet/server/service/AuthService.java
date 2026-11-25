package com.auctionuet.server.service;
import com.auctionuet.server.model.Role;
import com.auctionuet.server.model.Session;
import com.auctionuet.server.model.User;
public interface AuthService {
    User register(String username, String rawPassword, String fullName, String email, Role role); // Đăng ký tài khoản mới
    Session login(String username, String rawPassword); // Đăng nhập và trả về phiên làm việc (Session)
    void logout(String token); // Đăng xuất khỏi hệ thống
    void changePassword(String userId, String oldPassword, String newPassword); // Đổi mật khẩu
}
