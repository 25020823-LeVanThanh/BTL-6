package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.WalletClient;
import com.auctionuet.protocol.dto.response.user.UserDTO;
import com.auctionuet.protocol.dto.response.wallet.WalletResponseDTO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Popup nạp tiền: hiện thông tin chuyển khoản và mã QR VietQR.
 * Nội dung chuyển khoản = tên đăng nhập + mã 5 ký tự, để đối soát giao dịch.
 * Bấm xác nhận thì gọi API cộng tiền vào ví.
 */
public class PaymentCheckoutController {
    // Thông tin ngân hàng nhận thanh toán (MB Bank thực tế)
    private static final String BANK_ID = "MB";
    private static final String ACCOUNT_NO = "0090090910909";
    private static final String ACCOUNT_NAME = "CONG TY AUCTIONUET";
    private static final String QR_TEMPLATE = "compact";

    private static final double CHECKOUT_GAP = 24.0;
    private static final double CHECKOUT_STACK_WIDTH = 820.0;
    private static final double CHECKOUT_RIGHT_WIDTH = 340.0;

    @FXML private FlowPane checkoutFlow;
    @FXML private Label checkoutTitleLabel;

    private final WalletClient walletClient = new WalletClient();
    private double depositAmount;
    private String confirmCode; // Mã xác nhận giao dịch gồm 5 ký tự ngẫu nhiên
    private VBox checkoutLeftColumn;
    private VBox checkoutActionCard;
    private Label checkoutMessageLabel;
    private Button btnConfirmCheckout;
    private Consumer<WalletResponseDTO> walletDepositSuccessCallback;

    /** Khi popup đổi rộng thì xếp lại hai cột thông tin và QR. */
    @FXML
    public void initialize() {
        checkoutFlow.widthProperty().addListener((obs, oldWidth, newWidth) ->
                syncCheckoutWidth(newWidth.doubleValue()));
    }

    /** Nhận số tiền từ màn ví, tạo mã đối soát rồi vẽ form nạp. */
    public void setDepositData(double amount, Consumer<WalletResponseDTO> walletDepositSuccessCallback) {
        this.depositAmount = amount;
        this.walletDepositSuccessCallback = walletDepositSuccessCallback;
        this.confirmCode = generateRandomConfirmCode(); // Tạo mã xác thực ngẫu nhiên 5 ký tự
        renderCheckout();
        Platform.runLater(() -> syncCheckoutWidth(checkoutFlow.getWidth()));
    }

