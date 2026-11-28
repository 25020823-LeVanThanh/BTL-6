package com.auctionuet.server.service;
import com.auctionuet.server.model.Role;
import com.auctionuet.server.model.User;
public interface PermissionService {
    boolean hasRole(User user, Role role); // Kiểm tra user có đúng vai trò (Role) chỉ định không
    boolean isAdmin(User user); // Kiểm tra có phải Quản trị viên (ADMIN) không
    boolean canCreateProduct(User user); // Kiểm tra quyền được phép đăng sản phẩm mới
    boolean canCreateAuction(User user); // Kiểm tra quyền được phép mở phiên đấu giá
    boolean canPlaceBid(User user); // Kiểm tra quyền được phép tham gia đặt giá
    void checkPermission(User user, Role requiredRole); // Hàm chặn: ném lỗi ngay lập tức nếu user không đủ quyền
}
