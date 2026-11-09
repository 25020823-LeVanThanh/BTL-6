package com.auctionuet.client;

import com.auctionuet.client.ui.view.SceneManager;
import com.auctionuet.client.ui.view.ThemeManager;
import javafx.application.Application;
import javafx.stage.Stage;

public class ClientApp extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("AuctionUET");
        ThemeManager.getInstance().setTheme(ThemeManager.Theme.DARK);
        SceneManager.getInstance().init(stage);
        SceneManager.getInstance().switchScene("/fxml/MainView.fxml");
    }

    public static void main(String[] args) {
        launch(args);
    }
}
