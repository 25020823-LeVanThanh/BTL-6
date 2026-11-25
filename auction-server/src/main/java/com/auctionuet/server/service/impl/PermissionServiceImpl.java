package com.auctionuet.server.service.impl;
import com.auctionuet.server.model.Role;
import com.auctionuet.server.model.User;
import com.auctionuet.server.service.PermissionService;
import org.springframework.stereotype.Service;
public class PermissionServiceImpl implements PermissionService {
    public boolean hasRole(User user, Role role) {
        return user != null && user.getRole() != null && user.getRole() == role; // So sánh enum Role của user với role yêu cầu
    }

    public boolean isAdmin(User user) {
        return hasRole(user, Role.ADMIN); // Trả về true nếu user có quyền ADMIN
    }

    public boolean canCreateProduct(User user) {
        return hasRole(user, Role.SELLER) || isAdmin(user); // Chỉ Người bán (SELLER) hoặc ADMIN mới được tạo sản phẩm
    }

    public boolean canCreateAuction(User user) {
        return hasRole(user, Role.SELLER) || isAdmin(user); // Chỉ SELLER hoặc ADMIN mới được mở phiên đấu giá
    }

    public boolean canPlaceBid(User user) {
        return hasRole(user, Role.BIDDER) || isAdmin(user); // Chỉ Người mua (BIDDER) hoặc ADMIN mới được đặt giá thầu
    }

    public void checkPermission(User user, Role requiredRole) {
        // Dùng ở đầu các hàm xử lý quan trọng: nếu không đúng quyền và cũng không phải ADMIN thì chặn lại bằng SecurityException
        if (!hasRole(user, requiredRole) && !isAdmin(user)) {
            throw new SecurityException("Bạn không có quyền thực hiện thao tác này. Yêu cầu quyền: " + requiredRole);
        }
    }
}
