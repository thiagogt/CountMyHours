package com.countmyh.view;

import com.countmyh.util.I18n;
import com.countmyh.util.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.awt.Desktop;
import java.net.URI;

public class SupportView {

    private static final String SUPPORT_EMAIL = "countmyhour@gmail.com";

    private final VBox content;
    private final StackPane rootStack;

    public SupportView() {
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        this.rootStack = new StackPane();
        build();
    }

    private void build() {
        var title = new Label(I18n.get("support.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildContactCard());
    }

    private Node buildContactCard() {
        var container = new VBox(16);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("support.contact.title"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var desc = new Label(I18n.get("support.desc"));
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 13px;");
        desc.setWrapText(true);

        var emailLabel = new Label(SUPPORT_EMAIL);
        emailLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #6366f1; "
                + "-fx-padding: 10 16; -fx-background-color: #0f1117; "
                + "-fx-background-radius: 8; -fx-border-color: #2a2d3a; "
                + "-fx-border-radius: 8;");

        var btnSend = new Button(I18n.get("support.email.button"));
        btnSend.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 8 24; -fx-background-radius: 6;");
        btnSend.setOnAction(e -> openMailClient());

        var btnCopy = new Button(I18n.get("support.copy.button"));
        btnCopy.setStyle("-fx-background-color: #2a2d3a; -fx-text-fill: #e4e4e7; "
                + "-fx-padding: 8 24; -fx-background-radius: 6;");
        btnCopy.setOnAction(e -> copyEmail(btnCopy));

        var buttonRow = new HBox(10, btnSend, btnCopy);
        buttonRow.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(sectionTitle, desc, emailLabel, buttonRow);
        return container;
    }

    private void openMailClient() {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().mail(new URI("mailto:" + SUPPORT_EMAIL));
            } else {
                new ProcessBuilder("open", "mailto:" + SUPPORT_EMAIL).start();
            }
        } catch (Exception ex) {
            Toast.show(rootStack, I18n.get("support.email.failed"), Toast.Type.ERROR);
        }
    }

    private void copyEmail(Button btn) {
        var clipboard = Clipboard.getSystemClipboard();
        var clipContent = new ClipboardContent();
        clipContent.putString(SUPPORT_EMAIL);
        clipboard.setContent(clipContent);

        var original = btn.getText();
        btn.setText(I18n.get("support.copy.done"));
        btn.setDisable(true);
        new Thread(() -> {
            try { Thread.sleep(1500); } catch (InterruptedException ignored) {}
            javafx.application.Platform.runLater(() -> {
                btn.setText(original);
                btn.setDisable(false);
            });
        }).start();
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rootStack.getChildren().add(scrollPane);
        return rootStack;
    }
}
