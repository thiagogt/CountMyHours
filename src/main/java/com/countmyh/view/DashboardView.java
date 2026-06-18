package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CalculationService;
import com.countmyh.util.ChartStyler;
import com.countmyh.util.ColorPalette;
import com.countmyh.util.MonthNames;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class DashboardView {

    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final VBox content;
    private VBox chartContainer;

    public DashboardView(WorkPeriodTracker data, CalculationService calcService) {
        this.data = data;
        this.calcService = calcService;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        build();
    }

    private void build() {
        var title = new Label("Dashboard");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildStatsGrid(), buildChartSection());
    }

    private Node buildStatsGrid() {
        double totalHours = calcService.getTotalHours(data);
        int projects = calcService.getTotalProjects(data);
        double monthlyAvg = calcService.getMonthlyAverage(data);
        double grossExtra = calcService.getTotalGrossExtra(data);
        double sold = calcService.getTotalSold(data);
        double net = calcService.getNetBalance(data);

        var yearlyTotals = new TreeMap<>(calcService.getYearlyTotals(data));
        String period = yearlyTotals.isEmpty() ? "—" :
                yearlyTotals.firstKey() + " - " + yearlyTotals.lastKey();

        var grid = new FlowPane(16, 16);
        grid.getChildren().addAll(
                statCard("Total Hours", String.format("%.0f", totalHours), String.format("%.0f working days", totalHours / 8)),
                statCard("Projects", String.valueOf(projects), "excluding admin"),
                statCard("Period", period, calcService.getMonthlyTotalWorked(data).size() + " months recorded"),
                statCard("Monthly Avg", String.format("%.0f", monthlyAvg), "hours/month"),
                statCard("Gross Extras", formatSigned(grossExtra), "before selling"),
                statCard("Hours Sold", String.format("%.0fh", sold), "total sold"),
                statCard("Net Balance", formatSigned(net), "official balance")
        );
        return grid;
    }

    private VBox statCard(String label, String value, String detail) {
        var lblLabel = new Label(label.toUpperCase());
        lblLabel.getStyleClass().add("stat-label");

        var lblValue = new Label(value);
        lblValue.getStyleClass().add("stat-value");

        var lblDetail = new Label(detail);
        lblDetail.getStyleClass().add("stat-detail");

        var card = new VBox(4, lblLabel, lblValue, lblDetail);
        card.getStyleClass().add("stat-card");
        card.setPrefWidth(170);
        card.setMinWidth(150);
        return card;
    }

    private Node buildChartSection() {
        chartContainer = new VBox(12);
        chartContainer.getStyleClass().add("chart-container");

        var chartTitle = new Label("Monthly Hours by Project");
        chartTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var filters = buildFilters();

        chartContainer.getChildren().addAll(chartTitle, filters);
        buildChart(0, 9999);

        return chartContainer;
    }

    private HBox buildFilters() {
        var group = new ToggleGroup();
        int[][] ranges = {{0, 9999}, {2017, 2019}, {2020, 2022}, {2023, 2024}, {2025, 2026}};
        String[] labels = {"All", "2017-2019", "2020-2022", "2023-2024", "2025-2026"};

        var box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        for (int i = 0; i < labels.length; i++) {
            var btn = new ToggleButton(labels[i]);
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            int startYear = ranges[i][0];
            int endYear = ranges[i][1];
            btn.setOnAction(e -> buildChart(startYear, endYear));
            if (i == 0) btn.setSelected(true);
            box.getChildren().add(btn);
        }
        return box;
    }

    private void buildChart(int startYear, int endYear) {
        // Remove old chart if exists
        chartContainer.getChildren().removeIf(n -> n instanceof StackedBarChart);

        Map<String, Map<YearMonth, Double>> byProject = calcService.getMonthlyHoursByProject(data, startYear, endYear);
        if (byProject.isEmpty()) {
            return;
        }

        var allMonths = new TreeSet<YearMonth>();
        byProject.values().forEach(m -> allMonths.addAll(m.keySet()));

        var xAxis = new CategoryAxis();
        var yAxis = new NumberAxis();
        yAxis.setLabel("Hours");

        var chart = new StackedBarChart<String, Number>(xAxis, yAxis);
        chart.setAnimated(false);
        chart.setPrefHeight(450);
        chart.setLegendVisible(true);
        chart.setCategoryGap(1);

        List<String> monthLabels = new ArrayList<>();
        for (YearMonth ym : allMonths) {
            monthLabels.add(MonthNames.label(ym.getYear(), ym.getMonthValue()));
        }
        xAxis.getCategories().addAll(monthLabels);

        for (var entry : byProject.entrySet()) {
            String project = entry.getKey();
            var series = new XYChart.Series<String, Number>();
            series.setName(project);

            for (YearMonth ym : allMonths) {
                double hours = entry.getValue().getOrDefault(ym, 0.0);
                series.getData().add(new XYChart.Data<>(
                        MonthNames.label(ym.getYear(), ym.getMonthValue()), hours
                ));
            }
            chart.getData().add(series);
        }

        ChartStyler.applyProjectColors(chart);
        chartContainer.getChildren().add(chart);
    }

    private String formatSigned(double value) {
        return (value >= 0 ? "+" : "") + String.format("%.0f", value);
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
