package com.countmyh;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;

public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
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

        var mainView = new MainView(data, calcService, persistenceService, csvImportService);

        var scene = new Scene(mainView.getRoot(), 1280, 860);
        var style = getClass().getResource("dark-theme.css").toExternalForm();
        if(style != null)
            scene.getStylesheets().add(style);

        primaryStage.setTitle("CountMyHours");
        var logo = getClass().getResourceAsStream("logo.png");
        if (logo != null)
            primaryStage.getIcons().add(new Image(logo));
        primaryStage.setScene(scene);
        primaryStage.show();
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
