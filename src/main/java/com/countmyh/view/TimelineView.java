package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CalculationService;
import com.countmyh.util.ColorPalette;
import com.countmyh.util.I18n;
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
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;

import java.time.YearMonth;
import java.util.Comparator;
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
        var title = new Label(I18n.get("timeline.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildGanttChart(), buildYearlyChart(), buildProjectTable());
    }

    private Node buildGanttChart() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var chartTitle = new Label(I18n.get("timeline.projects"));
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var ranges = calcService.getProjectDateRanges(data);
        if (ranges.isEmpty()) {
            container.getChildren().addAll(chartTitle, new Label(I18n.get("timeline.no.data")));
            return container;
        }

        var summaries = calcService.getProjectSummaries(data).stream()
                .filter(s -> !s.project().equalsIgnoreCase("admin"))
                .sorted(Comparator.comparing(CalculationService.ProjectSummary::firstMonth))
                .toList();

        YearMonth globalMin = summaries.stream().map(CalculationService.ProjectSummary::firstMonth).min(Comparator.naturalOrder()).orElse(YearMonth.now());
        YearMonth globalMax = summaries.stream().map(CalculationService.ProjectSummary::lastMonth).max(Comparator.naturalOrder()).orElse(YearMonth.now());
        int totalMonths = (globalMax.getYear() - globalMin.getYear()) * 12 + globalMax.getMonthValue() - globalMin.getMonthValue() + 1;

        var grid = new GridPane();
        grid.setHgap(0);
        grid.setVgap(6);

        var nameCol = new javafx.scene.layout.ColumnConstraints();
        nameCol.setMinWidth(130);
        nameCol.setMaxWidth(130);
        grid.getColumnConstraints().add(nameCol);
        for (int i = 0; i < totalMonths; i++) {
            var col = new javafx.scene.layout.ColumnConstraints();
            col.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(col);
        }

        int firstYear = globalMin.getYear();
        int lastYear = globalMax.getYear();
        for (int y = firstYear; y <= lastYear; y++) {
            int startMonth = (y == firstYear) ? globalMin.getMonthValue() : 1;
            int endMonth = (y == lastYear) ? globalMax.getMonthValue() : 12;
            int span = endMonth - startMonth + 1;
            int gridCol = (YearMonth.of(y, startMonth).getYear() - globalMin.getYear()) * 12
                    + startMonth - globalMin.getMonthValue();

            var yearLabel = new Label(String.valueOf(y));
            yearLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 10px; -fx-font-weight: bold;");
            yearLabel.setAlignment(Pos.CENTER);
            yearLabel.setMaxWidth(Double.MAX_VALUE);
            GridPane.setColumnSpan(yearLabel, span);
            grid.add(yearLabel, 1 + gridCol, 0);
        }

        int barRow = 1;
        for (var summary : summaries) {
            String project = summary.project();
            String color = ColorPalette.getColor(project);

            var dot = new Rectangle(8, 8);
            dot.setStyle("-fx-fill: " + color + "; -fx-arc-width: 8; -fx-arc-height: 8;");
            var nameLabel = new Label(project);
            nameLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 12px;");
            var nameBox = new HBox(6, dot, nameLabel);
            nameBox.setAlignment(Pos.CENTER_LEFT);
            grid.add(nameBox, 0, barRow);

            int startOffset = (summary.firstMonth().getYear() - globalMin.getYear()) * 12
                    + summary.firstMonth().getMonthValue() - globalMin.getMonthValue();
            int duration = (summary.lastMonth().getYear() - summary.firstMonth().getYear()) * 12
                    + summary.lastMonth().getMonthValue() - summary.firstMonth().getMonthValue() + 1;

            var bar = new Pane();
            bar.setMinHeight(22);
            bar.setMaxHeight(22);
            bar.setStyle("-fx-background-color: " + color + "; -fx-background-radius: 6; -fx-opacity: 0.85;");

            String tip = MonthNames.label(summary.firstMonth().getYear(), summary.firstMonth().getMonthValue())
                    + " - " + MonthNames.label(summary.lastMonth().getYear(), summary.lastMonth().getMonthValue())
                    + " | " + String.format("%.0fh", summary.totalHours());
            Tooltip.install(bar, new Tooltip(tip));

            GridPane.setColumnSpan(bar, Math.max(duration, 1));
            grid.add(bar, 1 + startOffset, barRow);

            barRow++;
        }

        grid.setPadding(new Insets(8, 0, 8, 0));
        container.getChildren().addAll(chartTitle, grid);
        return container;
    }

    private Node buildYearlyChart() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var chartTitle = new Label(I18n.get("timeline.yearly.totals"));
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        yAxis.setLabel(I18n.get("dashboard.hours.label"));

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
                    String hex = hslToHex(hue, 0.7, 0.6);
                    d.getNode().setStyle("-fx-bar-fill: " + hex + "; -fx-background-radius: 4 4 0 0;");
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

        var tableTitle = new Label(I18n.get("timeline.project.summary"));
        tableTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var summaries = calcService.getProjectSummaries(data);
        double maxHours = summaries.stream().mapToDouble(CalculationService.ProjectSummary::totalHours).max().orElse(1);

        var table = new TableView<CalculationService.ProjectSummary>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        var colProject = new TableColumn<CalculationService.ProjectSummary, String>(I18n.get("timeline.col.project"));
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

        var colClient = new TableColumn<CalculationService.ProjectSummary, String>(I18n.get("timeline.col.client"));
        colClient.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().client()));
        colClient.setPrefWidth(100);

        var colPeriod = new TableColumn<CalculationService.ProjectSummary, String>(I18n.get("timeline.col.period"));
        colPeriod.setCellValueFactory(cd -> {
            var s = cd.getValue();
            String period = MonthNames.label(s.firstMonth().getYear(), s.firstMonth().getMonthValue())
                    + " - " + MonthNames.label(s.lastMonth().getYear(), s.lastMonth().getMonthValue());
            return new SimpleStringProperty(period);
        });
        colPeriod.setPrefWidth(160);

        var colMonths = new TableColumn<CalculationService.ProjectSummary, Number>(I18n.get("timeline.col.months"));
        colMonths.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().activeMonths()));
        colMonths.setPrefWidth(70);

        var colHours = new TableColumn<CalculationService.ProjectSummary, Number>(I18n.get("timeline.col.total.hours"));
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

    private static String hslToHex(int h, double s, double l) {
        double c = (1 - Math.abs(2 * l - 1)) * s;
        double x = c * (1 - Math.abs((h / 60.0) % 2 - 1));
        double m = l - c / 2;
        double r, g, b;
        if (h < 60)       { r = c; g = x; b = 0; }
        else if (h < 120) { r = x; g = c; b = 0; }
        else if (h < 180) { r = 0; g = c; b = x; }
        else if (h < 240) { r = 0; g = x; b = c; }
        else if (h < 300) { r = x; g = 0; b = c; }
        else              { r = c; g = 0; b = x; }
        int ri = (int) Math.round((r + m) * 255);
        int gi = (int) Math.round((g + m) * 255);
        int bi = (int) Math.round((b + m) * 255);
        return String.format("#%02x%02x%02x", ri, gi, bi);
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
