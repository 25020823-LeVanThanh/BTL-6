package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.AuctionClient;
import com.auctionuet.client.network.ItemClient;
import com.auctionuet.protocol.dto.response.auction.AuctionDTO;
import com.auctionuet.protocol.dto.response.item.ItemDTO;
import com.auctionuet.protocol.enums.ItemApprovalStatus;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Seller tạo phiên đấu giá cho một vật phẩm đã duyệt.
 * Thời gian bắt đầu/kết thúc có mốc nhanh hoặc chọn ngày giờ tùy ý.
 * Anti-sniping: nếu có bid trong cửa sổ cuối phiên thì kéo dài thêm số giây gia hạn.
 */
public class CreateAuctionController {

    @FXML private ComboBox<ItemDTO> itemComboBox;
    @FXML private Label durationLabel, statusLabel;
    @FXML private TextField titleField, antiSnipingWindowField, antiSnipingExtensionField;
    @FXML private TextArea descArea;
    @FXML private DatePicker startDatePicker, endDatePicker;
    @FXML private Spinner<Integer> startHourSpinner, startMinuteSpinner, endHourSpinner, endMinuteSpinner;
    @FXML private RadioButton startNowRadio, startInFiveRadio, startCustomRadio;
    @FXML private RadioButton endOneHourRadio, endSixHoursRadio, endOneDayRadio, endCustomRadio;
    @FXML private VBox startCustomBox, endCustomBox;
    @FXML private VBox previewContentBox;

    private final ItemClient itemClient = new ItemClient();
    private final AuctionClient auctionClient = new AuctionClient();
    private String initialItemId;
    /** Tiêu đề tự điền lần trước, để không ghi đè tiêu đề người dùng đã sửa tay. */
    private String lastAutoTitle;
    private final ToggleGroup startModeGroup = new ToggleGroup();
    private final ToggleGroup endModeGroup = new ToggleGroup();

