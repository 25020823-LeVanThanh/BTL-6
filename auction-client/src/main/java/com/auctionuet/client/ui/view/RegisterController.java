package com.auctionuet.client.ui.view;

import com.auctionuet.client.network.AuthClient;
import com.auctionuet.client.network.ServerConnection;
import com.auctionuet.client.util.ValidationUtil;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Màn hình đăng ký tài khoản Bidder hoặc Seller.
 * Validate dữ liệu trước, rồi kết nối server trên luồng nền.
 * Thành công thì chờ 1,5 giây để người dùng đọc thông báo, sau đó chuyển về đăng nhập.
 */
public class RegisterController {
    @FXML private TextField usernameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmField;
    @FXML private ComboBox<String> roleComboBox;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.control.Button registerButton;

    private MainController mainController;

    /** Nhận controller cha để đổi panel Login/Register mà không đổi Scene. */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /** JavaFX gọi sau khi nạp FXML: đổ sẵn hai vai trò được phép tự đăng ký. */
    @FXML
    public void initialize() {
        roleComboBox.getItems().addAll("Bidder", "Seller");
    }

    /** Gửi yêu cầu đăng ký tới localhost:3307. */
    @FXML
    public void handleRegister() {
        String username = usernameField.getText();
        String password = passwordField.getText();
        String confirm = confirmField.getText();
        String email = emailField.getText();
        String selectedRole = roleComboBox.getValue();

        if (selectedRole == null) {
            showError("Lỗi: Vui lòng chọn vai trò.");
            return;
        }

        try {
            ValidationUtil.validateUsername(username);
            ValidationUtil.validateEmail(email);
            ValidationUtil.validatePasswordConfirm(password, confirm);
        } catch (IllegalArgumentException e) {
            showError("Lỗi: " + e.getMessage());
            return;
        }

        // Server nhận role dạng enum: BIDDER, SELLER.
        String role = selectedRole.toUpperCase();
        errorLabel.setText("Đang kết nối Server...");
        errorLabel.setStyle("-fx-text-fill: blue;");
        errorLabel.setVisible(true);
        registerButton.setDisable(true);

        new Thread(() -> {
            try {
                ServerConnection.getInstance().connect(
                        ServerConnection.DEFAULT_HOST, ServerConnection.DEFAULT_PORT);
                AuthClient client = new AuthClient();
                client.register(username, password, email, role);

                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText("Đăng ký thành công! Đang chuyển sang Đăng nhập...");
                    errorLabel.setStyle("-fx-text-fill: green;");

                    // Giữ thông báo xanh một lúc rồi mới đổi form.
                    javafx.animation.PauseTransition pause =
                            new javafx.animation.PauseTransition(javafx.util.Duration.millis(1500));
                    pause.setOnFinished(e -> {
                        if (mainController != null) {
                            mainController.switchToLogin();
                        }
                    });
                    pause.play();
                    registerButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText("Lỗi đăng ký: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: red;");
                    registerButton.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    @FXML
    public void handleSwitchToLogin() {
        if (mainController != null) {
            mainController.switchToLogin();
        }
    }

    @FXML
    public void handleBack() {
        SceneManager.getInstance().switchScene("/fxml/MainView.fxml");
    }

    /** Hiện lỗi validation ngay trên form, chưa cần gọi server. */
    private void showError(String msg) {
        errorLabel.setText(msg);
        errorLabel.setVisible(true);
    }
}
