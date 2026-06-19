package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CalculationService;
import com.countmyh.util.I18n;
import com.countmyh.util.MonthNames;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

public class ExtraHoursView {

    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final VBox content;
    private FlowPane cardsPane;
    private int initialStartYear;
    private int initialEndYear;

    public ExtraHoursView(WorkPeriodTracker data, CalculationService calcService) {
        this.data = data;
        this.calcService = calcService;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        build();
    }

    private void build() {
        var title = new Label(I18n.get("extra.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("extra.monthly.balance"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var filters = buildFilters();

        cardsPane = new FlowPane(12, 12);
        cardsPane.setPadding(new Insets(8, 0, 0, 0));

        container.getChildren().addAll(sectionTitle, filters);
        buildCards(initialStartYear, initialEndYear);
        container.getChildren().add(cardsPane);

        content.getChildren().addAll(title, container);
    }

    private HBox buildFilters() {
        var group = new ToggleGroup();
        var box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        var allBtn = new ToggleButton(I18n.get("dashboard.all"));
        allBtn.getStyleClass().add("filter-button");
        allBtn.setToggleGroup(group);
        allBtn.setOnAction(e -> buildCards(0, 9999));
        box.getChildren().add(allBtn);

        var yearlyTotals = new TreeMap<>(calcService.getYearlyTotals(data));
        int currentYear = java.time.LocalDate.now().getYear();
        ToggleButton defaultBtn = allBtn;

        for (int year : yearlyTotals.keySet()) {
            var btn = new ToggleButton(String.valueOf(year));
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            btn.setOnAction(e -> buildCards(year, year));
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

    private void buildCards(int startYear, int endYear) {
        cardsPane.getChildren().clear();

        Map<YearMonth, CalculationService.MonthlyBalance> balance = calcService.getMonthlyBalance(data);
        double runningExtra = 0;

        for (var entry : balance.entrySet()) {
            YearMonth ym = entry.getKey();
            if (ym.getYear() < startYear || ym.getYear() > endYear) continue;

            var mb = entry.getValue();
            int days = (int) (mb.expected() / 8);
            runningExtra += mb.extra();

            cardsPane.getChildren().add(buildMonthCard(ym, mb, days, runningExtra));
        }

        if (cardsPane.getChildren().isEmpty()) {
            var empty = new Label(I18n.get("timeline.no.data"));
            empty.setStyle("-fx-text-fill: #8b8d97;");
            cardsPane.getChildren().add(empty);
        }
    }

    private Node buildMonthCard(YearMonth ym, CalculationService.MonthlyBalance mb, int businessDays, double accumulated) {
        var card = new VBox(4);
        card.setPrefWidth(175);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #1a1d27; -fx-border-color: #2a2d3a; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");

        var header = new Label(MonthNames.label(ym.getYear(), ym.getMonthValue()));
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var workedRow = buildRow(I18n.get("extra.worked"), String.format("%.0fh", mb.worked()), "#e4e4e7");
        var expectedRow = buildRow(I18n.get("extra.expected"), String.format("%.0fh (%dd)", mb.expected(), businessDays), "#8b8d97");

        String extraColor = mb.extra() >= 0 ? "#22c55e" : "#ef4444";
        String extraSign = mb.extra() >= 0 ? "+" : "";
        var extraRow = buildRow(I18n.get("extra.extra"), extraSign + String.format("%.0fh", mb.extra()), extraColor);

        var sep = new Label("");
        sep.setStyle("-fx-border-color: #2a2d3a; -fx-border-width: 1 0 0 0; -fx-padding: 2 0 0 0;");
        sep.setMaxWidth(Double.MAX_VALUE);

        String accColor = accumulated >= 0 ? "#22c55e" : "#ef4444";
        String accSign = accumulated >= 0 ? "+" : "";
        var accRow = buildRow(I18n.get("extra.accumulated"), accSign + String.format("%.0fh", accumulated), accColor);

        card.getChildren().addAll(header, workedRow, expectedRow, extraRow, sep, accRow);
        return card;
    }

    private HBox buildRow(String label, String value, String valueColor) {
        var lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");
        lbl.setMinWidth(70);

        var val = new Label(value);
        val.setStyle("-fx-text-fill: " + valueColor + "; -fx-font-size: 12px; -fx-font-weight: bold;");

        var row = new HBox(6, lbl, val);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
