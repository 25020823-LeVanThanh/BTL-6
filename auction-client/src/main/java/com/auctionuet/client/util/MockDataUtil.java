package com.auctionuet.client.util;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Dữ liệu giả để dựng giao diện khi server chưa trả về kết quả.
 */
public final class MockDataUtil {
    private MockDataUtil() {
    }

    public record SampleUser(String username, String email, String role, long balance) {
    }

    public record SampleItem(String name, String type, String condition, long price) {
    }

    public record SampleAuction(
            String id,
            String title,
            String seller,
            long currentPrice,
            String status,
            LocalDateTime endTime) {
    }

    public static SampleUser bidder() {
        return new SampleUser("bidder01", "bidder01@uet.edu.vn", "BIDDER", 5_000_000L);
    }

    public static SampleUser seller() {
        return new SampleUser("seller01", "seller01@uet.edu.vn", "SELLER", 12_500_000L);
    }

    public static List<SampleItem> items() {
        return List.of(
                new SampleItem("Laptop Dell XPS 13", "ELECTRONICS", "Đã qua sử dụng", 15_000_000L),
                new SampleItem("Tranh sơn dầu Hà Nội", "ART", "Mới", 8_000_000L),
                new SampleItem("Honda Vision 2022", "VEHICLE", "Đã qua sử dụng", 22_000_000L)
        );
    }

    public static List<SampleAuction> auctions() {
        LocalDateTime now = LocalDateTime.now();
        return List.of(
                new SampleAuction("AUC-001", "Laptop Dell XPS 13", "seller01", 16_500_000L, "RUNNING", now.plusHours(2)),
                new SampleAuction("AUC-002", "Tranh sơn dầu Hà Nội", "seller01", 8_200_000L, "RUNNING", now.plusMinutes(40)),
                new SampleAuction("AUC-003", "Honda Vision 2022", "seller02", 23_000_000L, "SCHEDULED", now.plusDays(1))
        );
    }
}