    /** Tải vật phẩm, gắn preview theo lựa chọn và dựng bộ chọn giờ. */
    @FXML
    public void initialize() {
        renderEmptyItemPreview();
        loadMyItems();

        itemComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                renderItemPreview(newVal);
                syncTitleWithSelectedItem(newVal);
            } else {
                renderEmptyItemPreview();
            }
        });

        setupTimeControls();
    }

    /** Spinner giờ/phút, radio mốc nhanh và listener tính lại thời lượng. */
    private void setupTimeControls() {
        setupSpinner(startHourSpinner, 0, 23, LocalDateTime.now().getHour());
        setupSpinner(startMinuteSpinner, 0, 59, LocalDateTime.now().getMinute());
        setupSpinner(endHourSpinner, 0, 23, LocalDateTime.now().plusHours(1).getHour());
        setupSpinner(endMinuteSpinner, 0, 59, LocalDateTime.now().plusHours(1).getMinute());

        startNowRadio.setToggleGroup(startModeGroup);
        startInFiveRadio.setToggleGroup(startModeGroup);
        startCustomRadio.setToggleGroup(startModeGroup);
        endOneHourRadio.setToggleGroup(endModeGroup);
        endSixHoursRadio.setToggleGroup(endModeGroup);
        endOneDayRadio.setToggleGroup(endModeGroup);
        endCustomRadio.setToggleGroup(endModeGroup);

        startNowRadio.setSelected(true);
        endOneHourRadio.setSelected(true);

        startModeGroup.selectedToggleProperty().addListener((o, old, now) -> syncTimeModeVisibility());
        endModeGroup.selectedToggleProperty().addListener((o, old, now) -> syncTimeModeVisibility());
        startModeGroup.selectedToggleProperty().addListener((o, old, now) -> calculateDuration());
        endModeGroup.selectedToggleProperty().addListener((o, old, now) -> calculateDuration());
        startDatePicker.valueProperty().addListener((o, old, now) -> calculateDuration());
        endDatePicker.valueProperty().addListener((o, old, now) -> calculateDuration());
        startHourSpinner.valueProperty().addListener((o, old, now) -> calculateDuration());
        startMinuteSpinner.valueProperty().addListener((o, old, now) -> calculateDuration());
        endHourSpinner.valueProperty().addListener((o, old, now) -> calculateDuration());
        endMinuteSpinner.valueProperty().addListener((o, old, now) -> calculateDuration());

        syncTimeModeVisibility();
        calculateDuration();
    }

    /** Spinner số nguyên, cho gõ tay, bước nhảy 1. */
    private void setupSpinner(Spinner<Integer> spinner, int min, int max, int initialValue) {
        spinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(min, max, initialValue, 1));
        spinner.setEditable(true);
        spinner.setPrefWidth(82);
    }

    /** Hiện thời lượng dự kiến hoặc lỗi nếu thiếu giờ / kết thúc không sau bắt đầu. */
    private void calculateDuration() {
        LocalDateTime start = resolveStartTime();
        LocalDateTime end = resolveEndTime(start);

        if (start == null || end == null) {
            durationLabel.setText("Thời lượng: Vui lòng chọn đủ ngày giờ.");
            durationLabel.setStyle("-fx-text-fill: #f39c12;");
            return;
        }

        if (!end.isAfter(start)) {
            durationLabel.setText("Lỗi: Thời gian kết thúc phải sau thời gian bắt đầu.");
            durationLabel.setStyle("-fx-text-fill: #e94560;");
            return;
        }

        Duration duration = Duration.between(start, end);
        durationLabel.setText(String.format(
                "Thời lượng dự kiến: %d ngày, %d giờ, %d phút",
                duration.toDays(),
                duration.toHoursPart(),
                duration.toMinutesPart()));
        durationLabel.setStyle("-fx-text-fill: #2ecc71;");
    }

    /** Bắt đầu ngay, sau 5 phút, hoặc ngày giờ tự chọn. */
    private LocalDateTime resolveStartTime() {
        LocalDateTime now = LocalDateTime.now();
        if (startNowRadio.isSelected()) {
            return now;
        }
        if (startInFiveRadio.isSelected()) {
            return now.plusMinutes(5);
        }
        return getCustomDateTime(startDatePicker, startHourSpinner, startMinuteSpinner);
    }

    /** Kết thúc sau 1 giờ, 6 giờ, 1 ngày, hoặc mốc tự chọn. */
    private LocalDateTime resolveEndTime(LocalDateTime start) {
        if (start == null) {
            return null;
        }
        if (endOneHourRadio.isSelected()) {
            return start.plusHours(1);
        }
        if (endSixHoursRadio.isSelected()) {
            return start.plusHours(6);
        }
        if (endOneDayRadio.isSelected()) {
            return start.plusDays(1);
        }
        return getCustomDateTime(endDatePicker, endHourSpinner, endMinuteSpinner);
    }

    /** Ghép DatePicker với giờ và phút đã commit từ spinner. */
    private LocalDateTime getCustomDateTime(
            DatePicker date,
            Spinner<Integer> hourSpinner,
            Spinner<Integer> minuteSpinner) {
        if (date.getValue() == null || hourSpinner.getValue() == null || minuteSpinner.getValue() == null) {
            return null;
        }
        commitSpinnerValue(hourSpinner);
        commitSpinnerValue(minuteSpinner);
        return LocalDateTime.of(date.getValue(), LocalTime.of(hourSpinner.getValue(), minuteSpinner.getValue()));
    }

    /** Spinner cho phép gõ tay; commit giá trị editor trước khi tính giờ. */
    private void commitSpinnerValue(Spinner<Integer> spinner) {
        if (spinner.getValueFactory() == null) {
            return;
        }
        String text = spinner.getEditor().getText();
        try {
            spinner.getValueFactory().setValue(Integer.parseInt(text));
        } catch (NumberFormatException ignored) {
            spinner.getEditor().setText(String.valueOf(spinner.getValue()));
        }
    }

    /** Chỉ hiện khối ngày giờ khi radio "tùy chọn" đang được chọn. */
    private void syncTimeModeVisibility() {
        boolean customStart = startCustomRadio.isSelected();
        boolean customEnd = endCustomRadio.isSelected();
        startCustomBox.setVisible(customStart);
        startCustomBox.setManaged(customStart);
        endCustomBox.setVisible(customEnd);
        endCustomBox.setManaged(customEnd);
    }

    /**
     * Kiểm tra vật phẩm, tiêu đề, mốc thời gian và anti-sniping rồi gửi tạo phiên.
     * Bắt đầu được phép lệch tối đa 5 giây so với "bây giờ" để tránh lệch đồng hồ.
     */
    @FXML
    public void handleCreateAuction() {
        ItemDTO selectedItem = itemComboBox.getValue();
        String title = titleField.getText();
        LocalDateTime start = resolveStartTime();
        LocalDateTime end = resolveEndTime(start);

        if (selectedItem == null || title.isBlank() || start == null || end == null) {
            showError("Vui lòng điền đầy đủ thông tin bắt buộc.");
            return;
        }
        if (start.isBefore(LocalDateTime.now().minusSeconds(5))) {
            showError("Thời gian bắt đầu không được trước hiện tại.");
            return;
        }
        if (!end.isAfter(start)) {
            showError("Thời gian kết thúc không hợp lệ.");
            return;
        }

        // Mặc định: bid trong 60 giây cuối thì gia hạn thêm 120 giây. Ô trống giữ mặc định.
        int windowSec = 60;
        int extensionSec = 120;
        try {
            if (!antiSnipingWindowField.getText().trim().isEmpty()) {
                windowSec = Integer.parseInt(antiSnipingWindowField.getText().trim());
            }
            if (!antiSnipingExtensionField.getText().trim().isEmpty()) {
                extensionSec = Integer.parseInt(antiSnipingExtensionField.getText().trim());
            }
        } catch (NumberFormatException ex) {
            showError("Anti-Sniping phải là số nguyên.");
            return;
        }

        String token = ClientSession.getInstance().getToken();
        if (token == null || token.isEmpty()) {
            showError("Vui lòng đăng nhập lại.");
            return;
        }

        statusLabel.setText("Đang xử lý...");
        statusLabel.setStyle("-fx-text-fill: #f39c12;");

        final int finalWindowSec = windowSec;
        final int finalExtSec = extensionSec;

        new Thread(() -> {
            try {
                auctionClient.createAuction(
                        token,
                        selectedItem.getId(),
                        start,
                        end,
                        title,
                        descArea.getText(),
                        finalWindowSec,
                        finalExtSec);

                Platform.runLater(() -> {
                    statusLabel.setText("Tạo phiên đấu giá thành công.");
                    statusLabel.setStyle("-fx-text-fill: #2ecc71;");
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi từ server: " + e.getMessage()));
            }
        }).start();
    }

    /** Hiện lỗi đỏ trên nhãn trạng thái. */
    private void showError(String msg) {
        statusLabel.setText("Lỗi: " + msg);
        statusLabel.setStyle("-fx-text-fill: #e94560;");
    }

    /** Preview khi chưa chọn vật phẩm. */
    private void renderEmptyItemPreview() {
        previewContentBox.getChildren().clear();
        addPreviewFullWidth("Trạng thái", "Chưa chọn sản phẩm.");
    }

    /** Vẽ tên, giá, loại, tình trạng, thông số theo loại và mô tả. */
    private void renderItemPreview(ItemDTO item) {
        previewContentBox.getChildren().clear();
        List<VBox> fields = new ArrayList<>();
        fields.add(createPreviewField("Tên", item.getName(), true));
        fields.add(createPreviewMoneyField("Giá khởi điểm", item.getStartingPrice()));
        fields.add(createPreviewField("Loại", valueOrEmpty(item.getType()), false));
        fields.add(createPreviewField("Tình trạng", valueOrEmpty(item.getCondition()), false));
        fields.addAll(createTypeSpecificPreviewFields(item));
        addPreviewRows(fields);

        String description = item.getDescription();
        if (description != null && !description.isBlank()) {
            addPreviewFullWidth("Chi tiết", description.trim());
        }
    }

    /** Thông số riêng của ELECTRONICS, ART hoặc VEHICLE. */
    private List<VBox> createTypeSpecificPreviewFields(ItemDTO item) {
        List<VBox> result = new ArrayList<>();
        Map<String, Object> fields = item.getExtraFields();
        if (fields == null || item.getType() == null) {
            return result;
        }

        switch (item.getType()) {
            case ELECTRONICS -> {
                result.add(createPreviewField("Brand", fieldValue(fields, "brand"), false));
                result.add(createPreviewField("Warranty", withUnit(fieldValue(fields, "warrantyMonths"), "tháng"), false));
            }
            case ART -> {
                result.add(createPreviewField("Artist", fieldValue(fields, "artist"), false));
                result.add(createPreviewField("Year created", fieldValue(fields, "yearCreated"), false));
                result.add(createPreviewField("Medium", fieldValue(fields, "medium"), false));
            }
            case VEHICLE -> {
                result.add(createPreviewField("Brand", fieldValue(fields, "brand"), false));
                result.add(createPreviewField("Manufacture year", fieldValue(fields, "manufactureYear"), false));
                result.add(createPreviewField("Odo", withUnit(fieldValue(fields, "odo"), "km"), false));
            }
        }
        return result;
    }

    /** Xếp các ô preview thành hàng hai cột. */
    private void addPreviewRows(List<VBox> fields) {
        for (int i = 0; i < fields.size(); i += 2) {
            HBox row = new HBox(24);
            row.setMaxWidth(Double.MAX_VALUE);
            row.getChildren().add(fields.get(i));
            HBox.setHgrow(fields.get(i), Priority.ALWAYS);

            if (i + 1 < fields.size()) {
                row.getChildren().add(fields.get(i + 1));
                HBox.setHgrow(fields.get(i + 1), Priority.ALWAYS);
            }
            previewContentBox.getChildren().add(row);
        }
    }

    /** Một ô "nhãn: giá trị"; highlight thì chữ đậm hơn. */
    private VBox createPreviewField(String name, String value, boolean highlight) {
        VBox fieldBox = new VBox();
        fieldBox.setMaxWidth(Double.MAX_VALUE);
        fieldBox.setPrefWidth(1);

        Label fieldLabel = new Label(name + ": " + value);
        fieldLabel.getStyleClass().add(highlight ? "text-primary" : "text-detail");
        fieldLabel.setWrapText(true);
        fieldLabel.setMaxWidth(Double.MAX_VALUE);
        fieldLabel.setStyle(highlight
                ? "-fx-font-size: 14px; -fx-font-weight: bold;"
                : "-fx-font-size: 13px;");

        fieldBox.getChildren().add(fieldLabel);
        return fieldBox;
    }

    /** Ô tiền rút gọn trên preview, tooltip giữ số đầy đủ. */
    private VBox createPreviewMoneyField(String name, double amount) {
        VBox fieldBox = createPreviewField(name, CurrencyFormatter.format(amount), true);
        if (!fieldBox.getChildren().isEmpty() && fieldBox.getChildren().get(0) instanceof Label label) {
            CurrencyFormatter.installTooltip(label, name + ": " + CurrencyFormatter.formatFull(amount));
        }
        return fieldBox;
    }

    /** Thêm một dòng preview chiếm hết chiều ngang, dùng cho mô tả dài. */
    private void addPreviewFullWidth(String name, String value) {
        VBox fieldBox = createPreviewField(name, value, false);
        previewContentBox.getChildren().add(fieldBox);
    }

    /** Đọc một extraField và format số nếu cần. */
    private String fieldValue(Map<String, Object> fields, String key) {
        return formatFieldValue(fields.get(key));
    }

    /** Ghép đơn vị (tháng, km) khi giá trị không rỗng. */
    private String withUnit(String value, String unit) {
        return value == null || value.isBlank() ? "" : value + " " + unit;
    }

    /** Số nguyên bỏ .0; giá trị khác giữ toString. */
    private String formatFieldValue(Object value) {
        if (value instanceof Number number) {
            double doubleValue = number.doubleValue();
            if (doubleValue == Math.rint(doubleValue)) {
                return String.format("%.0f", doubleValue);
            }
        }
        return value != null ? value.toString() : "";
    }

    /** Null thành chuỗi rỗng. */
    private String valueOrEmpty(Object value) {
        return value != null ? value.toString() : "";
    }

    /** Tự điền "Đấu giá: tên vật phẩm" khi ô trống hoặc vẫn là tiêu đề tự sinh trước đó. */
    private void syncTitleWithSelectedItem(ItemDTO item) {
        String currentTitle = titleField.getText() != null ? titleField.getText().trim() : "";
        String nextAutoTitle = "Đấu giá: " + item.getName();
        // Chỉ tự điền khi ô trống hoặc vẫn là tiêu đề tự sinh của vật phẩm trước.
        if (currentTitle.isEmpty() || currentTitle.equals(lastAutoTitle)) {
            titleField.setText(nextAutoTitle);
            lastAutoTitle = nextAutoTitle;
        }
    }

    /** Màn "vật phẩm của tôi" truyền id để chọn sẵn sau khi danh sách tải xong. */
    public void setInitialItemId(String itemId) {
        this.initialItemId = itemId;
        selectInitialItemIfLoaded();
    }

    /** Tải vật phẩm của seller và chỉ đưa những cái tạo phiên được vào combo. */
    private void loadMyItems() {
        String token = ClientSession.getInstance().getToken();
        if (token == null || token.isEmpty()) {
            System.err.println("[CreateAuction] Chưa đăng nhập, không thể tải item.");
            return;
        }

        new Thread(() -> {
            try {
                List<ItemDTO> items = itemClient.getMyItems(token);
                List<ItemDTO> auctionableItems = filterAuctionableItems(token, items);
                Platform.runLater(() -> {
                    itemComboBox.setItems(FXCollections.observableArrayList(auctionableItems));
                    boolean selectedInitialItem = selectInitialItemIfLoaded();
                    if (items.isEmpty()) {
                        statusLabel.setText("Bạn chưa có sản phẩm nào. Hãy tạo sản phẩm trước.");
                        statusLabel.setStyle("-fx-text-fill: #f39c12;");
                    } else if (auctionableItems.isEmpty()) {
                        statusLabel.setText("Không có vật phẩm đã duyệt sẵn sàng để tạo phiên.");
                        statusLabel.setStyle("-fx-text-fill: #f39c12;");
                    } else if (initialItemId != null && !selectedInitialItem) {
                        statusLabel.setText("Vật phẩm này chưa được duyệt hoặc đang có phiên hoạt động.");
                        statusLabel.setStyle("-fx-text-fill: #f39c12;");
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Không thể tải danh sách sản phẩm: " + e.getMessage()));
                System.err.println("[CreateAuction] Lỗi tải item: " + e.getMessage());
            }
        }).start();
    }

    /** Chỉ vật phẩm đã duyệt và không còn phiên OPEN/RUNNING/WAITING_PAYMENT. */
    private List<ItemDTO> filterAuctionableItems(String token, List<ItemDTO> items) {
        List<ItemDTO> result = new ArrayList<>();
        if (items == null) {
            return result;
        }

        for (ItemDTO item : items) {
            if (item.getApprovalStatus() == ItemApprovalStatus.APPROVED
                    && !hasBlockingAuction(token, item)) {
                result.add(item);
            }
        }
        return result;
    }

    /** Có phiên OPEN, RUNNING hoặc WAITING_PAYMENT thì vật phẩm chưa được tạo phiên mới. */
    private boolean hasBlockingAuction(String token, ItemDTO item) {
        try {
            List<AuctionDTO> auctions = itemClient.getItemAuctionHistory(token, item.getId());
            return auctions != null && auctions.stream()
                    .anyMatch(auction -> AuctionViewFilter.isBlockingStatus(auction.getStatus()));
        } catch (Exception e) {
            return false;
        }
    }

    /** Chọn vật phẩm initialItemId nếu nó có trong combo; không có thì trả false. */
    private boolean selectInitialItemIfLoaded() {
        if (initialItemId == null || itemComboBox.getItems() == null) {
            return false;
        }
        for (ItemDTO item : itemComboBox.getItems()) {
            if (initialItemId.equals(item.getId())) {
                itemComboBox.getSelectionModel().select(item);
                return true;
            }
        }
        return false;
    }
}
