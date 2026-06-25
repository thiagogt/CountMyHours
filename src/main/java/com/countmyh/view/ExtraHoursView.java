package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.util.I18n;
import com.countmyh.util.MonthNames;
import com.countmyh.util.Toast;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public class ExtraHoursView {

    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final BusinessDayService businessDayService;
    private final JsonPersistenceService persistenceService;
    private final VBox content;
    private final StackPane rootStack;
    private FlowPane cardsPane;
    private int currentStartYear;
    private int currentEndYear;

    public ExtraHoursView(WorkPeriodTracker data, CalculationService calcService,
                          BusinessDayService businessDayService, JsonPersistenceService persistenceService) {
        this.data = data;
        this.calcService = calcService;
        this.businessDayService = businessDayService;
        this.persistenceService = persistenceService;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        this.rootStack = new StackPane();
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
        buildCards(currentStartYear, currentEndYear);
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
        int curYear = java.time.LocalDate.now().getYear();
        ToggleButton defaultBtn = allBtn;

        for (int year : yearlyTotals.keySet()) {
            var btn = new ToggleButton(String.valueOf(year));
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            btn.setOnAction(e -> buildCards(year, year));
            box.getChildren().add(btn);
            if (year == curYear) {
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
            currentStartYear = y;
            currentEndYear = y;
        } else {
            currentStartYear = 0;
            currentEndYear = 9999;
        }

        return box;
    }

    private void buildCards(int startYear, int endYear) {
        currentStartYear = startYear;
        currentEndYear = endYear;
        cardsPane.getChildren().clear();

        Map<YearMonth, CalculationService.MonthlyBalance> balance = calcService.getMonthlyBalance(data);
        double runningExtra = 0;

        for (var entry : balance.entrySet()) {
            YearMonth ym = entry.getKey();
            if (ym.getYear() < startYear || ym.getYear() > endYear) continue;

            var mb = entry.getValue();
            int totalBusinessDays = businessDayService.getBusinessDays(ym.getYear(), ym.getMonthValue());
            runningExtra += mb.extra();

            cardsPane.getChildren().add(buildMonthCard(ym, mb, totalBusinessDays, runningExtra));
        }

        if (cardsPane.getChildren().isEmpty()) {
            var empty = new Label(I18n.get("timeline.no.data"));
            empty.setStyle("-fx-text-fill: #8b8d97;");
            cardsPane.getChildren().add(empty);
        }
    }

    private Node buildMonthCard(YearMonth ym, CalculationService.MonthlyBalance mb, int businessDays, double accumulated) {
        var card = new VBox(4);
        card.setPrefWidth(210);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #1a1d27; -fx-border-color: #2a2d3a; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");

        var header = new Label(MonthNames.label(ym.getYear(), ym.getMonthValue()));
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var autoHolidays = businessDayService.getHolidaysInMonth(ym.getYear(), ym.getMonthValue());
        var monthNote = data.getMonthNote(ym.getYear(), ym.getMonthValue());
        double holidayCount = monthNote != null ? monthNote.holidays() : autoHolidays.size();
        String defaultObs = autoHolidays.stream()
                .map(e -> e.getKey().format(DateTimeFormatter.ofPattern("dd")) + " " + e.getValue())
                .collect(Collectors.joining(", "));
        String observation = monthNote != null ? monthNote.observation() : defaultObs;

        double effectiveDays = businessDays - (holidayCount - autoHolidays.size()) - mb.vacationDays();
        var workedRow = buildRow(I18n.get("extra.worked"), String.format("%.0fh", mb.worked()), "#e4e4e7");
        String daysStr = effectiveDays % 1 == 0 ? String.format("%.0fd", effectiveDays) : String.format("%.1fd", effectiveDays);
        var expectedRow = buildRow(I18n.get("extra.expected"), String.format("%.0fh (%s)", mb.expected(), daysStr), "#8b8d97");

        var holidayRow = buildHolidayRow(ym, holidayCount);
        var obsRow = buildObservationRow(ym, observation, holidayCount);
        var vacationRow = buildVacationRow(ym, mb.vacationDays());

        String extraColor = mb.extra() >= 0 ? "#22c55e" : "#ef4444";
        String extraSign = mb.extra() >= 0 ? "+" : "";
        var extraRow = buildRow(I18n.get("extra.extra"), extraSign + String.format("%.0fh", mb.extra()), extraColor);

        var sep = new Label("");
        sep.setStyle("-fx-border-color: #2a2d3a; -fx-border-width: 1 0 0 0; -fx-padding: 2 0 0 0;");
        sep.setMaxWidth(Double.MAX_VALUE);

        String accColor = accumulated >= 0 ? "#22c55e" : "#ef4444";
        String accSign = accumulated >= 0 ? "+" : "";
        var accRow = buildRow(I18n.get("extra.accumulated"), accSign + String.format("%.0fh", accumulated), accColor);

        card.getChildren().addAll(header, workedRow, expectedRow, holidayRow, obsRow, vacationRow, extraRow, sep, accRow);
        return card;
    }

    private HBox buildHolidayRow(YearMonth ym, double currentCount) {
        var lbl = new Label(I18n.get("extra.holidays"));
        lbl.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");
        lbl.setMinWidth(70);

        var spinner = new Spinner<Double>();
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 15, currentCount, 0.5));
        spinner.setPrefWidth(65);
        spinner.setPrefHeight(22);
        spinner.setStyle("-fx-font-size: 11px;");
        spinner.setEditable(true);

        var dayLabel = new Label("d");
        dayLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");

        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;
            var existing = data.getMonthNote(ym.getYear(), ym.getMonthValue());
            String currentObs = existing != null ? existing.observation() : "";
            data.setMonthNote(ym.getYear(), ym.getMonthValue(), newVal, currentObs);
            saveAndRefresh();
        });

        var row = new HBox(4, lbl, spinner, dayLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Node buildObservationRow(YearMonth ym, String observation, double holidayCount) {
        var field = new TextField(observation);
        field.setPromptText(I18n.get("extra.obs.prompt"));
        field.setStyle("-fx-font-size: 10px; -fx-padding: 3 6; -fx-background-color: #0f1117; "
                + "-fx-text-fill: #f59e0b; -fx-border-color: #2a2d3a; -fx-border-radius: 4; -fx-background-radius: 4;");
        field.setPrefHeight(24);

        field.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                data.setMonthNote(ym.getYear(), ym.getMonthValue(), holidayCount, field.getText());
                saveQuietly();
            }
        });

        return field;
    }

    private HBox buildVacationRow(YearMonth ym, double currentDays) {
        var lbl = new Label(I18n.get("extra.vacation"));
        lbl.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px; -fx-cursor: hand;");
        lbl.setMinWidth(70);
        Tooltip.install(lbl, new Tooltip(I18n.get("extra.vacation.tip")));

        var spinner = new Spinner<Double>();
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0, 30, currentDays, 0.5));
        spinner.setPrefWidth(65);
        spinner.setPrefHeight(22);
        spinner.setStyle("-fx-font-size: 11px;");
        spinner.setEditable(true);

        var dayLabel = new Label("d");
        dayLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");

        spinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;
            data.setVacation(ym.getYear(), ym.getMonthValue(), newVal);
            saveAndRefresh();
        });

        var row = new HBox(4, lbl, spinner, dayLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void saveAndRefresh() {
        saveQuietly();
        buildCards(currentStartYear, currentEndYear);
    }

    private void saveQuietly() {
        try {
            persistenceService.save(data);
        } catch (IOException ex) {
            Toast.show(rootStack, I18n.get("toast.save.failed", ex.getMessage()), Toast.Type.ERROR);
        }
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
        rootStack.getChildren().add(scrollPane);
        return rootStack;
    }
}
