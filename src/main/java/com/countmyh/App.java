package com.countmyh;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.view.MainView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

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

        var mainView = new MainView(data, calcService, persistenceService, csvImportService);

        var scene = new Scene(mainView.getRoot(), 1280, 860);
        scene.getStylesheets().add(
                getClass().getResource("dark-theme.css").toExternalForm()
        );

        primaryStage.setTitle("CountMyHours");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
