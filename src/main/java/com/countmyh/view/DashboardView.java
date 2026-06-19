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
    private int initialStartYear;
    private int initialEndYear;

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

        chartContainer.getChildren().addAll(chartTitle, buildFilters());
        buildChart(initialStartYear, initialEndYear);

        return chartContainer;
    }

    private HBox buildFilters() {
        var group = new ToggleGroup();
        var box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        var allBtn = new ToggleButton("All");
        allBtn.getStyleClass().add("filter-button");
        allBtn.setToggleGroup(group);
        allBtn.setOnAction(e -> buildChart(0, 9999));
        box.getChildren().add(allBtn);

        var yearlyTotals = new TreeMap<>(calcService.getYearlyTotals(data));
        int currentYear = java.time.LocalDate.now().getYear();
        ToggleButton defaultBtn = allBtn;

        for (int year : yearlyTotals.keySet()) {
            var btn = new ToggleButton(String.valueOf(year));
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            btn.setOnAction(e -> buildChart(year, year));
            box.getChildren().add(btn);
            if (year == currentYear) {
                defaultBtn = btn;
            }
        }

        if (defaultBtn == allBtn && !yearlyTotals.isEmpty()) {
            int lastYear = yearlyTotals.lastKey();
            for (var node : box.getChildren()) {
                if (node instanceof ToggleButton tb && tb.getText().equals(String.valueOf(lastYear))) {
                    defaultBtn = tb;
                    break;
                }
            }
        }

        defaultBtn.setSelected(true);
        if (defaultBtn != allBtn) {
            int y = Integer.parseInt(defaultBtn.getText());
            initialStartYear = y;
            initialEndYear = y;
        } else {
            initialStartYear = 0;
            initialEndYear = 9999;
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
