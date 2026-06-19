package com.countmyh;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.view.MainView;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Label;
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

public class App extends Application {

    private static final double MIN_SPLASH_SECONDS = 4.0;

    @Override
    public void start(Stage primaryStage) {
        var rootStack = new StackPane();
        var scene = new Scene(rootStack, 1280, 860);
        var style = getClass().getResource("dark-theme.css");
        if (style != null)
            scene.getStylesheets().add(style.toExternalForm());

        primaryStage.setTitle("CountMyHours");
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
            var persistenceService = new JsonPersistenceService();
            var businessDayService = new BusinessDayService();
            var calcService = new CalculationService(businessDayService);
            var csvImportService = new CsvImportService();

            WorkPeriodTracker data;
            try {
                data = persistenceService.load();
            } catch (IOException e) {
                data = new WorkPeriodTracker();
            }

            if (data.getEntries().isEmpty()) {
                loadSampleData(data, csvImportService, persistenceService);
            }

            final var loadedData = data;
            Platform.runLater(() -> {
                var mainView = new MainView(loadedData, calcService, persistenceService, csvImportService);
                rootStack.getChildren().addFirst(mainView.getRoot());

                long elapsed = System.currentTimeMillis() - startTime;
                double remaining = MIN_SPLASH_SECONDS - (elapsed / 1000.0);
                var splashPane = splash.root();

                if (remaining > 0) {
                    var wait = new PauseTransition(Duration.seconds(remaining));
                    wait.setOnFinished(e -> fadeOutSplash(splashPane, blinkAnim));
                    wait.play();
                } else {
                    fadeOutSplash(splashPane, blinkAnim);
                }
            });
        }).start();
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

        var title = new Label("CountMyHours");
        title.setStyle("-fx-font-size: 28px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var subtitle = new Label("Loading...");
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

    private void fadeOutSplash(VBox splash, Timeline blinkAnim) {
        blinkAnim.stop();
        var fadeOut = new FadeTransition(Duration.millis(600), splash);
        fadeOut.setFromValue(1.0);
        fadeOut.setToValue(0.0);
        fadeOut.setOnFinished(e -> ((StackPane) splash.getParent()).getChildren().remove(splash));
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
