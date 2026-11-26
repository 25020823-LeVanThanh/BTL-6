package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.AuctionClient;
import com.auctionuet.client.network.BidClient;
import com.auctionuet.client.network.ServerConnection;
import com.auctionuet.client.network.WalletClient;
import com.auctionuet.protocol.PushActionType;
import com.auctionuet.protocol.PushMessage;
import com.auctionuet.protocol.dto.push.PushEvents;
import com.auctionuet.protocol.dto.response.auction.AuctionDTO;
import com.auctionuet.protocol.dto.response.bid.AutoBidConfigDTO;
import com.auctionuet.protocol.dto.response.bid.BidDTO;
import com.auctionuet.protocol.dto.response.user.UserDTO;
import com.auctionuet.protocol.dto.response.wallet.WalletResponseDTO;
import com.auctionuet.protocol.enums.AutoBidStatus;
import com.auctionuet.protocol.enums.BidType;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.geometry.Insets;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.File;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import com.auctionuet.client.util.AudioRecorder;
import com.auctionuet.client.util.CurrencyInputHelper;
import com.auctionuet.client.network.GeminiVoiceServiceClient;

/**
 * Phòng đặt giá của bidder: giá hiện tại, đồng hồ, lịch sử, biểu đồ,
 * đặt giá tay, Auto-Bid và đặt giá bằng giọng nói.
 * Tin push cập nhật giá, gia hạn anti-sniping, kết thúc và hủy phiên.
 */
public class BiddingController {
    private static final DateTimeFormatter DISPLAY_TIME = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    /** Phải dẫn đầu liên tục khoảng 3 giây mới được bật lại Auto-Bid sau khi hết hiệu lực. */
    private static final double AUTO_BID_STABILITY_CHECK_SECONDS = 3.2;
    private static final String AUTO_BID_INEFFECTIVE_NOTICE = "Auto-Bid đã hết hiệu lực vì giá hiện tại vượt trần. Hãy dẫn đầu lại, giữ vị trí 3 giây, rồi bật Auto-Bid mới.";
    private static final String AUTO_BID_REGAIN_LEAD_HINT = "Bạn đang dẫn đầu trở lại. Giữ vị trí đủ 3 giây rồi bật lại Auto-Bid.";
    private static final String AUTO_BID_REENABLE_HINT = "Bạn đang dẫn đầu ổn định. Có thể bật Auto-Bid mới.";

    @FXML
    private Label titleLabel;
    @FXML
    private Label priceLabel;
    @FXML
    private Label leaderLabel;
    @FXML
    private Label timeLeftLabel;
    @FXML
    private Label sellerLabel;
    @FXML
    private Label statusBadge;
    @FXML
    private Label cancelReasonLabel;

    @FXML
    private Label depositLabel;
    @FXML
    private Label balanceLabel;

    @FXML
    private TextField bidAmountField;
    @FXML
    private Button btnMic;
    @FXML
    private Button placeBidBtn;
    @FXML
    private Label bidStatusLabel;

    @FXML
    private TextField maxBidField;
    @FXML
    private TextField incrementField;
    @FXML
    private Button enableAutoBidBtn;
    @FXML
    private Button cancelAutoBidBtn;
    @FXML
    private Label autoBidStatusLabel;

    @FXML
    private Button btnVoiceSettings;
    @FXML
    private Label voiceBidStatusLabel;

    @FXML
    private LineChart<Number, Number> bidPriceChart;
    @FXML
    private ListView<String> bidHistoryList;

    /** Id phiên đang mở trong phòng đặt giá. */
    private String currentAuctionId;
    /** Mô tả phiên, hiện trong hộp xác nhận cọc. */
    private String auctionDescription;
    private final BidClient bidClient = new BidClient();
    private final AuctionClient auctionClient = new AuctionClient();
    private final WalletClient walletClient = new WalletClient();
    private final XYChart.Series<Number, Number> bidPriceSeries = new XYChart.Series<>();

    /** Đồng hồ đếm ngược tới endDateTime. */
    private javafx.animation.Timeline countdownTimeline;
    /** Giờ kết thúc hiện tại, có thể bị anti-sniping đẩy muộn hơn. */
    private LocalDateTime endDateTime;
    /** Số tiền cọc của phiên, mặc định 10% giá khởi điểm nếu server không gửi. */
    private double auctionDepositAmount;
    /** User đã đồng ý cọc và đặt giá ít nhất một lần trong phiên này. */
    private boolean currentUserDeposited;
    /** Trạng thái Auto-Bid lần kiểm tra trước, để chỉ báo khi vừa chuyển sang hết hiệu lực. */
    private AutoBidStatus lastAutoBidStatus;
    /** Đã nhận ít nhất một lần check Auto-Bid, tránh báo nhầm lúc mới vào phòng. */
    private boolean autoBidStateLoaded;
    /** Có bid của người khác vừa xảy ra, dùng để quyết định có ghi thông báo mất hiệu lực không. */
    private boolean pendingAutoBidLossNotice;
    private String currentLeaderId;
    /** Thời điểm user bắt đầu giữ vị trí dẫn đầu, để đếm đủ khoảng 3 giây. */
    private LocalDateTime currentUserLeaderObservedAt;
    private javafx.animation.PauseTransition delayedAutoBidCheck;

