package com.countmyh;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.util.I18n;
import com.countmyh.view.MainView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.Locale;

public class App extends Application {

    private static final double MIN_SPLASH_SECONDS = 4.0;
    private StackPane rootStack;
    private Stage primaryStage;

    private WorkPeriodTracker loadedData;
    private CalculationService calcService;
    private BusinessDayService businessDayService;
    private JsonPersistenceService persistenceService;
    private CsvImportService csvImportService;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        rootStack = new StackPane();
        var scene = new Scene(rootStack, 1280, 860);
        var style = getClass().getResource("dark-theme.css");
        if (style != null)
            scene.getStylesheets().add(style.toExternalForm());

        primaryStage.setTitle(I18n.get("app.title"));
        var iconStream = getClass().getResourceAsStream("logo.png");
        if (iconStream != null)
            primaryStage.getIcons().add(new Image(iconStream));

        var splash = buildSplash();
        rootStack.getChildren().add(splash.root());
        primaryStage.setScene(scene);
        primaryStage.show();

        var blinkAnim = createBlinkAnimation(splash.logoView(), splash.eyesOpen(), splash.eyesClosed());
        blinkAnim.play();

        long startTime = System.currentTimeMillis();

        new Thread(() -> {
            persistenceService = new JsonPersistenceService();
            businessDayService = new BusinessDayService();
            calcService = new CalculationService(businessDayService);
            csvImportService = new CsvImportService();

            WorkPeriodTracker data;
            try {
                data = persistenceService.load();
            } catch (IOException e) {
                data = new WorkPeriodTracker();
            }

            if (data.getEntries().isEmpty()) {
                loadSampleData(data, csvImportService, persistenceService);
            }

            loadedData = data;
            Platform.runLater(() -> {
                long elapsed = System.currentTimeMillis() - startTime;
                double remaining = MIN_SPLASH_SECONDS - (elapsed / 1000.0);
                var splashPane = splash.root();

                Runnable afterSplash = () -> {
                    if (I18n.hasSavedLocale()) {
                        showMainApp();
                    } else {
                        showLanguagePicker();
                    }
                };

                if (remaining > 0) {
                    var wait = new PauseTransition(Duration.seconds(remaining));
                    wait.setOnFinished(e -> fadeOutSplash(splashPane, blinkAnim, afterSplash));
                    wait.play();
                } else {
                    fadeOutSplash(splashPane, blinkAnim, afterSplash);
                }
            });
        }).start();
    }

    private void showLanguagePicker() {
        var picker = new VBox(24);
        picker.setAlignment(Pos.CENTER);
        picker.setStyle("-fx-background-color: #0f1117;");
        picker.setPadding(new Insets(40));

        var logoStream = getClass().getResourceAsStream("logo.png");
        if (logoStream != null) {
            var logoView = new ImageView(new Image(logoStream));
            logoView.setFitWidth(90);
            logoView.setFitHeight(90);
            logoView.setPreserveRatio(true);
            picker.getChildren().add(logoView);
        }

        var welcomeTitle = new Label("Welcome to CountMyHours!");
        welcomeTitle.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7; -fx-text-alignment: center;");
        welcomeTitle.setWrapText(true);

        var welcomeMsg = new Label("Thank you for choosing CountMyHours.\nYour personal work hours tracker, handcrafted with care.");
        welcomeMsg.setStyle("-fx-font-size: 14px; -fx-text-fill: #8b8d97; -fx-text-alignment: center;");
        welcomeMsg.setWrapText(true);

        var thanksMsg = new Label("Thank you for supporting independent software.\n— The CountMyHours Team");
        thanksMsg.setStyle("-fx-font-size: 13px; -fx-text-fill: #6366f1; -fx-text-alignment: center; -fx-font-style: italic;");
        thanksMsg.setWrapText(true);

        var selectLabel = new Label("Select your language:");
        selectLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #e4e4e7; -fx-font-weight: bold;");

        record LangOption(String label, Locale locale) {}
        var langOptions = java.util.List.of(
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
        var langPane = new javafx.scene.layout.FlowPane(10, 10);
        langPane.setAlignment(Pos.CENTER);
        langPane.setMaxWidth(600);
        for (var opt : langOptions) {
            var btn = new ToggleButton(opt.label());
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            btn.setStyle("-fx-font-size: 13px; -fx-padding: 9 20;");
            btn.setUserData(opt.locale());
            langPane.getChildren().add(btn);
        }

        var btnContinue = new Button("Continue");
        btnContinue.setStyle("-fx-background-color: #6366f1; -fx-text-fill: white; -fx-font-size: 15px; "
                + "-fx-font-weight: bold; -fx-padding: 12 40; -fx-background-radius: 10; -fx-cursor: hand;");
        btnContinue.setDisable(true);

        group.selectedToggleProperty().addListener((obs, old, sel) -> btnContinue.setDisable(sel == null));

        btnContinue.setOnAction(e -> {
            var selected = group.getSelectedToggle();
            if (selected == null) return;
            Locale locale = (Locale) selected.getUserData();
            I18n.setLocale(locale);
            primaryStage.setTitle(I18n.get("app.title"));

            var fadeOut = new FadeTransition(Duration.millis(400), picker);
            fadeOut.setFromValue(1.0);
            fadeOut.setToValue(0.0);
            fadeOut.setOnFinished(ev -> {
                rootStack.getChildren().remove(picker);
                showMainApp();
            });
            fadeOut.play();
        });

        picker.getChildren().addAll(welcomeTitle, welcomeMsg, thanksMsg, selectLabel, langPane, btnContinue);
        rootStack.getChildren().add(picker);
    }

    private void showMainApp() {
        var mainView = new MainView(loadedData, calcService, businessDayService, persistenceService, csvImportService);
        rootStack.getChildren().addFirst(mainView.getRoot());

        var mainNode = mainView.getRoot();
        mainNode.setOpacity(0);
        var fadeIn = new FadeTransition(Duration.millis(400), mainNode);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
    }

    private record SplashComponents(VBox root, ImageView logoView, Image eyesOpen, Image eyesClosed) {}

    private SplashComponents buildSplash() {
        var splash = new VBox(20);
        splash.setAlignment(Pos.CENTER);
        splash.setStyle("-fx-background-color: #0f1117;");

        Image eyesOpen = loadImage("logo.png");
        Image eyesClosed = loadImage("logo-blink.png");

        var logoView = new ImageView(eyesOpen != null ? eyesOpen : new Image(getClass().getResourceAsStream("logo.png")));
        logoView.setFitWidth(120);
        logoView.setFitHeight(120);
        logoView.setPreserveRatio(true);

        var title = new Label(I18n.get("app.title"));
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var subtitle = new Label(I18n.get("app.loading"));
        subtitle.setStyle("-fx-font-size: 14px; -fx-text-fill: #6366f1;");

        splash.getChildren().addAll(logoView, title, subtitle);
        return new SplashComponents(splash, logoView, eyesOpen, eyesClosed);
    }

    private Image loadImage(String name) {
        var stream = getClass().getResourceAsStream(name);
        return stream != null ? new Image(stream) : null;
    }

    private Timeline createBlinkAnimation(ImageView logoView, Image eyesOpen, Image eyesClosed) {
        if (eyesOpen == null || eyesClosed == null) {
            return new Timeline();
        }

        var timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> logoView.setImage(eyesOpen)),
                new KeyFrame(Duration.millis(1800), e -> logoView.setImage(eyesClosed)),
                new KeyFrame(Duration.millis(1950), e -> logoView.setImage(eyesOpen)),
                new KeyFrame(Duration.millis(2100), e -> logoView.setImage(eyesClosed)),
                new KeyFrame(Duration.millis(2250), e -> logoView.setImage(eyesOpen)),
                new KeyFrame(Duration.millis(4000))
        );
        timeline.setCycleCount(Timeline.INDEFINITE);
        return timeline;
    }

    private void fadeOutSplash(VBox splash, Timeline blinkAnim, Runnable onFinished) {
        blinkAnim.stop();
        var fadeOut = new FadeTransition(Duration.millis(600), splash);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> {
            ((StackPane) splash.getParent()).getChildren().remove(splash);
            onFinished.run();
        });
        fadeOut.play();
    }

    private void loadSampleData(WorkPeriodTracker data, CsvImportService csvImportService,
                                JsonPersistenceService persistenceService) {
        try (InputStream is = getClass().getResourceAsStream("sample-data.csv")) {
            if (is == null) return;
            Path tempFile = Files.createTempFile("countmyhours-sample", ".csv");
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);

            var items = csvImportService.importFile(tempFile.toFile());
            var tagged = items.stream().map(i -> i.withSourceFile("sample-data.csv")).toList();
            int added = data.addEntriesWithDedup(tagged);

            if (added > 0) {
                data.addImportRecord(new com.countmyh.model.ImportRecord(
                        "sample-data.csv", "sample-data.csv", LocalDateTime.now(), added
                ));
                data.setLastImportDate(LocalDateTime.now());
                data.setLastSourceFile("sample-data.csv");
                persistenceService.save(data);
            }

            Files.deleteIfExists(tempFile);
        } catch (IOException e) {
            // silently skip sample data loading
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
