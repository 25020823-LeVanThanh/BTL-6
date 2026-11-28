package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.ItemClient;
import com.auctionuet.client.network.ImageLoader;
import com.auctionuet.protocol.dto.response.auction.AuctionDTO;
import com.auctionuet.protocol.dto.response.item.ItemDTO;
import com.auctionuet.protocol.dto.response.user.UserDTO;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Chi tiết một vật phẩm và bảng các phiên đấu giá từng gắn với nó.
 * Dữ liệu phiên được sắp theo thời gian kết thúc, thiếu thì dùng thời điểm bắt đầu.
 */
public class ItemHistoryController {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML private Label nameLabel, ownerLabel, typeLabel, specLabel, conditionLabel, descriptionLabel, statusLabel, imagePlaceholderLabel;
    @FXML private ImageView productImageView;
    @FXML private TableView<AuctionDTO> auctionHistoryTable;
    @FXML private TableColumn<AuctionDTO, String> titleCol, priceCol, statusCol, timeCol;

    private final ItemClient itemClient = new ItemClient();
    private ItemDTO currentItem;

    /** Gắn cách lấy dữ liệu cho từng cột của bảng lịch sử phiên. */
    @FXML
    public void initialize() {
        setupAuctionHistoryTable();
    }
    /** Màn trước truyền vật phẩm vào sau khi FXML đã initialize. */
    public void setItem(ItemDTO item) {
        this.currentItem = item;
        updateItemInfo(item);
        loadAuctionHistory();
    }

    /** Điền tên, chủ sở hữu, loại, tình trạng, mô tả, thông số và ảnh. */
    private void updateItemInfo(ItemDTO item) {
        if (item == null) {
            statusLabel.setText("Không tìm thấy vật phẩm.");
            return;
        }

        nameLabel.setText("Vật phẩm: " + item.getName());
        ownerLabel.setText("Chủ sở hữu: " + usernameOf(item.getSeller()));
        typeLabel.setText("Loại: " + item.getType());
        conditionLabel.setText("Tình trạng: " + valueOrEmpty(item.getCondition()));
        descriptionLabel.setText(nullToEmpty(item.getDescription()));
        specLabel.setText("Thông số: " + formatExtraFields(item.getExtraFields()));

        // Tải hình ảnh sản phẩm thực tế và quản lý placeholder qua ImageLoader
        ImageLoader.loadImage(productImageView, imagePlaceholderLabel, item.getImageUrl());
    }

    /** Xin lịch sử phiên của vật phẩm hiện tại trên luồng nền. */
    private void loadAuctionHistory() {
        String token = ClientSession.getInstance().getToken();
        if (token == null || currentItem == null) {
            statusLabel.setText("Bạn chưa đăng nhập.");
            return;
        }

        statusLabel.setText("Đang tải lịch sử vật phẩm...");
        new Thread(() -> {
            try {
                List<AuctionDTO> history = itemClient.getItemAuctionHistory(token, currentItem.getId());
                Platform.runLater(() -> renderAuctionHistory(history));
            } catch (Exception e) {
                Platform.runLater(() -> statusLabel.setText("Lỗi tải lịch sử vật phẩm: " + e.getMessage()));
            }
        }).start();
    }

    /** Cột tiêu đề, giá, trạng thái và thời điểm kết thúc lấy trực tiếp từ AuctionDTO. */
    private void setupAuctionHistoryTable() {
        titleCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(valueOrUnknown(cell.getValue().getTitle())));
        priceCol.setCellValueFactory(cell ->
                new ReadOnlyStringWrapper(CurrencyFormatter.format(cell.getValue().getCurrentPrice())));
        statusCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getStatus() != null ? cell.getValue().getStatus().name() : "UNKNOWN"));
        timeCol.setCellValueFactory(cell -> new ReadOnlyStringWrapper(formatTime(cell.getValue().getEndTime())));
    }

    /** Sắp phiên theo thời gian rồi đổ vào bảng; rỗng thì báo chưa từng đấu giá. */
    private void renderAuctionHistory(List<AuctionDTO> history) {
        auctionHistoryTable.getItems().clear();

        if (history == null || history.isEmpty()) {
            statusLabel.setText("Vật phẩm này chưa từng gắn với phiên đấu giá nào.");
            return;
        }

        List<AuctionDTO> sortedHistory = history.stream()
                .sorted(Comparator.comparing(this::historyTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        auctionHistoryTable.getItems().setAll(sortedHistory);

        statusLabel.setText("Có " + sortedHistory.size() + " phiên đấu giá gắn với vật phẩm này.");
    }

    /** Mốc sắp xếp: ưu tiên giờ kết thúc, không có thì dùng giờ bắt đầu. */
    private LocalDateTime historyTime(AuctionDTO auction) {
        if (auction == null) {
            return null;
        }
        return auction.getEndTime() != null ? auction.getEndTime() : auction.getStartTime();
    }

    /** Nối extraFields thành một dòng "Tên: giá trị | Tên: giá trị". */
    private String formatExtraFields(Map<String, Object> extraFields) {
        if (extraFields == null || extraFields.isEmpty()) {
            return "--";
        }
        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, Object> entry : extraFields.entrySet()) {
            if (!builder.isEmpty()) {
                builder.append(" | ");
            }
            builder.append(formatExtraFieldName(entry.getKey()))
                    .append(": ")
                    .append(formatExtraFieldValue(entry.getValue()));
        }
        return builder.toString();
    }

    /** Đổi camelCase (manufactureYear) thành chữ có khoảng trắng để đọc trên UI. */
    private String formatExtraFieldName(String key) {
        if ("brand".equals(key)) {
            return "Brand";
        }
        if ("warrantyMonths".equals(key)) {
            return "Warranty";
        }
        if ("yearCreated".equals(key)) {
            return "Year created";
        }
        if ("manufactureYear".equals(key)) {
            return "Manufacture year";
        }
        if ("odo".equals(key)) {
            return "Odo";
        }

        StringBuilder result = new StringBuilder();
        for (int i = 0; i < key.length(); i++) {
            char ch = key.charAt(i);
            if (i > 0 && Character.isUpperCase(ch)) {
                result.append(' ');
            }
            result.append(i == 0 ? Character.toUpperCase(ch) : ch);
        }
        return result.toString();
    }

    /** Số nguyên (1.0) hiện không phần thập phân; giá trị khác giữ nguyên chuỗi. */
    private String formatExtraFieldValue(Object value) {
        if (value instanceof Number number) {
            double doubleValue = number.doubleValue();
            if (doubleValue == Math.rint(doubleValue)) {
                return String.format("%.0f", doubleValue);
            }
        }
        return value != null ? value.toString() : "";
    }

    /** Định dạng thời điểm phiên; null thì "Chưa rõ". */
    private String formatTime(LocalDateTime time) {
        return time != null ? time.format(DISPLAY_TIME) : "Chưa rõ";
    }

    /** Null thành chuỗi rỗng để label không hiện chữ "null". */
    private String nullToEmpty(String value) {
        return value != null ? value : "";
    }

    /** Object null thành chuỗi rỗng, còn lại dùng toString. */
    private String valueOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    /** Chuỗi trống thành "Chưa rõ". */
    private String valueOrUnknown(String value) {
        return value != null && !value.isBlank() ? value : "Chưa rõ";
    }

    /** Lấy username của seller; thiếu user thì "Chưa rõ". */
    private String usernameOf(UserDTO user) {
        return user != null ? user.getUsername() : "Chưa rõ";
    }
}
