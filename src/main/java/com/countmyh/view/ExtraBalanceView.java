package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CalculationService.ProjectExtra;
import com.countmyh.service.CalculationService.YearlyBalance;
import com.countmyh.util.ChartStyler;
import com.countmyh.util.I18n;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
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

import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class ExtraBalanceView {

    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final VBox content;
    private final StackPane rootStack;

    public ExtraBalanceView(WorkPeriodTracker data, CalculationService calcService) {
        this.data = data;
        this.calcService = calcService;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        this.rootStack = new StackPane();
        build();
    }

    private void build() {
        var title = new Label(I18n.get("extras.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var yearlyBalance = calcService.getYearlyBalance(data);
        var projectExtras = calcService.getExtraPerProject(data);

        if (yearlyBalance.isEmpty()) {
            var empty = new Label(I18n.get("extras.no.data"));
            empty.setStyle("-fx-text-fill: #8b8d97;");
            content.getChildren().addAll(title, empty);
            return;
        }

        content.getChildren().addAll(
                title,
                buildYearlyBalanceSection(yearlyBalance),
                buildProjectExtrasSection(projectExtras)
        );
    }

    // ── Yearly Balance ─────────────────────────────────────────────────────────

    private Node buildYearlyBalanceSection(Map<Integer, YearlyBalance> balance) {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("extras.yearly.title"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        container.getChildren().addAll(sectionTitle, buildYearlyBalanceChart(balance));
        return container;
    }

    private BarChart<String, Number> buildYearlyBalanceChart(Map<Integer, YearlyBalance> balance) {
        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        yAxis.setLabel(I18n.get("extras.hours.label"));

        var chart = new BarChart<String, Number>(xAxis, yAxis);
        chart.setPrefHeight(320);
        chart.setAnimated(false);
        chart.setBarGap(2);
        chart.setCategoryGap(12);
        applyChartStyle(chart);

        var grossSeries = new XYChart.Series<String, Number>();
        grossSeries.setName(I18n.get("extras.gross"));

        var soldSeries = new XYChart.Series<String, Number>();
        soldSeries.setName(I18n.get("extras.sold"));

        var netSeries = new XYChart.Series<String, Number>();
        netSeries.setName(I18n.get("extras.net"));

        new TreeMap<>(balance).forEach((year, yb) -> {
            String y = String.valueOf(year);
            grossSeries.getData().add(new XYChart.Data<>(y, yb.gross()));
            soldSeries.getData().add(new XYChart.Data<>(y, yb.sold() > 0 ? -yb.sold() : 0.0));
            netSeries.getData().add(new XYChart.Data<>(y, yb.net()));
        });

        chart.getData().addAll(grossSeries, soldSeries, netSeries);

        // Double runLater ensures bars are laid out before styling
        Platform.runLater(() -> Platform.runLater(() -> {
            colorSeriesUniform(chart, 0, "#6366f1");
            colorSeriesUniform(chart, 1, "#f97316");
            colorNetBars(chart, 2);
            colorLegendSymbol(chart, 0, "#6366f1");
            colorLegendSymbol(chart, 1, "#f97316");
            colorLegendSymbol(chart, 2, "#22c55e");
        }));

        return chart;
    }

    // ── Project Extras ─────────────────────────────────────────────────────────

    private Node buildProjectExtrasSection(Map<String, ProjectExtra> extras) {
        var container = new VBox(16);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("extras.project.title"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var hBarChart = buildProjectHBarChart(extras);
        var stackedChart = buildProjectYearlyStackedChart(extras);

        var chartsRow = new HBox(16, hBarChart, stackedChart);
        HBox.setHgrow(hBarChart, Priority.ALWAYS);
        HBox.setHgrow(stackedChart, Priority.ALWAYS);

        container.getChildren().addAll(sectionTitle, chartsRow, buildProjectTable(extras));
        return container;
    }

    private BarChart<Number, String> buildProjectHBarChart(Map<String, ProjectExtra> extras) {
        var xAxis = new NumberAxis();
        xAxis.setLabel(I18n.get("extras.hours.label"));
        var yAxis = new CategoryAxis();

        var chart = new BarChart<Number, String>(xAxis, yAxis);
        chart.setPrefHeight(400);
        chart.setAnimated(false);
        chart.setBarGap(1);
        chart.setCategoryGap(8);
        applyChartStyle(chart);

        var grossSeries = new XYChart.Series<Number, String>();
        grossSeries.setName(I18n.get("extras.gross"));

        var soldSeries = new XYChart.Series<Number, String>();
        soldSeries.setName(I18n.get("extras.sold"));

        extras.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().grossExtra(), a.getValue().grossExtra()))
                .forEach(e -> {
                    String proj = e.getKey();
                    ProjectExtra pe = e.getValue();
                    grossSeries.getData().add(new XYChart.Data<>(pe.grossExtra(), proj));
                    soldSeries.getData().add(new XYChart.Data<>(pe.sold() > 0 ? -pe.sold() : 0.0, proj));
                });

        chart.getData().addAll(grossSeries, soldSeries);

        Platform.runLater(() -> Platform.runLater(() -> {
            colorGrossHBars(chart, 0);
            colorHSeriesUniform(chart, 1, "#f97316");
            colorLegendSymbol(chart, 0, "#22c55e");
            colorLegendSymbol(chart, 1, "#f97316");
        }));

        return chart;
    }

    private StackedBarChart<String, Number> buildProjectYearlyStackedChart(Map<String, ProjectExtra> extras) {
        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        yAxis.setLabel(I18n.get("extras.yearly.breakdown"));

        var chart = new StackedBarChart<String, Number>(xAxis, yAxis);
        chart.setPrefHeight(400);
        chart.setAnimated(false);
        applyChartStyle(chart);

        var allYears = new TreeSet<Integer>();
        extras.values().forEach(pe -> allYears.addAll(pe.yearlyBreakdown().keySet()));

        for (var entry : extras.entrySet()) {
            var series = new XYChart.Series<String, Number>();
            series.setName(entry.getKey());
            for (int year : allYears) {
                series.getData().add(new XYChart.Data<>(
                        String.valueOf(year),
                        entry.getValue().yearlyBreakdown().getOrDefault(year, 0.0)));
            }
            chart.getData().add(series);
        }

        Platform.runLater(() -> ChartStyler.applyProjectColors(chart));
        return chart;
    }

    private TableView<Map.Entry<String, ProjectExtra>> buildProjectTable(Map<String, ProjectExtra> extras) {
        var table = new TableView<Map.Entry<String, ProjectExtra>>();
        table.setStyle("-fx-background-color: #1a1d27;");
        table.setFixedCellSize(36);
        table.setPrefHeight(36.0 * (extras.size() + 1) + 14);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        var projCol = col(I18n.get("timeline.col.project"), 180, e -> e.getKey());
        var workedCol = col(I18n.get("extras.table.worked"), 110,
                e -> String.format("%.0fh", e.getValue().totalHours()));
        var grossCol = coloredCol(I18n.get("extras.table.gross"), 120, e -> {
            double v = e.getValue().grossExtra();
            return (v >= 0 ? "+" : "") + String.format("%.1fh", v);
        }, true);
        var soldCol = col(I18n.get("extras.table.sold"), 110,
                e -> e.getValue().sold() > 0 ? String.format("-%.1fh", e.getValue().sold()) : "-");
        var netCol = coloredCol(I18n.get("extras.table.net"), 110, e -> {
            double v = e.getValue().net();
            return (v >= 0 ? "+" : "") + String.format("%.1fh", v);
        }, false);

        table.getColumns().addAll(projCol, workedCol, grossCol, soldCol, netCol);
        table.getColumns().forEach(c -> c.setSortable(false));

        extras.entrySet().stream()
                .sorted((a, b) -> Double.compare(b.getValue().grossExtra(), a.getValue().grossExtra()))
                .forEach(table.getItems()::add);

        return table;
    }

    // ── Table column helpers ───────────────────────────────────────────────────

    private TableColumn<Map.Entry<String, ProjectExtra>, String> col(
            String header, double width,
            java.util.function.Function<Map.Entry<String, ProjectExtra>, String> valueMapper) {

        var col = new TableColumn<Map.Entry<String, ProjectExtra>, String>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(c -> new SimpleStringProperty(valueMapper.apply(c.getValue())));
        col.setStyle("-fx-alignment: CENTER-RIGHT;");
        return col;
    }

    private TableColumn<Map.Entry<String, ProjectExtra>, String> coloredCol(
            String header, double width,
            java.util.function.Function<Map.Entry<String, ProjectExtra>, String> valueMapper,
            boolean dimNegative) {

        var col = new TableColumn<Map.Entry<String, ProjectExtra>, String>(header);
        col.setPrefWidth(width);
        col.setCellValueFactory(c -> new SimpleStringProperty(valueMapper.apply(c.getValue())));
        col.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item);
                boolean positive = item.startsWith("+");
                String color = positive ? "#22c55e" : (dimNegative ? "#8b8d97" : "#ef4444");
                setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold; -fx-alignment: CENTER-RIGHT;");
            }
        });
        return col;
    }

    // ── Chart styling helpers ──────────────────────────────────────────────────

    private void applyChartStyle(BarChart<?, ?> chart) {
        chart.setStyle("-fx-background-color: transparent;");
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
    }

    private void applyChartStyle(StackedBarChart<?, ?> chart) {
        chart.setStyle("-fx-background-color: transparent;");
        chart.setAlternativeRowFillVisible(false);
        chart.setAlternativeColumnFillVisible(false);
        chart.setHorizontalGridLinesVisible(true);
        chart.setVerticalGridLinesVisible(false);
    }

    private void colorSeriesUniform(BarChart<String, Number> chart, int idx, String color) {
        if (idx >= chart.getData().size()) return;
        chart.getData().get(idx).getData().forEach(d -> {
            if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: " + color + ";");
        });
    }

    private void colorNetBars(BarChart<String, Number> chart, int idx) {
        if (idx >= chart.getData().size()) return;
        chart.getData().get(idx).getData().forEach(d -> {
            if (d.getNode() == null) return;
            boolean pos = d.getYValue().doubleValue() >= 0;
            d.getNode().setStyle("-fx-bar-fill: " + (pos ? "#22c55e" : "#ef4444") + ";");
        });
    }

    private void colorHSeriesUniform(BarChart<Number, String> chart, int idx, String color) {
        if (idx >= chart.getData().size()) return;
        chart.getData().get(idx).getData().forEach(d -> {
            if (d.getNode() != null) d.getNode().setStyle("-fx-bar-fill: " + color + ";");
        });
    }

    private void colorGrossHBars(BarChart<Number, String> chart, int idx) {
        if (idx >= chart.getData().size()) return;
        chart.getData().get(idx).getData().forEach(d -> {
            if (d.getNode() == null) return;
            boolean pos = d.getXValue().doubleValue() >= 0;
            d.getNode().setStyle("-fx-bar-fill: " + (pos ? "#22c55e" : "#ef4444") + ";");
        });
    }

    private void colorLegendSymbol(BarChart<?, ?> chart, int idx, String color) {
        Node sym = chart.lookup(".chart-legend-item-symbol.series" + idx);
        if (sym != null) sym.setStyle("-fx-background-color: " + color + ";");
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rootStack.getChildren().add(scrollPane);
        return rootStack;
    }
}
