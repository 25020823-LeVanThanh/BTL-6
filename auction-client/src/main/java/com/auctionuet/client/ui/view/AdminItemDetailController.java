package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.AdminClient;
import com.auctionuet.client.network.ImageLoader;
import com.auctionuet.protocol.dto.response.item.ItemDTO;
import com.auctionuet.protocol.enums.ItemApprovalStatus;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.time.format.DateTimeFormatter;
import java.util.Map;

/**
 * Admin xem một sản phẩm chờ duyệt: thông tin, ảnh, thông số và nút Duyệt / Từ chối.
 * Hai nút chỉ hiện khi sản phẩm còn trạng thái PENDING.
 */
public class AdminItemDetailController {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label nameLabel;
    @FXML private Label sellerLabel;
    @FXML private Label typeLabel;
    @FXML private Label conditionLabel;
    @FXML private Label priceLabel;
    @FXML private Label createdLabel;
    @FXML private Label descriptionLabel;
    @FXML private Label specificationsLabel;
    @FXML private Label approvalStatusLabel;
    @FXML private Label statusMessage;
    @FXML private Label imagePlaceholderLabel;
    @FXML private ImageView productImageView;
    @FXML private HBox actionArea;

    private final AdminClient adminClient = new AdminClient();
    private ItemDTO currentItem;

    /** Màn danh sách truyền vật phẩm vào sau khi FXML đã tạo controller. */
    public void setItem(ItemDTO item) {
        currentItem = item;
        renderItem();
    }

    /** Quay lại bảng kiểm duyệt, thay đúng vùng nội dung giữa Dashboard. */
    @FXML
    private void handleBack() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/AdminItemsView.fxml"));
            Parent root = loader.load();
            replaceContent(root);
        } catch (Exception e) {
            statusMessage.setText("Lỗi quay lại danh sách: " + e.getMessage());
        }
    }

    /** Duyệt sản phẩm đang chờ. */
    @FXML
    private void handleApprove() {
        updateStatus(ItemApprovalStatus.APPROVED);
    }

    /** Từ chối sản phẩm đang chờ. */
    @FXML
    private void handleReject() {
        updateStatus(ItemApprovalStatus.REJECTED);
    }

    /** Đổ toàn bộ nhãn, badge trạng thái, nút hành động và ảnh. */
    private void renderItem() {
        if (currentItem == null) {
            statusMessage.setText("Không tìm thấy sản phẩm.");
            return;
        }
        nameLabel.setText(currentItem.getName());
        sellerLabel.setText("Seller: " + currentItem.getSeller().getUsername());
        typeLabel.setText("Loại: " + currentItem.getType());
        conditionLabel.setText("Tình trạng: " + currentItem.getCondition());
        priceLabel.setText(CurrencyFormatter.format(currentItem.getStartingPrice()));
        createdLabel.setText("Tạo lúc: " + formatCreatedAt());
        descriptionLabel.setText(valueOrDefault(currentItem.getDescription(), "Không có mô tả."));
        specificationsLabel.setText(formatExtraFields(currentItem.getExtraFields()));
        updateApprovalBadge(currentItem.getApprovalStatus());
        // Chỉ sản phẩm chờ duyệt mới hiện nút Duyệt / Từ chối.
        boolean pending = currentItem.getApprovalStatus() == ItemApprovalStatus.PENDING;
        actionArea.setVisible(pending);
        actionArea.setManaged(pending);
        loadImage();
    }

    /** Tải ảnh sản phẩm; không có URL thì ImageLoader hiện placeholder. */
    private void loadImage() {
        ImageLoader.loadImage(productImageView, imagePlaceholderLabel, currentItem.getImageUrl());
    }

    /**
     * Gửi duyệt hoặc từ chối trên luồng nền.
     * Chỉ chạy khi sản phẩm còn PENDING; trong lúc chờ thì khóa cụm nút.
     */
    private void updateStatus(ItemApprovalStatus status) {
        if (currentItem == null || currentItem.getApprovalStatus() != ItemApprovalStatus.PENDING) {
            return;
        }
        String token = ClientSession.getInstance().getToken();
        actionArea.setDisable(true);
        statusMessage.setText("Đang cập nhật sản phẩm...");
        new Thread(() -> {
            try {
                ItemDTO updated = adminClient.updateItemApprovalStatus(token, currentItem.getId(), status);
                Platform.runLater(() -> {
                    currentItem = updated;
                    actionArea.setDisable(false);
                    renderItem();
                    statusMessage.setText(status == ItemApprovalStatus.APPROVED
                            ? "Đã duyệt sản phẩm."
                            : "Đã từ chối sản phẩm.");
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    actionArea.setDisable(false);
                    statusMessage.setText("Lỗi: " + e.getMessage());
                });
            }
        }, "admin-item-approval-detail-update").start();
    }

    /** Đổi chữ và class CSS của badge: chờ duyệt / đã duyệt / bị từ chối. */
    private void updateApprovalBadge(ItemApprovalStatus status) {
        approvalStatusLabel.getStyleClass().removeAll(
                "status-badge-open", "status-badge-running", "status-badge-canceled");
        switch (status) {
            case PENDING -> {
                approvalStatusLabel.setText("Chờ duyệt");
                approvalStatusLabel.getStyleClass().add("status-badge-open");
            }
            case APPROVED -> {
                approvalStatusLabel.setText("Đã duyệt");
                approvalStatusLabel.getStyleClass().add("status-badge-running");
            }
            case REJECTED -> {
                approvalStatusLabel.setText("Bị từ chối");
                approvalStatusLabel.getStyleClass().add("status-badge-canceled");
            }
        }
    }

    /** Giờ tạo sản phẩm theo dd/MM/yyyy HH:mm. */
    private String formatCreatedAt() {
        return currentItem.getCreatedAt() != null
                ? currentItem.getCreatedAt().format(DATE_FORMAT)
                : "Chưa rõ";
    }

    /** Mỗi thông số phụ một dòng "Tên: giá trị". */
    private String formatExtraFields(Map<String, Object> extraFields) {
        if (extraFields == null || extraFields.isEmpty()) {
            return "Không có thông số bổ sung.";
        }
        StringBuilder values = new StringBuilder();
        for (Map.Entry<String, Object> field : extraFields.entrySet()) {
            if (!values.isEmpty()) {
                values.append("\n");
            }
            values.append(formatFieldName(field.getKey()))
                    .append(": ")
                    .append(field.getValue());
        }
        return values.toString();
    }

    /** Đổi camelCase thành nhãn đọc được, ví dụ warrantyMonths -> Warranty Months. */
    private String formatFieldName(String key) {
        StringBuilder text = new StringBuilder();
        for (int index = 0; index < key.length(); index++) {
            char value = key.charAt(index);
            if (index > 0 && Character.isUpperCase(value)) {
                text.append(' ');
            }
            text.append(index == 0 ? Character.toUpperCase(value) : value);
        }
        return text.toString();
    }

    /** Chuỗi trống dùng câu mặc định, ví dụ "Không có mô tả." */
    private String valueOrDefault(String value, String defaultValue) {
        return value != null && !value.isBlank() ? value : defaultValue;
    }

    /** Thay vùng giữa Dashboard bằng danh sách kiểm duyệt. */
    private void replaceContent(Parent content) {
        Parent root = nameLabel.getScene().getRoot();
        if (root instanceof HBox dashboard) {
            VBox mainCard = (VBox) dashboard.getChildren().get(1);
            StackPane contentArea = (StackPane) mainCard.getChildren().get(1);
            contentArea.getChildren().setAll(content);
        }
    }
}
