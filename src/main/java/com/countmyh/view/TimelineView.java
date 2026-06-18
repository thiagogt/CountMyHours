package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CalculationService;
import com.countmyh.util.ColorPalette;
import com.countmyh.util.MonthNames;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.util.Map;

public class TimelineView {

    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final VBox content;

    public TimelineView(WorkPeriodTracker data, CalculationService calcService) {
        this.data = data;
        this.calcService = calcService;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        build();
    }

    private void build() {
        var title = new Label("Timeline");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildYearlyChart(), buildProjectTable());
    }

    private Node buildYearlyChart() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var chartTitle = new Label("Yearly Totals");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        yAxis.setLabel("Hours");

        var chart = new BarChart<String, Number>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setPrefHeight(350);
        chart.setLegendVisible(false);
        chart.setCategoryGap(10);
        chart.setBarGap(2);

        var series = new XYChart.Series<String, Number>();
        Map<Integer, Double> yearlyTotals = calcService.getYearlyTotals(data);

        for (var entry : yearlyTotals.entrySet()) {
            series.getData().add(new XYChart.Data<>(String.valueOf(entry.getKey()), entry.getValue()));
        }
        chart.getData().add(series);

        javafx.application.Platform.runLater(() -> {
            int i = 0;
            for (var d : series.getData()) {
                if (d.getNode() != null) {
                    int hue = 240 + (i * 15);
                    d.getNode().setStyle("-fx-bar-fill: hsl(" + hue + ", 70%, 60%); -fx-background-radius: 4 4 0 0;");
                }
                i++;
            }
        });

        container.getChildren().addAll(chartTitle, chart);
        return container;
    }

    private Node buildProjectTable() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var tableTitle = new Label("Project Summary");
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var summaries = calcService.getProjectSummaries(data);
        double maxHours = summaries.stream().mapToDouble(CalculationService.ProjectSummary::totalHours).max().orElse(1);

        var table = new TableView<CalculationService.ProjectSummary>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var colProject = new TableColumn<CalculationService.ProjectSummary, String>("Project");
        colProject.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().project()));
        colProject.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    var dot = new Rectangle(10, 10);
                    dot.setStyle("-fx-fill: " + ColorPalette.getColor(item) + "; -fx-arc-width: 10; -fx-arc-height: 10;");
                    var label = new Label(item);
                    label.setStyle("-fx-text-fill: #e4e4e7;");
                    var box = new HBox(8, dot, label);
                    box.setAlignment(Pos.CENTER_LEFT);
                    setGraphic(box);
                }
            }
        });
        colProject.setPrefWidth(180);

        var colClient = new TableColumn<CalculationService.ProjectSummary, String>("Client");
        colClient.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().client()));
        colClient.setPrefWidth(100);

        var colPeriod = new TableColumn<CalculationService.ProjectSummary, String>("Period");
        colPeriod.setCellValueFactory(cd -> {
            var s = cd.getValue();
            String period = MonthNames.label(s.firstMonth().getYear(), s.firstMonth().getMonthValue())
                    + " - " + MonthNames.label(s.lastMonth().getYear(), s.lastMonth().getMonthValue());
            return new SimpleStringProperty(period);
        });
        colPeriod.setPrefWidth(160);

        var colMonths = new TableColumn<CalculationService.ProjectSummary, Number>("Months");
        colMonths.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().activeMonths()));
        colMonths.setPrefWidth(70);

        var colHours = new TableColumn<CalculationService.ProjectSummary, Number>("Total Hours");
        colHours.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().totalHours()));
        colHours.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    double hours = item.doubleValue();
                    double pct = hours / maxHours;

                    var summary = getTableRow().getItem();
                    String color = summary != null ? ColorPalette.getColor(summary.project()) : "#6366f1";

                    var bar = new Rectangle(pct * 250, 18);
                    bar.setStyle("-fx-fill: " + color + "; -fx-opacity: 0.7; -fx-arc-width: 4; -fx-arc-height: 4;");

                    var label = new Label(String.format("%.0fh", hours));
                    label.setStyle("-fx-text-fill: #e4e4e7; -fx-font-weight: bold; -fx-font-size: 12px;");

                    var stack = new StackPane(bar, label);
                    stack.setAlignment(Pos.CENTER_LEFT);
                    label.setPadding(new Insets(0, 0, 0, 6));
                    setGraphic(stack);
                }
            }
        });

        table.getColumns().addAll(colProject, colClient, colPeriod, colMonths, colHours);
        summaries.forEach(s -> table.getItems().add(s));
        table.setPrefHeight(40 + summaries.size() * 36);

        container.getChildren().addAll(tableTitle, table);
        return container;
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
