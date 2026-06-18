package com.countmyh;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        var label = new Label("CountMyHours");
        label.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 24px; -fx-font-weight: bold;");

        var root = new StackPane(label);
        root.setStyle("-fx-background-color: #0f1117;");

        var scene = new Scene(root, 1200, 800);
        primaryStage.setTitle("CountMyHours");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
