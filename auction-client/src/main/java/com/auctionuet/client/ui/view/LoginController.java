package com.auctionuet.client.ui.view;

import com.auctionuet.client.model.ClientSession;
import com.auctionuet.client.network.AuthClient;
import com.auctionuet.client.network.ServerConnection;
import com.auctionuet.protocol.dto.response.auth.LoginResponseDTO;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Màn hình đăng nhập.
 * Kiểm tra form trên luồng giao diện, rồi kết nối server và gọi API trên luồng nền
 * để cửa sổ không bị đơ. Kết quả được đưa về luồng JavaFX bằng Platform.runLater.
 */
public class LoginController {
    /** Ô nhập tên đăng nhập, mật khẩu và nhãn báo lỗi gắn từ FXML. */
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Label errorLabel;
    @FXML private javafx.scene.control.Button loginButton;

    private MainController mainController;

    /** MainController giữ panel bên phải, dùng khi chuyển sang màn đăng ký. */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /** Đăng nhập: kiểm tra form, mở socket, nhận token rồi vào Dashboard. */
    @FXML
    public void handleLogin() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username.isBlank() || password.isBlank()) {
            errorLabel.setText("Lỗi: Không được để trống!");
            errorLabel.setVisible(true);
            return;
        }

        errorLabel.setText("Đang kết nối Server...");
        errorLabel.setStyle("-fx-text-fill: blue;");
        errorLabel.setVisible(true);
        loginButton.setDisable(true);

        new Thread(() -> {
            try {
                ServerConnection.getInstance().connect(
                        ServerConnection.DEFAULT_HOST, ServerConnection.DEFAULT_PORT);
                AuthClient client = new AuthClient();
                LoginResponseDTO loginResponse = client.login(username, password);

                javafx.application.Platform.runLater(() -> {
                    ClientSession.getInstance().login(
                            loginResponse.getToken(),
                            loginResponse.getUser(),
                            loginResponse.isMustChangePassword());
                    SceneManager.getInstance().switchScene("/fxml/DashboardView.fxml");
                    loginButton.setDisable(false);
                });
            } catch (Exception e) {
                javafx.application.Platform.runLater(() -> {
                    errorLabel.setText("Lỗi đăng nhập: " + e.getMessage());
                    errorLabel.setStyle("-fx-text-fill: red;");
                    loginButton.setDisable(false);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    /** Đổi panel phải sang form đăng ký, vẫn giữ banner bên trái. */
    @FXML
    public void handleSwitchToRegister() {
        if (mainController != null) {
            mainController.switchToRegister();
        }
    }

    /** Quay về màn chọn vai trò / màn chính. */
    @FXML
    public void handleBack() {
        SceneManager.getInstance().switchScene("/fxml/MainView.fxml");
    }
}
