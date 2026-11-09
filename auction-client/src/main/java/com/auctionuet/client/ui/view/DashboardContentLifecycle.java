package com.auctionuet.client.ui.view;

/**
 * Màn hình con của Dashboard có thể dọn tài nguyên khi người dùng chuyển trang.
 * Ví dụ: hủy đăng ký push realtime, dừng đồng hồ đếm ngược.
 */
public interface DashboardContentLifecycle {
    /** Gọi trước khi thay nội dung giữa Dashboard. */
    void cleanup();
}