    // Tạo mã xác thực ngẫu nhiên 5 ký tự (chữ in hoa và số)
    private String generateRandomConfirmCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random rnd = new Random();
        StringBuilder sb = new StringBuilder(5);
        for (int i = 0; i < 5; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    /** Dựng cột trái (thông tin) và cột phải (QR + nút xác nhận). */
    private void renderCheckout() {
        if (depositAmount <= 0) {
            return;
        }

        checkoutTitleLabel.setText("💳 Nạp tiền");
        checkoutLeftColumn = createDepositInfoCard();
        checkoutActionCard = createDepositActionCard();
        checkoutFlow.setHgap(CHECKOUT_GAP);
        checkoutFlow.setVgap(CHECKOUT_GAP);
        checkoutFlow.getStyleClass().add("payment-checkout-flow");
        checkoutFlow.getChildren().setAll(checkoutLeftColumn, checkoutActionCard);
        syncCheckoutWidth(checkoutFlow.getWidth());
    }

    /** Cửa sổ hẹp thì xếp hai cột thành một cột; cửa sổ rộng thì cột QR cố định 340px. */
    private void syncCheckoutWidth(double availableWidth) {
        if (checkoutLeftColumn == null || checkoutActionCard == null) {
            return;
        }

        double width = availableWidth > 0 ? availableWidth : checkoutFlow.getPrefWidth();
        boolean stacked = width < CHECKOUT_STACK_WIDTH;
        double leftWidth = stacked
                ? width
                : Math.max(360, width - CHECKOUT_RIGHT_WIDTH - CHECKOUT_GAP);
        double rightWidth = stacked ? width : CHECKOUT_RIGHT_WIDTH;

        applyRegionWidth(checkoutLeftColumn, leftWidth);
        applyRegionWidth(checkoutActionCard, rightWidth);
    }

    /** Gán min, pref và max bằng nhau để FlowPane không tự kéo cột. */
    private void applyRegionWidth(Region region, double width) {
        region.setMinWidth(width);
        region.setPrefWidth(width);
        region.setMaxWidth(width);
    }

    /** Tài khoản, nội dung chuyển khoản và số tiền cần nạp. */
    private VBox createDepositInfoCard() {
        VBox card = new VBox(12);
        card.getStyleClass().addAll("payment-info-card", "payment-checkout-column");

        Label title = new Label("Thông tin nạp tiền");
        title.getStyleClass().add("payment-card-title");

        UserDTO currentUser = ClientSession.getInstance().getCurrentUser();
        card.getChildren().addAll(
                title,
                createInfoRow("Tài khoản", usernameOf(currentUser)),
                createInfoRow("Nội dung chuyển khoản", depositReference()),
                createDivider(),
                createAmountDueRow(
                        "Số tiền nạp",
                        CurrencyFormatter.format(depositAmount),
                        CurrencyFormatter.formatFull(depositAmount)));
        return card;
    }

    /** Một hàng nhãn bên trái, giá trị bên phải, không tooltip. */
    private HBox createInfoRow(String labelText, String valueText) {
        return createInfoRow(labelText, valueText, null);
    }

    /** Hàng thông tin; tooltip dùng khi số tiền bị rút gọn trên nhãn. */
    private HBox createInfoRow(String labelText, String valueText, String tooltipText) {
        HBox row = new HBox(12);
        row.setAlignment(Pos.CENTER_LEFT);
        Label label = new Label(labelText);
        label.getStyleClass().add("payment-info-label");
        Label value = new Label(valueText);
        value.getStyleClass().add("payment-info-value");
        value.setWrapText(true);
        if (tooltipText != null && !tooltipText.isBlank()) {
            CurrencyFormatter.installTooltip(value, tooltipText);
        }
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        row.getChildren().addAll(label, spacer, value);
        return row;
    }

    /** Hàng số tiền, thêm class CSS để nhấn mạnh tổng. */
    private HBox createAmountDueRow(String labelText, String valueText, String tooltipText) {
        HBox row = createInfoRow(labelText, valueText, tooltipText);
        row.getStyleClass().add("payment-total-row");
        return row;
    }

    /** Đường kẻ ngang 1px ngăn phần thông tin và số tiền. */
    private Region createDivider() {
        Region divider = new Region();
        divider.getStyleClass().add("payment-divider");
        divider.setMinHeight(1);
        divider.setPrefHeight(1);
        return divider;
    }

    /** Cột phải: QR, thông báo, nút xác nhận đã chuyển và nút đóng popup. */
    private VBox createDepositActionCard() {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_CENTER);
        card.getStyleClass().add("payment-action-card");
        card.getChildren().addAll(createDepositQrContent());

        checkoutMessageLabel = new Label("");
        checkoutMessageLabel.getStyleClass().add("payment-message");
        checkoutMessageLabel.setWrapText(true);

        btnConfirmCheckout = new Button("Xác nhận đã chuyển khoản");
        btnConfirmCheckout.getStyleClass().addAll("button", "payment-confirm-button");
        btnConfirmCheckout.setMaxWidth(Double.MAX_VALUE);
        btnConfirmCheckout.setOnAction(e -> confirmWalletDeposit());

        Button btnBack = new Button("Quay về");
        btnBack.getStyleClass().add("payment-back-button");
        btnBack.setMaxWidth(Double.MAX_VALUE);
        btnBack.setOnAction(e -> closePopup());

        card.getChildren().addAll(checkoutMessageLabel, btnConfirmCheckout, btnBack);
        return card;
    }

