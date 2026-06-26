package com.countmyh.view;

import com.countmyh.model.WorkHourItem;
import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.util.AppDirs;
import com.countmyh.util.ColorPalette;
import com.countmyh.util.I18n;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SettingsView {

    private static final Path DATA_DIR = AppDirs.DATA_DIR;

    private final WorkPeriodTracker data;
    private final JsonPersistenceService persistenceService;
    private final Runnable onLanguageChanged;
    private final VBox content;

    public SettingsView(WorkPeriodTracker data, JsonPersistenceService persistenceService,
                        Runnable onLanguageChanged) {
        this.data = data;
        this.persistenceService = persistenceService;
        this.onLanguageChanged = onLanguageChanged;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        build();
    }

    private void build() {
        var title = new Label(I18n.get("settings.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildLanguageSection(), buildProjectManageSection(), buildUninstallSection());
    }

    private Node buildLanguageSection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("settings.language"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var desc = new Label(I18n.get("settings.language.desc"));
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 12px;");

        record LangOption(String label, Locale locale) {}
        var options = List.of(
            new LangOption("Português (BR)",    Locale.of("pt", "BR")),
            new LangOption("English (US)",      Locale.of("en", "US")),
            new LangOption("English (UK)",      Locale.of("en", "GB")),
            new LangOption("English (Canada)",  Locale.of("en", "CA")),
            new LangOption("中文 (CN)",          Locale.of("zh", "CN")),
            new LangOption("हिन्दी (India)",      Locale.of("hi", "IN")),
            new LangOption("日本語 (JP)",         Locale.of("ja", "JP")),
            new LangOption("Italiano (IT)",     Locale.of("it", "IT")),
            new LangOption("Español (ES)",      Locale.of("es", "ES"))
        );

        var group = new ToggleGroup();
        var flowPane = new javafx.scene.layout.FlowPane(8, 8);

        String currentTag = I18n.getLocale().toLanguageTag();
        for (var opt : options) {
            var btn = new ToggleButton(opt.label());
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            btn.setStyle("-fx-font-size: 12px; -fx-padding: 7 16;");
            btn.setUserData(opt.locale());
            if (opt.locale().toLanguageTag().equals(currentTag)
                    || (currentTag.startsWith("en") && opt.locale().equals(Locale.of("en","US"))
                        && !currentTag.equals("en-GB") && !currentTag.equals("en-CA"))) {
                btn.setSelected(true);
            }
            flowPane.getChildren().add(btn);
        }

        var btnApply = new Button(I18n.get("settings.language.apply"));
        btnApply.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 8 24;");

        btnApply.setOnAction(e -> {
            var selected = group.getSelectedToggle();
            if (selected == null) return;
            Locale locale = (Locale) selected.getUserData();
            if (!locale.toLanguageTag().equals(I18n.getLocale().toLanguageTag())) {
                I18n.setLocale(locale);
                onLanguageChanged.run();
            }
        });

        container.getChildren().addAll(sectionTitle, desc, flowPane, btnApply);
        return container;
    }

    private Node buildProjectManageSection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("settings.projects"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var desc = new Label(I18n.get("settings.projects.desc"));
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 12px; -fx-wrap-text: true;");
        desc.setWrapText(true);

        var projectList = new VBox(6);
        projectList.setPadding(new Insets(4, 0, 0, 0));

        var projects = data.getEntries().stream()
                .map(WorkHourItem::project)
                .distinct()
                .sorted()
                .toList();

        for (String project : projects) {
            var dot = new Rectangle(10, 10);
            dot.setStyle("-fx-fill: " + ColorPalette.getColor(project) + "; -fx-arc-width: 10; -fx-arc-height: 10;");

            var label = new Label(project);
            label.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 13px;");

            long hours = Math.round(data.getEntries().stream()
                    .filter(e -> e.project().equals(project))
                    .mapToDouble(WorkHourItem::hours)
                    .sum());
            var hoursLabel = new Label(hours + "h");
            hoursLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");

            var cb = new CheckBox(I18n.get("settings.projects.visible"));
            cb.setSelected(!data.isProjectHidden(project));
            cb.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");

            cb.selectedProperty().addListener((obs, old, isSelected) -> {
                data.setProjectHidden(project, !isSelected);
                try {
                    persistenceService.save(data);
                } catch (IOException ignored) {}
            });

            var row = new HBox(10, dot, label, hoursLabel, cb);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 8, 4, 8));
            row.setStyle("-fx-background-color: #0f1117; -fx-background-radius: 6;");
            projectList.getChildren().add(row);
        }

        if (projects.isEmpty()) {
            var empty = new Label(I18n.get("settings.projects.empty"));
            empty.setStyle("-fx-text-fill: #8b8d97;");
            projectList.getChildren().add(empty);
        }

        container.getChildren().addAll(sectionTitle, desc, projectList);
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