    private boolean isRecording = false;
    private AudioRecorder audioRecorder;
    private File tempVoiceFile;
    private final GeminiVoiceServiceClient voiceServiceClient = new GeminiVoiceServiceClient();
    private javafx.event.EventHandler<KeyEvent> keyHandler;

    /** Chuẩn bị biểu đồ giá và ba ô tiền (bid tay, trần auto-bid, bước tăng). */
    @FXML
    public void initialize() {
        setupBidPriceChart();
        CurrencyInputHelper.setupCurrencyInput(bidAmountField);
        CurrencyInputHelper.setupCurrencyInput(maxBidField);
        CurrencyInputHelper.setupCurrencyInput(incrementField);
    }

    /** Màn trước truyền id phiên: tải chi tiết, ví, lịch sử bid, subscribe push và kiểm tra Auto-Bid. */
    public void setAuctionId(String auctionId) {
        this.currentAuctionId = auctionId;

        loadAuctionDetail();
        loadWalletInfo();
        loadBidHistory();
        subscribeToAuction(auctionId);
        ServerConnection.getInstance().setPushListener(this::onPushMessage);
        checkAutoBidState();

        this.tempVoiceFile = new File(System.getProperty("java.io.tmpdir"), "uet_voice_bid.wav");
    }

    /** Điền tiêu đề, seller, giá, cọc, người dẫn đầu và bắt đầu đồng hồ; phiên hủy thì khóa form. */
    private void loadAuctionDetail() {
        String token = ClientSession.getInstance().getToken();
        new Thread(() -> {
            try {
                AuctionDTO auction = auctionClient.getAuctionDetail(token, currentAuctionId);
                Platform.runLater(() -> {
                    titleLabel.setText(auction.getTitle());
                    sellerLabel.setText("Seller: " + usernameOf(auction.getSeller()));
                    CurrencyFormatter.setMoneyText(priceLabel, auction.getCurrentPrice());
                    auctionDescription = auction.getDescription();
                    // Server chưa gửi số cọc thì lấy 10% giá khởi điểm.
                    auctionDepositAmount = auction.getDepositAmount() > 0
                            ? auction.getDepositAmount()
                            : auction.getItem() != null ? auction.getItem().getStartingPrice() * 0.10 : 0;
                    currentUserDeposited = auction.isCurrentUserDeposited();
                    renderAuctionDeposit();

                    if (auction.getCurrentHighestBid() != null) {
                        updateCurrentLeader(auction.getCurrentHighestBid().getBidder());
                    }

                    if (auction.getEndTime() != null) {
                        endDateTime = auction.getEndTime();
                        startCountdown();
                    }
                    if (auction.getStatus() == com.auctionuet.protocol.enums.AuctionStatus.CANCELED) {
                        showCanceledState(auction.getCanceledReason());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> titleLabel.setText("Lỗi tải chi tiết: " + e.getMessage()));
            }
        }).start();
    }

    /** Lấy số dư khả dụng để bidder biết còn đủ tiền cọc hay trả giá không. */
    private void loadWalletInfo() {
        String token = ClientSession.getInstance().getToken();
        if (token == null)
            return;

        new Thread(() -> {
            try {
                WalletResponseDTO wallet = walletClient.getWallet(token);
                Platform.runLater(() -> renderWallet(wallet));
            } catch (Exception e) {
                Platform.runLater(() -> balanceLabel.setText("Lỗi tải ví"));
            }
        }).start();
    }

    /** Đổ lịch sử bid mới nhất lên trên và vẽ từng điểm giá; bid cuối xác định người dẫn đầu. */
    private void loadBidHistory() {
        String token = ClientSession.getInstance().getToken();
        new Thread(() -> {
            try {
                List<BidDTO> history = bidClient.getBidHistory(token, currentAuctionId);
                Platform.runLater(() -> {
                    bidHistoryList.getItems().clear();
                    bidPriceSeries.getData().clear();
                    if (history != null && !history.isEmpty()) {
                        for (BidDTO entry : history) {
                            // Thêm vào đầu danh sách để hiển thị mới nhất lên trên
                            bidHistoryList.getItems().add(0, formatBid(entry));
                            appendBidPricePoint(entry);
                        }

                        BidDTO latest = history.get(history.size() - 1);
                        updateCurrentLeader(latest.getBidder());
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> bidHistoryList.getItems().add("Lỗi tải lịch sử"));
            }
        }).start();
    }

    /**
     * Push của đúng phiên này:
     * bid mới, gia hạn anti-sniping, kết thúc, hoặc bị hủy.
     * Bid tay của người khác được kiểm tra lại Auto-Bid sau vài giây.
     */
    private void onPushMessage(PushMessage push) {
        if (push == null || push.getPushType() == null)
            return;

        Platform.runLater(() -> {
            if (push.getPushType() == PushActionType.BID_UPDATE) {
                PushEvents.BidUpdatePush data = push.getDataAs(PushEvents.BidUpdatePush.class);
                if (data == null || !currentAuctionId.equals(data.getAuctionId()))
                    return;
                BidDTO bid = data.getBid();
                if (bid == null)
                    return;

                CurrencyFormatter.setMoneyText(priceLabel, bid.getAmount());
                updateCurrentLeader(bid.getBidder(), true);
                bidHistoryList.getItems().add(0, "Vừa xong - " + formatBid(bid));
                appendBidPricePoint(bid);
                if (isCurrentUser(bid.getBidder())) {
                    currentUserDeposited = true;
                    renderAuctionDeposit();
                    loadWalletInfo();
                }
                // Bid của người khác có thể làm Auto-Bid mất hiệu lực; kiểm tra lại sau vài giây.
                boolean externalBid = !isCurrentUser(bid.getBidder());
                pendingAutoBidLossNotice = externalBid;
                checkAutoBidState();
                if (externalBid && bid.getBidType() == BidType.MANUAL) {
                    scheduleDelayedAutoBidCheck();
                }
                return;
            }

            // Anti-sniping: server đẩy giờ kết thúc mới, đồng hồ đếm lại từ mốc đó.
            if (push.getPushType() == PushActionType.AUCTION_EXTENDED) {
                PushEvents.AuctionExtendedPush data = push.getDataAs(PushEvents.AuctionExtendedPush.class);
                if (data == null || !currentAuctionId.equals(data.getAuctionId()))
                    return;

                try {
                    endDateTime = data.getNewEndTime();
                    startCountdown();
                } catch (Exception ignored) {
                }
                return;
            }

            if (push.getPushType() == PushActionType.AUCTION_ENDED) {
                PushEvents.AuctionEndedPush data = push.getDataAs(PushEvents.AuctionEndedPush.class);
                if (data == null || !currentAuctionId.equals(data.getAuctionId()))
                    return;

                if (countdownTimeline != null)
                    countdownTimeline.stop();
                timeLeftLabel.setText("Đã kết thúc");
                statusBadge.setText("ĐÃ KẾT THÚC");
                statusBadge.getStyleClass().remove("status-badge-running");
                statusBadge.getStyleClass().add("status-badge-finished");
                leaderLabel.setText("Người thắng: " + usernameOf(data.getWinner())
                        + " (Giá: " + CurrencyFormatter.format(data.getFinalPrice()) + ")");
                currentUserDeposited = currentUserDeposited && isCurrentUser(data.getWinner());
                renderAuctionDeposit();
                loadWalletInfo();

                bidAmountField.setDisable(true);
                placeBidBtn.setDisable(true);
                enableAutoBidBtn.setDisable(true);
                cancelAutoBidBtn.setDisable(true);
                return;
            }

            if (push.getPushType() == PushActionType.AUCTION_CANCELED) {
                PushEvents.AuctionCanceledPush data = push.getDataAs(PushEvents.AuctionCanceledPush.class);
                if (data == null || !currentAuctionId.equals(data.getAuctionId()))
                    return;

                showCanceledState(data.getReason());
                currentUserDeposited = false;
                renderAuctionDeposit();
                loadWalletInfo();
            }
        });
    }

    /** Dừng đồng hồ, hiện lý do hủy và khóa mọi ô đặt giá lẫn Auto-Bid. */
    private void showCanceledState(String reason) {
        if (countdownTimeline != null)
            countdownTimeline.stop();
        timeLeftLabel.setText("Đã hủy");
        statusBadge.setText("ĐÃ HỦY");
        statusBadge.getStyleClass().remove("status-badge-running");
        statusBadge.getStyleClass().add("status-badge-canceled");
        cancelReasonLabel.setText("Lý do hủy: " + (reason != null ? reason : "Chưa rõ"));
        cancelReasonLabel.setVisible(true);
        cancelReasonLabel.setManaged(true);
        bidAmountField.setDisable(true);
        placeBidBtn.setDisable(true);
        maxBidField.setDisable(true);
        incrementField.setDisable(true);
        enableAutoBidBtn.setDisable(true);
        cancelAutoBidBtn.setDisable(true);
    }

    /** Đăng ký kênh push của phiên; lỗi hiện trên nhãn trạng thái bid. */
    private void subscribeToAuction(String auctionId) {
        new Thread(() -> {
            try {
                bidClient.subscribe(ClientSession.getInstance().getToken(), auctionId);
            } catch (Exception e) {
                Platform.runLater(() -> bidStatusLabel.setText("Lỗi subscribe: " + e.getMessage()));
            }
        }).start();
    }

    /** Dừng đồng hồ, hủy listener và xóa file ghi âm tạm khi rời phòng. */
    public void cleanup() {
        if (countdownTimeline != null)
            countdownTimeline.stop();
        if (delayedAutoBidCheck != null)
            delayedAutoBidCheck.stop();
        ServerConnection.getInstance().setPushListener(null);
        new Thread(() -> bidClient.unsubscribe(ClientSession.getInstance().getToken(), currentAuctionId)).start();

        if (tempVoiceFile != null && tempVoiceFile.exists()) {
            tempVoiceFile.delete();
        }
    }

    /** Đồng hồ mỗi giây theo endDateTime; hết giờ thì ghi "Đã kết thúc" và dừng. */
    private void startCountdown() {
        if (countdownTimeline != null) {
            countdownTimeline.stop();
        }
        countdownTimeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), e -> {
                    long secs = Duration.between(LocalDateTime.now(), endDateTime).getSeconds();
                    if (secs <= 0) {
                        timeLeftLabel.setText("Đã kết thúc");
                        countdownTimeline.stop();
                    } else {
                        long h = secs / 3600;
                        long m = (secs % 3600) / 60;
                        long s = secs % 60;
                        timeLeftLabel.setText(String.format("Còn: %02d:%02d:%02d", h, m, s));
                    }
                }));
        countdownTimeline.setCycleCount(javafx.animation.Animation.INDEFINITE);
        countdownTimeline.play();
    }

    /** Biểu đồ giá, tắt animation để điểm realtime không làm chart giật. */
    private void setupBidPriceChart() {
        bidPriceSeries.setName("Giá bid");
        bidPriceChart.setAnimated(false);
        bidPriceChart.setCreateSymbols(true);
        bidPriceChart.getData().clear();
        bidPriceChart.getData().add(bidPriceSeries);
    }

    /** Thêm một điểm (thứ tự bid, số tiền) vào biểu đồ. */
    private void appendBidPricePoint(BidDTO bid) {
        int bidIndex = bidPriceSeries.getData().size() + 1;
        bidPriceSeries.getData().add(new XYChart.Data<>(bidIndex, bid.getAmount()));
    }

    /** Cập nhật người dẫn đầu mà không reset đồng hồ 3 giây. */
    private void updateCurrentLeader(UserDTO leader) {
        updateCurrentLeader(leader, false);
    }

    /**
     * Ghi nhận người dẫn đầu.
     * Nếu chính user vừa lên dẫn hoặc bid của họ đổi thì đếm lại khoảng 3 giây ổn định trước khi bật lại Auto-Bid.
     */
    private void updateCurrentLeader(UserDTO leader, boolean leaderBidChanged) {
        String previousLeaderId = currentLeaderId;
        currentLeaderId = leader != null ? leader.getId() : null;
        leaderLabel.setText("Người dẫn đầu: " + usernameOf(leader));

        if (isCurrentUser(leader)) {
            boolean justBecameLeader = previousLeaderId == null || !previousLeaderId.equals(currentLeaderId);
            // Mỗi lần vừa lên dẫn đầu thì đếm lại 3 giây ổn định.
            if (justBecameLeader || leaderBidChanged) {
                currentUserLeaderObservedAt = LocalDateTime.now();
                scheduleDelayedAutoBidCheck(AUTO_BID_STABILITY_CHECK_SECONDS);
            }
            return;
        }

        currentUserLeaderObservedAt = null;
    }

    /** Sau 3,5 giây hỏi lại trạng thái Auto-Bid, dùng khi có bid tay từ người khác. */
    private void scheduleDelayedAutoBidCheck() {
        scheduleDelayedAutoBidCheck(3.5);
    }

    /** Hẹn kiểm tra Auto-Bid; lần hẹn cũ bị hủy để không chạy chồng. */
    private void scheduleDelayedAutoBidCheck(double seconds) {
        if (delayedAutoBidCheck != null) {
            delayedAutoBidCheck.stop();
        }
        delayedAutoBidCheck = new javafx.animation.PauseTransition(javafx.util.Duration.seconds(seconds));
        delayedAutoBidCheck.setOnFinished(e -> checkAutoBidState());
        delayedAutoBidCheck.playFromStart();
    }

    /** Hỏi server cấu hình Auto-Bid hiện tại rồi điền trần, bước tăng và trạng thái nút. */
    private void checkAutoBidState() {
        String token = ClientSession.getInstance().getToken();
        new Thread(() -> {
            try {
                AutoBidConfigDTO state = bidClient.checkAutoBid(token, currentAuctionId);
                Platform.runLater(() -> {
                    if (state != null) {
                        maxBidField.setText(String.format("%.0f", state.getMaxBid()));
                        incrementField.setText(String.format("%.0f", state.getIncrement()));
                        renderAutoBidState(state);
                    } else {
                        enableAutoBidBtn.setDisable(false);
                        cancelAutoBidBtn.setDisable(true);
                        autoBidStatusLabel.setText("");
                        lastAutoBidStatus = null;
                        autoBidStateLoaded = true;
                        pendingAutoBidLossNotice = false;
                    }
                });
            } catch (Exception ignored) {
            }
        }).start();
    }

    /** Nút mic trên FXML: bật hoặc dừng ghi âm đặt giá. */
    @FXML
    private void handleMicClicked() {
        handleVoiceBiddingToggle();
    }

    /** Chưa có API key thì mở popup cấu hình; có key thì bắt đầu hoặc dừng ghi âm. */
    private void handleVoiceBiddingToggle() {
        // 1. Kiểm tra API Key trong cấu hình
        String apiKey = ClientSession.getInstance().getGeminiApiKey();
        if (apiKey.isEmpty()) {
            if (voiceBidStatusLabel != null) {
                voiceBidStatusLabel.setText("Chưa cấu hình API Key. Vui lòng thiết lập cấu hình.");
                voiceBidStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
            }
            // Mở popup cấu hình để nhập key
            handleVoiceSettingsClicked();

            // Kiểm tra lại sau khi đóng popup
            apiKey = ClientSession.getInstance().getGeminiApiKey();
            if (apiKey.isEmpty()) {
                return;
            }
        }

        // 2. Chuyển đổi trạng thái thu âm (Toggle)
        if (!isRecording) {
            startVoiceRecording();
        } else {
            stopVoiceRecordingAndProcess(apiKey);
        }
    }

    /** Ghi WAV tạm và đổi nút mic sang trạng thái đang thu. */
    private void startVoiceRecording() {
        try {
            isRecording = true;
            if (voiceBidStatusLabel != null) {
                voiceBidStatusLabel.setText("Đang thu âm... Click lại nút Nói để dừng");
                voiceBidStatusLabel.setStyle("-fx-text-fill: #3b82f6;");
            }
            btnMic.getStyleClass().add("recording");
            btnMic.setText("Dừng");

            audioRecorder = new AudioRecorder(tempVoiceFile);
            audioRecorder.startRecording();
        } catch (Exception e) {
            isRecording = false;
            btnMic.getStyleClass().remove("recording");
            btnMic.setText("Nói");
            if (voiceBidStatusLabel != null) {
                voiceBidStatusLabel.setText("Lỗi khởi động mic: " + e.getMessage());
                voiceBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            }
        }
    }

    /** Dừng mic, gửi file cho Gemini và điền số tiền nhận ra vào ô đặt giá. */
    private void stopVoiceRecordingAndProcess(String apiKey) {
        if (audioRecorder == null)
            return;

        try {
            isRecording = false;
            btnMic.getStyleClass().remove("recording");
            btnMic.setText("Nói");
            audioRecorder.stopRecording();

            if (voiceBidStatusLabel != null) {
                voiceBidStatusLabel.setText("Đang phân tích...");
                voiceBidStatusLabel.setStyle("-fx-text-fill: #f59e0b;");
            }

            String selectedModel = ClientSession.getInstance().getGeminiModel();

            new Thread(() -> {
                try {
                    long amount = voiceServiceClient.processVoiceBid(tempVoiceFile, apiKey, selectedModel);
                    Platform.runLater(() -> {
                        if (amount > 0) {
                            bidAmountField.setText(String.valueOf(amount));
                            if (voiceBidStatusLabel != null) {
                                voiceBidStatusLabel
                                        .setText("Đã ghi nhận số tiền: " + CurrencyFormatter.format(amount));
                                voiceBidStatusLabel.setStyle("-fx-text-fill: #10b981;");
                            }
                        } else {
                            if (voiceBidStatusLabel != null) {
                                voiceBidStatusLabel.setText("Gemini không phát hiện số tiền hợp lệ. Hãy thử lại!");
                                voiceBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                            }
                        }
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        if (voiceBidStatusLabel != null) {
                            voiceBidStatusLabel.setText("Lỗi xử lý: " + e.getMessage());
                            voiceBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                        }
                    });
                } finally {
                    if (tempVoiceFile.exists()) {
                        tempVoiceFile.delete();
                    }
                }
            }).start();

        } catch (Exception e) {
            isRecording = false;
            btnMic.getStyleClass().remove("recording");
            btnMic.setText("Nói");
            if (voiceBidStatusLabel != null) {
                voiceBidStatusLabel.setText("Lỗi dừng mic: " + e.getMessage());
                voiceBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            }
        }
    }

    /** Popup modal cấu hình model và API key Gemini; chờ đóng rồi mới tiếp tục. */
    @FXML
    private void handleVoiceSettingsClicked() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/VoiceConfigView.fxml"));
            Parent root = loader.load();

            javafx.stage.Stage stage = new javafx.stage.Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            if (btnMic != null && btnMic.getScene() != null) {
                stage.initOwner(btnMic.getScene().getWindow());
            }
            stage.setTitle("Cấu hình Nhận diện giọng nói Gemini");
            stage.setResizable(false);

            javafx.scene.Scene scene = new javafx.scene.Scene(root);
            ThemeManager.getInstance().applyTheme(scene);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            if (voiceBidStatusLabel != null) {
                voiceBidStatusLabel.setText("Lỗi mở cửa sổ cấu hình: " + e.getMessage());
                voiceBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            }
        }
    }

    /**
     * Lần đặt giá đầu phải đồng ý cọc 10% giá khởi điểm.
     * Đã cọc rồi thì bỏ qua hộp thoại. Trả false nếu user hủy.
     */
    private boolean confirmDepositIfNeeded() {
        if (currentUserDeposited) {
            return true;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận Đăng ký Đấu giá & Đặt cọc");
        alert.setHeaderText("ĐĂNG KÝ THAM GIA PHIÊN ĐẤU GIÁ");

        VBox content = new VBox(10);
        content.setPadding(new Insets(10));

        Label ruleLabel = new Label("Bằng việc đặt giá, bạn đồng ý với các điều khoản tham gia:");
        ruleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");

        Label rule1 = new Label("• Tuân thủ toàn bộ quy định và thời gian của phiên đấu giá.");
        Label rule2 = new Label("• Đồng ý đặt cọc 10% giá khởi điểm của sản phẩm.");

        // Tính toán và hiển thị số tiền đặt cọc cụ thể
        String depositMoneyStr = CurrencyFormatter.format(auctionDepositAmount);
        Label depositDetail = new Label("• Số tiền đặt cọc cần cọc cho phiên này: " + depositMoneyStr);
        depositDetail.setStyle("-fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-font-size: 14px;"); // Màu đỏ cảnh
                                                                                                       // báo

        Label rule3 = new Label("• Nếu thắng: Tiền cọc được khấu trừ khi thanh toán hóa đơn sản phẩm.");
        Label rule4 = new Label("• Nếu thua: Tiền cọc được hoàn trả đầy đủ vào ví tài khoản.");

        content.getChildren().addAll(ruleLabel, rule1, rule2, depositDetail, rule3, rule4);

        // Nếu có mô tả phiên (quy định phiên từ DTO)
        if (auctionDescription != null && !auctionDescription.isBlank()) {
            Label descTitle = new Label("Quy định cụ thể của phiên này:");
            descTitle.setStyle("-fx-font-weight: bold; -fx-padding: 10 0 0 0; -fx-font-size: 13px;");
            Label descDetail = new Label(auctionDescription);
            descDetail.setWrapText(true);
            descDetail.setMaxWidth(450);
            content.getChildren().addAll(descTitle, descDetail);
        }

        alert.getDialogPane().setContent(content);
        alert.getDialogPane().getStyleClass().add("card-bg");

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == ButtonType.OK;
    }

    /** Đặt giá tay sau khi đồng ý cọc; thành công thì đánh dấu đã cọc và tải lại ví. */
    @FXML
    private void handlePlaceBid() {
        String amountText = bidAmountField.getText().replace(",", "").trim();
        if (amountText.isEmpty()) {
            bidStatusLabel.setText("Vui lòng nhập số tiền.");
            return;
        }

        // Kiểm tra và yêu cầu cọc cho lần bid đầu tiên
        if (!confirmDepositIfNeeded()) {
            bidStatusLabel.setText("Đã hủy đặt giá do không đồng ý điều khoản đặt cọc.");
            bidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            return;
        }

        try {
            double amount = Double.parseDouble(amountText);
            String token = ClientSession.getInstance().getToken();

            bidStatusLabel.setText("Đang xử lý...");
            bidStatusLabel.setStyle("-fx-text-fill: #f39c12;");

            new Thread(() -> {
                try {
                    bidClient.placeBid(token, currentAuctionId, amount);
                    Platform.runLater(() -> {
                        bidStatusLabel.setText("Đặt giá thành công!");
                        bidStatusLabel.setStyle("-fx-text-fill: #22c55e;");
                        bidAmountField.clear();
                        currentUserDeposited = true;
                        renderAuctionDeposit();
                        loadWalletInfo();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        bidStatusLabel.setText("Lỗi: " + e.getMessage());
                        bidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                    });
                }
            }).start();
        } catch (NumberFormatException ex) {
            bidStatusLabel.setText("Số tiền không hợp lệ.");
            bidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
        }
    }

    /** Bật Auto-Bid với trần và bước tăng; cũng yêu cầu cọc nếu chưa đặt. */
    @FXML
    private void handleSetAutoBid() {
        String maxText = maxBidField.getText().replace(",", "").trim();
        String incText = incrementField.getText().replace(",", "").trim();

        if (maxText.isEmpty() || incText.isEmpty()) {
            autoBidStatusLabel.setText("Vui lòng nhập đủ thông tin.");
            return;
        }

        // Kiểm tra và yêu cầu cọc trước khi thiết lập Auto-Bid
        if (!confirmDepositIfNeeded()) {
            autoBidStatusLabel.setText("Đã hủy thiết lập Auto-Bid do không đồng ý điều khoản đặt cọc.");
            autoBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            return;
        }

        try {
            double maxBid = Double.parseDouble(maxText);
            double increment = Double.parseDouble(incText);
            String token = ClientSession.getInstance().getToken();

            autoBidStatusLabel.setText("Đang thiết lập...");

            new Thread(() -> {
                try {
                    bidClient.setAutoBid(token, currentAuctionId, maxBid, increment);
                    Platform.runLater(() -> {
                        currentUserDeposited = true;
                        renderAuctionDeposit();
                        loadWalletInfo();
                        checkAutoBidState();
                    });
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        autoBidStatusLabel.setText("Lỗi: " + e.getMessage());
                        autoBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                    });
                }
            }).start();
        } catch (NumberFormatException ex) {
            autoBidStatusLabel.setText("Số tiền không hợp lệ.");
        }
    }

    /** Tắt Auto-Bid trên server và mở lại nút bật. */
    @FXML
    private void handleCancelAutoBid() {
        String token = ClientSession.getInstance().getToken();
        new Thread(() -> {
            try {
                bidClient.cancelAutoBid(token, currentAuctionId);
                Platform.runLater(() -> {
                    autoBidStatusLabel.setText("Đã hủy Auto-Bid.");
                    autoBidStatusLabel.setStyle("-fx-text-fill: #f39c12;");
                    enableAutoBidBtn.setDisable(false);
                    cancelAutoBidBtn.setDisable(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    autoBidStatusLabel.setText("Lỗi hủy Auto-Bid: " + e.getMessage());
                    autoBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
                });
            }
        }).start();
    }

    /** Dọn phòng đặt giá rồi quay về danh sách phiên. */
    @FXML
    private void handleBack() {
        cleanup();
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/fxml/AuctionListView.fxml"));
            Parent listRoot = loader.load();

            Parent currentRoot = titleLabel.getScene().getRoot();
            if (currentRoot instanceof HBox) {
                VBox mainCard = (VBox) ((HBox) currentRoot).getChildren().get(1);
                javafx.scene.layout.StackPane contentArea = (javafx.scene.layout.StackPane) mainCard.getChildren()
                        .get(1);
                contentArea.getChildren().setAll(listRoot);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Một dòng lịch sử: giờ, người đặt, số tiền và loại bid (tay hoặc auto). */
    private String formatBid(BidDTO bid) {
        String time = bid.getTimestamp() != null ? bid.getTimestamp().format(DISPLAY_TIME) : "";
        String type = bid.getBidType() != null ? " - " + bid.getBidType().name() : "";
        return time + " - " + usernameOf(bid.getBidder()) + " - "
                + CurrencyFormatter.format(bid.getAmount()) + type;
    }

    /** Hiện số dư khả dụng, số lớn được rút gọn kèm tooltip. */
    private void renderWallet(WalletResponseDTO wallet) {
        double balance = wallet != null ? wallet.getBalance() : 0.0;
        CurrencyFormatter.setMoneyText(balanceLabel, balance);
    }

    /** Hiện tiền cọc của phiên nếu user đã đặt; chưa cọc thì 0. */
    private void renderAuctionDeposit() {
        double deposit = currentUserDeposited ? auctionDepositAmount : 0.0;
        CurrencyFormatter.setMoneyText(depositLabel, deposit);
    }

    /**
     * PROTECTING: đang giữ giá, khóa nút bật.
     * INEFFECTIVE: vượt trần, chỉ cho bật lại sau khi user dẫn đầu đủ khoảng 3 giây.
     * Trạng thái khác: đang chờ và sẽ tự bid khi cần.
     */
    private void renderAutoBidState(AutoBidConfigDTO state) {
        AutoBidStatus status = state.getStatus();
        // Chỉ báo một lần khi Auto-Bid vừa chuyển sang hết hiệu lực vì bị vượt trần.
        boolean shouldNotifyIneffective = autoBidStateLoaded
                && status == AutoBidStatus.INEFFECTIVE
                && lastAutoBidStatus != null
                && lastAutoBidStatus != AutoBidStatus.INEFFECTIVE
                && pendingAutoBidLossNotice;

        // PROTECTING: đang dẫn đầu, hệ thống giữ giá tới trần. INEFFECTIVE: giá đã vượt trần.
        if (status == AutoBidStatus.PROTECTING) {
            enableAutoBidBtn.setDisable(true);
            cancelAutoBidBtn.setDisable(false);
            autoBidStatusLabel.setText("Bạn đang dẫn đầu. Auto-Bid sẽ bảo vệ tới "
                    + formatMoney(state.getProtectedUntil()) + ".");
            autoBidStatusLabel.setStyle("-fx-text-fill: #22c55e;");
            lastAutoBidStatus = status;
            autoBidStateLoaded = true;
            pendingAutoBidLossNotice = false;
            return;
        }

        if (status == AutoBidStatus.INEFFECTIVE) {
            boolean currentUserLeader = isCurrentUserLeader();
            boolean canTryEnable = currentUserLeader && hasObservedCurrentUserLeadLongEnough();
            enableAutoBidBtn.setDisable(!canTryEnable);
            cancelAutoBidBtn.setDisable(false);
            autoBidStatusLabel.setText(currentUserLeader
                    ? (canTryEnable ? AUTO_BID_REENABLE_HINT : AUTO_BID_REGAIN_LEAD_HINT)
                    : AUTO_BID_INEFFECTIVE_NOTICE);
            autoBidStatusLabel.setStyle("-fx-text-fill: #dc2626;");
            if (shouldNotifyIneffective) {
                bidHistoryList.getItems().add(0, "Thông báo - " + AUTO_BID_INEFFECTIVE_NOTICE);
            }
            lastAutoBidStatus = status;
            autoBidStateLoaded = true;
            pendingAutoBidLossNotice = false;
            return;
        }

        enableAutoBidBtn.setDisable(true);
        cancelAutoBidBtn.setDisable(false);
        autoBidStatusLabel.setText("Auto-Bid đang chờ. Hệ thống sẽ tự đặt giá tới "
                + formatMoney(state.getProtectedUntil()) + " khi cần.");
        autoBidStatusLabel.setStyle("-fx-text-fill: #f39c12;");
        lastAutoBidStatus = status;
        autoBidStateLoaded = true;
        pendingAutoBidLossNotice = false;
    }

    /** Chuỗi tiền rút gọn dùng trong câu trạng thái Auto-Bid. */
    private String formatMoney(double amount) {
        return CurrencyFormatter.format(amount);
    }

    /** User đang đăng nhập có đúng là người dẫn đầu hiện tại không. */
    private boolean isCurrentUserLeader() {
        UserDTO currentUser = ClientSession.getInstance().getCurrentUser();
        return currentUser != null && currentLeaderId != null && currentLeaderId.equals(currentUser.getId());
    }

    /** Đã giữ vị trí dẫn đầu liên tục đủ AUTO_BID_STABILITY_CHECK_SECONDS. */
    private boolean hasObservedCurrentUserLeadLongEnough() {
        return currentUserLeaderObservedAt != null
                && Duration.between(currentUserLeaderObservedAt, LocalDateTime.now()).toMillis() >= Math
                        .round(AUTO_BID_STABILITY_CHECK_SECONDS * 1000);
    }

    /** So id bidder với user trong session. */
    private boolean isCurrentUser(UserDTO user) {
        UserDTO currentUser = ClientSession.getInstance().getCurrentUser();
        return user != null && currentUser != null && user.getId().equals(currentUser.getId());
    }

    /** Username hoặc "Chưa rõ". */
    private String usernameOf(UserDTO user) {
        return user != null ? user.getUsername() : "Chưa rõ";
    }
}