    /**
     * Ghép URL ảnh VietQR: ngân hàng, số tài khoản, số tiền nguyên và nội dung chuyển khoản.
     * Ảnh tải nền để không khóa luồng giao diện.
     */
    private List<Node> createDepositQrContent() {
        Label title = new Label("Quét mã QR để nạp tiền");
        title.getStyleClass().add("payment-card-title");

        // Tạo URL gọi Quicklink API của VietQR.io
        String url = "";
        try {
            String encodedBankId = URLEncoder.encode(BANK_ID, StandardCharsets.UTF_8.name());
            String encodedAccountNo = URLEncoder.encode(ACCOUNT_NO, StandardCharsets.UTF_8.name());
            String encodedTemplate = URLEncoder.encode(QR_TEMPLATE, StandardCharsets.UTF_8.name());
            
            // Số tiền nạp phải được chuyển về chuỗi số nguyên để quét đúng
            String amountStr = String.valueOf((int) depositAmount);
            String encodedAmount = URLEncoder.encode(amountStr, StandardCharsets.UTF_8.name());
            
            String reference = depositReference();
            String encodedAddInfo = URLEncoder.encode(reference, StandardCharsets.UTF_8.name());
            String encodedAccountName = URLEncoder.encode(ACCOUNT_NAME, StandardCharsets.UTF_8.name());

            url = String.format("https://img.vietqr.io/image/%s-%s-%s.png?amount=%s&addInfo=%s&accountName=%s",
                    encodedBankId, encodedAccountNo, encodedTemplate,
                    encodedAmount, encodedAddInfo, encodedAccountName);
        } catch (Exception e) {
            // Trường hợp dự phòng nếu encoding gặp lỗi (hầu như không thể xảy ra với UTF-8)
            url = "https://img.vietqr.io/image/" + BANK_ID + "-" + ACCOUNT_NO + "-" + QR_TEMPLATE + ".png"
                    + "?amount=" + (int) depositAmount 
                    + "&addInfo=" + depositReference()
                    + "&accountName=" + ACCOUNT_NAME;
        }

        ImageView qrImageView = new ImageView();
        qrImageView.setFitWidth(220);
        qrImageView.setFitHeight(220);
        qrImageView.setPreserveRatio(true);
        try {
            // Tải ảnh bất đồng bộ (background loading = true) để tránh nghẽn luồng JavaFX UI
            Image qrImage = new Image(url, true);
            qrImageView.setImage(qrImage);
        } catch (Exception e) {
            System.err.println("Lỗi tải ảnh QR từ VietQR: " + e.getMessage());
        }

        HBox qrHolder = new HBox(qrImageView);
        qrHolder.setAlignment(Pos.CENTER);
        qrHolder.setMaxWidth(Double.MAX_VALUE);

        Label amount = new Label();
        CurrencyFormatter.setMoneyText(amount, "Số tiền nạp: ", depositAmount);
        amount.getStyleClass().add("payment-action-amount");
        amount.setWrapText(true);

        Label ref = new Label("Nội dung chuyển khoản: " + depositReference());
        ref.getStyleClass().add("text-secondary");
        ref.setWrapText(true);

        return List.of(title, qrHolder, amount, ref);
    }

    /** Gọi API cộng tiền; thành công thì callback cập nhật ví rồi đóng popup. */
    private void confirmWalletDeposit() {
        String token = ClientSession.getInstance().getToken();
        btnConfirmCheckout.setDisable(true);
        checkoutMessageLabel.setText("Đang xác nhận nạp tiền...");
        checkoutMessageLabel.getStyleClass().removeAll("success", "error");

        new Thread(() -> {
            try {
                WalletResponseDTO wallet = walletClient.deposit(token, depositAmount);
                Platform.runLater(() -> {
                    if (walletDepositSuccessCallback != null) {
                        walletDepositSuccessCallback.accept(wallet);
                    }
                    closePopup();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    checkoutMessageLabel.setText("Lỗi nạp tiền: " + e.getMessage());
                    checkoutMessageLabel.getStyleClass().removeAll("success", "error");
                    checkoutMessageLabel.getStyleClass().add("error");
                    btnConfirmCheckout.setDisable(false);
                });
            }
        }).start();
    }

    /** Đóng Stage của popup nạp tiền. */
    private void closePopup() {
        if (checkoutFlow != null && checkoutFlow.getScene() != null
                && checkoutFlow.getScene().getWindow() instanceof Stage stage) {
            stage.close();
        }
    }

    /** Username hiện trên phiếu nạp. */
    private String usernameOf(UserDTO user) {
        return user != null && user.getUsername() != null ? user.getUsername() : "Chưa rõ";
    }

    /** Rút id dài còn 8 ký tự đầu để hiện gọn nếu cần. */
    private String shortId(String id) {
        if (id == null || id.isBlank()) {
            return "UNKNOWN";
        }
        return id.length() <= 8 ? id : id.substring(0, 8);
    }

    /** Nội dung CK = username + mã 5 ký tự, dùng để đối soát giao dịch. */
    private String depositReference() {
        UserDTO user = ClientSession.getInstance().getCurrentUser();
        String username = user != null && user.getUsername() != null ? user.getUsername() : "UNKNOWN";
        return username + " " + (confirmCode != null ? confirmCode : "");
    }
}
