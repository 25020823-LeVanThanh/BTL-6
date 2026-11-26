package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.AdminClient;
import com.auctionuet.client.network.ServerConnection;
import com.auctionuet.protocol.PushActionType;
import com.auctionuet.protocol.PushMessage;
import com.auctionuet.protocol.dto.push.PushEvents;
import com.auctionuet.protocol.dto.response.admin.SystemMonitorDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Màn giám sát hệ thống của admin.
 * Đăng ký một lần để nhận snapshot, sau đó cập nhật bằng tin push realtime.
 * Sequence cũ hơn snapshot mới nhất thì bỏ qua, tránh số liệu bị lùi.
 */
public class AdminSystemMonitorController implements DashboardContentLifecycle {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    @FXML private Label activeConnectionLabel;
    @FXML private Label liveAuctionLabel;
    @FXML private Label lastUpdatedLabel;
    @FXML private Label statusLabel;
    @FXML private Button retryButton;

    private final AdminClient adminClient = new AdminClient();
    /** false khi đã rời màn hình, luồng nền không được vẽ UI nữa. */
    private volatile boolean active;
    private long latestSequence = -1;

    /** Đánh dấu màn hình đang mở, gắn listener push và gửi lệnh subscribe. */
    @FXML
    public void initialize() {
        active = true;
        ServerConnection.getInstance().setPushListener(this::onPushMessage);
        subscribe();
    }

    /** Ẩn nút thử lại, gắn lại listener rồi subscribe kênh giám sát một lần nữa. */
    @FXML
    private void handleRetry() {
        retryButton.setVisible(false);
        retryButton.setManaged(false);
        ServerConnection.getInstance().setPushListener(this::onPushMessage);
        subscribe();
    }

    /**
     * Xin snapshot ban đầu từ server.
     * Nếu user đã rời màn hình trước khi response về thì hủy đăng ký ngay, không vẽ UI.
     */
    private void subscribe() {
        String token = ClientSession.getInstance().getToken();
        if (token == null || token.isBlank()) {
            showError("Phiên đăng nhập không hợp lệ.");
            return;
        }
        statusLabel.setText("Đang kết nối kênh giám sát...");
        new Thread(() -> {
            try {
                SystemMonitorDTO snapshot = adminClient.subscribeSystemMonitor(token);
                if (!active) {
                    unsubscribeQuietly(token);
                    return;
                }
                Platform.runLater(() -> {
                    if (active) {
                        renderSnapshot(
                                snapshot.getActiveConnectionCount(),
                                snapshot.getLiveAuctionCount(),
                                snapshot.getSequence(),
                                snapshot.getCapturedAt());
                        statusLabel.setText("Đang theo dõi realtime.");
                    }
                });
            } catch (Exception e) {
                if (active) {
                    Platform.runLater(() -> showError("Lỗi kết nối monitor: " + e.getMessage()));
                }
            }
        }, "admin-monitor-subscribe").start();
    }

    /** Chỉ xử lý push SYSTEM_MONITOR_UPDATED và đưa số liệu về luồng JavaFX. */
    private void onPushMessage(PushMessage push) {
        if (!active || push == null || push.getPushType() != PushActionType.SYSTEM_MONITOR_UPDATED) {
            return;
        }
        PushEvents.SystemMonitorUpdatedPush data =
                push.getDataAs(PushEvents.SystemMonitorUpdatedPush.class);
        if (data == null) {
            return;
        }
        Platform.runLater(() -> {
            if (active) {
                renderSnapshot(
                        data.getActiveConnectionCount(),
                        data.getLiveAuctionCount(),
                        data.getSequence(),
                        data.getCapturedAt());
            }
        });
    }

    private void renderSnapshot(
            int activeConnectionCount,
            int liveAuctionCount,
            long sequence,
            LocalDateTime capturedAt) {
        // Tin đến muộn có sequence nhỏ hơn: giữ số liệu mới hơn.
        if (sequence < latestSequence) {
            return;
        }
        latestSequence = sequence;
        activeConnectionLabel.setText(Integer.toString(activeConnectionCount));
        liveAuctionLabel.setText(Integer.toString(liveAuctionCount));
        lastUpdatedLabel.setText("Cập nhật lúc: " + (capturedAt != null
                ? capturedAt.format(DISPLAY_TIME)
                : "Chưa rõ"));
    }

    /** Hiện lỗi và bật nút thử lại để admin subscribe lại kênh. */
    private void showError(String message) {
        statusLabel.setText(message);
        retryButton.setVisible(true);
        retryButton.setManaged(true);
    }

    /** Dashboard gọi khi rời trang: tắt vẽ UI, gỡ listener và hủy đăng ký trên luồng nền. */
    @Override
    public void cleanup() {
        active = false;
        ServerConnection.getInstance().setPushListener(null);
        String token = ClientSession.getInstance().getToken();
        if (token != null && !token.isBlank()) {
            new Thread(() -> unsubscribeQuietly(token), "admin-monitor-unsubscribe").start();
        }
    }

    /** Hủy đăng ký monitor; lỗi mạng được nuốt để việc chuyển trang không bị kẹt. */
    private void unsubscribeQuietly(String token) {
        try {
            adminClient.unsubscribeSystemMonitor(token);
        } catch (Exception ignored) {
            // Bỏ qua lỗi dọn đăng ký để thao tác điều hướng không bị chặn.
        }
    }
}
