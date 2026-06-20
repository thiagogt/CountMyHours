package com.countmyh.view;

import com.countmyh.util.I18n;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Locale;

public class SettingsView {

    private static final Path DATA_DIR = Path.of(System.getProperty("user.home"), ".countmyhours");

    private final Runnable onLanguageChanged;
    private final VBox content;

    public SettingsView(Runnable onLanguageChanged) {
        this.onLanguageChanged = onLanguageChanged;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        build();
    }

    private void build() {
        var title = new Label(I18n.get("settings.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildLanguageSection(), buildUninstallSection());
    }

    private Node buildLanguageSection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("settings.language"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var desc = new Label(I18n.get("settings.language.desc"));
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 12px;");

        var group = new ToggleGroup();

        var btnEn = new ToggleButton("English");
        btnEn.getStyleClass().add("filter-button");
        btnEn.setToggleGroup(group);
        btnEn.setStyle("-fx-font-size: 13px; -fx-padding: 8 24;");

        var btnPt = new ToggleButton("Português (BR)");
        btnPt.getStyleClass().add("filter-button");
        btnPt.setToggleGroup(group);
        btnPt.setStyle("-fx-font-size: 13px; -fx-padding: 8 24;");

        String currentLang = I18n.getLocale().getLanguage();
        if ("pt".equals(currentLang)) {
            btnPt.setSelected(true);
        } else {
            btnEn.setSelected(true);
        }

        var btnApply = new Button(I18n.get("settings.language.apply"));
        btnApply.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 24;");

        btnApply.setOnAction(e -> {
            Locale locale = btnPt.isSelected() ? Locale.of("pt", "BR") : Locale.ENGLISH;
            if (!locale.equals(I18n.getLocale())) {
                I18n.setLocale(locale);
                onLanguageChanged.run();
            }
        });

        var buttons = new HBox(12, btnEn, btnPt);
        buttons.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(sectionTitle, desc, buttons, btnApply);
        return container;
    }

    private Node buildUninstallSection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("settings.uninstall"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var desc = new Label(I18n.get("settings.uninstall.desc"));
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 12px; -fx-wrap-text: true;");
        desc.setWrapText(true);

        var pathLabel = new Label(I18n.get("settings.uninstall.path", DATA_DIR.toString()));
        pathLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");

        var btnUninstall = new Button(I18n.get("settings.uninstall.button"));
        btnUninstall.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 24;");

        btnUninstall.setOnAction(e -> handleUninstall());

        container.getChildren().addAll(sectionTitle, desc, pathLabel, btnUninstall);
        return container;
    }

    private void handleUninstall() {
        var alert = new Alert(Alert.AlertType.WARNING);
        styleAlert(alert);
        alert.setTitle(I18n.get("settings.uninstall.dialog.title"));
        alert.setHeaderText(I18n.get("settings.uninstall.dialog.header"));
        alert.setContentText(I18n.get("settings.uninstall.dialog.content"));
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        var result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        try {
            if (Files.exists(DATA_DIR)) {
                try (var paths = Files.walk(DATA_DIR)) {
                    paths.sorted(Comparator.reverseOrder())
                            .forEach(p -> {
                                try { Files.delete(p); } catch (IOException ignored) {}
                            });
                }
            }
        } catch (IOException ignored) {}

        var done = new Alert(Alert.AlertType.INFORMATION);
        styleAlert(done);
        done.setTitle(I18n.get("settings.uninstall.done.title"));
        done.setHeaderText(I18n.get("settings.uninstall.done.header"));
        done.setContentText(I18n.get("settings.uninstall.done.content"));
        done.showAndWait();

        Platform.exit();
    }

    private void styleAlert(Alert alert) {
        var dialogPane = alert.getDialogPane();
        var css = getClass().getResource("/com/countmyh/dark-theme.css");
        if (css != null) {
            dialogPane.getStylesheets().add(css.toExternalForm());
        }
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
