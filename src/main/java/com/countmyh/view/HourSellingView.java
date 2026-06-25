package com.countmyh.view;

import com.countmyh.model.WorkHourSelling;
import com.countmyh.model.WorkPeriodTracker;
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
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Map;
import java.util.TreeMap;

public class HourSellingView {

    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final JsonPersistenceService persistenceService;
    private final VBox content;
    private final StackPane rootStack;
    private FlowPane cardsPane;
    private boolean monthlyMode = true;
    private int currentStartYear;
    private int currentEndYear;

    public HourSellingView(WorkPeriodTracker data, CalculationService calcService,
                           JsonPersistenceService persistenceService) {
        this.data = data;
        this.calcService = calcService;
        this.persistenceService = persistenceService;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        this.rootStack = new StackPane();
        build();
    }

    private void build() {
        var title = new Label(I18n.get("selling.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var controls = buildControls();
        cardsPane = new FlowPane(12, 12);
        cardsPane.setPadding(new Insets(8, 0, 0, 0));

        container.getChildren().addAll(controls, cardsPane);
        buildCards();

        content.getChildren().addAll(title, container);
    }

    private VBox buildControls() {
        var modeGroup = new ToggleGroup();

        var monthlyBtn = new ToggleButton(I18n.get("selling.mode.monthly"));
        monthlyBtn.getStyleClass().add("filter-button");
        monthlyBtn.setToggleGroup(modeGroup);
        monthlyBtn.setSelected(true);

        var yearlyBtn = new ToggleButton(I18n.get("selling.mode.yearly"));
        yearlyBtn.getStyleClass().add("filter-button");
        yearlyBtn.setToggleGroup(modeGroup);

        monthlyBtn.setOnAction(e -> { monthlyMode = true; buildCards(); });
        yearlyBtn.setOnAction(e -> { monthlyMode = false; buildCards(); });

        var modeRow = new HBox(8, monthlyBtn, yearlyBtn);
        modeRow.setAlignment(Pos.CENTER_LEFT);

        var yearRow = buildYearFilter();
        return new VBox(8, modeRow, yearRow);
    }

    private HBox buildYearFilter() {
        var group = new ToggleGroup();
        var box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);

        var allBtn = new ToggleButton(I18n.get("dashboard.all"));
        allBtn.getStyleClass().add("filter-button");
        allBtn.setToggleGroup(group);
        allBtn.setOnAction(e -> { currentStartYear = 0; currentEndYear = 9999; buildCards(); });
        box.getChildren().add(allBtn);

        var yearlyTotals = new TreeMap<>(calcService.getYearlyTotals(data));
        int curYear = LocalDate.now().getYear();
        ToggleButton defaultBtn = allBtn;

        for (int year : yearlyTotals.keySet()) {
            var btn = new ToggleButton(String.valueOf(year));
            btn.getStyleClass().add("filter-button");
            btn.setToggleGroup(group);
            btn.setOnAction(e -> { currentStartYear = year; currentEndYear = year; buildCards(); });
            box.getChildren().add(btn);
            if (year == curYear) defaultBtn = btn;
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

    private void buildCards() {
        cardsPane.getChildren().clear();
        if (monthlyMode) buildMonthlyCards();
        else buildYearlyCards();
    }

    private void buildMonthlyCards() {
        Map<YearMonth, Double> workedByMonth = calcService.getMonthlyTotalWorked(data);
        for (var entry : workedByMonth.entrySet()) {
            YearMonth ym = entry.getKey();
            if (ym.getYear() < currentStartYear || ym.getYear() > currentEndYear) continue;
            cardsPane.getChildren().add(buildMonthCard(ym, entry.getValue()));
        }
        if (cardsPane.getChildren().isEmpty()) {
            var empty = new Label(I18n.get("selling.no.data"));
            empty.setStyle("-fx-text-fill: #8b8d97;");
            cardsPane.getChildren().add(empty);
        }
    }

    private void buildYearlyCards() {
        Map<Integer, Double> workedByYear = new TreeMap<>();
        for (var entry : calcService.getMonthlyTotalWorked(data).entrySet()) {
            workedByYear.merge(entry.getKey().getYear(), entry.getValue(), Double::sum);
        }
        for (var entry : workedByYear.entrySet()) {
            int year = entry.getKey();
            if (year < currentStartYear || year > currentEndYear) continue;
            cardsPane.getChildren().add(buildYearCard(year, entry.getValue()));
        }
        if (cardsPane.getChildren().isEmpty()) {
            var empty = new Label(I18n.get("selling.no.data"));
            empty.setStyle("-fx-text-fill: #8b8d97;");
            cardsPane.getChildren().add(empty);
        }
    }

    private Node buildMonthCard(YearMonth ym, double worked) {
        var card = new VBox(4);
        card.setPrefWidth(210);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #1a1d27; -fx-border-color: #2a2d3a; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");

        var header = new Label(MonthNames.label(ym.getYear(), ym.getMonthValue()));
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var workedRow = buildRow(I18n.get("selling.worked"), String.format("%.0fh", worked), "#e4e4e7");

        var sep = separator();

        double currentSold = data.getMonthSelling(ym.getYear(), ym.getMonthValue());
        var soldSpinner = buildSpinner(0, 10000, currentSold);
        soldSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;
            data.setMonthSelling(ym.getYear(), ym.getMonthValue(), newVal);
            saveQuietly();
        });
        var soldRow = buildSpinnerRow(I18n.get("selling.sold"), soldSpinner, "h");

        card.getChildren().addAll(header, workedRow, sep, soldRow);
        return card;
    }

    private Node buildYearCard(int year, double worked) {
        var card = new VBox(4);
        card.setPrefWidth(210);
        card.setPadding(new Insets(12));
        card.setStyle("-fx-background-color: #1a1d27; -fx-border-color: #2a2d3a; "
                + "-fx-border-radius: 10; -fx-background-radius: 10;");

        var header = new Label(String.valueOf(year));
        header.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var workedRow = buildRow(I18n.get("selling.worked"), String.format("%.0fh", worked), "#e4e4e7");

        var sep = separator();

        WorkHourSelling existing = data.getYearlySelling(year);
        double[] sold = {existing != null ? existing.hoursSold() : 0};
        double[] vac = {existing != null ? existing.vacationDaysSold() : 0};
        String[] note = {existing != null && existing.note() != null ? existing.note() : ""};

        var soldSpinner = buildSpinner(0, 100000, sold[0]);
        soldSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;
            sold[0] = newVal;
            data.setYearlySelling(year, sold[0], vac[0], note[0].isEmpty() ? null : note[0]);
            saveQuietly();
        });

        var vacSpinner = buildSpinner(0, 90, vac[0]);
        vacSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal == null || newVal.equals(oldVal)) return;
            vac[0] = newVal;
            data.setYearlySelling(year, sold[0], vac[0], note[0].isEmpty() ? null : note[0]);
            saveQuietly();
        });

        var noteField = new TextField(note[0]);
        noteField.setPromptText(I18n.get("selling.note.prompt"));
        noteField.setStyle("-fx-font-size: 10px; -fx-padding: 3 6; -fx-background-color: #0f1117; "
                + "-fx-text-fill: #f59e0b; -fx-border-color: #2a2d3a; -fx-border-radius: 4; -fx-background-radius: 4;");
        noteField.setPrefHeight(24);
        noteField.focusedProperty().addListener((obs, wasFocused, isFocused) -> {
            if (!isFocused) {
                note[0] = noteField.getText();
                data.setYearlySelling(year, sold[0], vac[0], note[0].isEmpty() ? null : note[0]);
                saveQuietly();
            }
        });

        card.getChildren().addAll(
                header,
                workedRow,
                sep,
                buildSpinnerRow(I18n.get("selling.sold"), soldSpinner, "h"),
                buildSpinnerRow(I18n.get("selling.vac.days"), vacSpinner, "d"),
                noteField
        );
        return card;
    }

    private Spinner<Double> buildSpinner(double min, double max, double initial) {
        var spinner = new Spinner<Double>();
        spinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(min, max, initial, 0.5));
        spinner.setPrefWidth(80);
        spinner.setPrefHeight(22);
        spinner.setStyle("-fx-font-size: 11px;");
        spinner.setEditable(true);
        return spinner;
    }

    private HBox buildSpinnerRow(String label, Spinner<Double> spinner, String unit) {
        var lbl = new Label(label);
        lbl.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");
        lbl.setMinWidth(70);

        var unitLabel = new Label(unit);
        unitLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");

        var row = new HBox(4, lbl, spinner, unitLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
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

    private Label separator() {
        var sep = new Label("");
        sep.setStyle("-fx-border-color: #2a2d3a; -fx-border-width: 1 0 0 0; -fx-padding: 2 0 0 0;");
        sep.setMaxWidth(Double.MAX_VALUE);
        return sep;
    }

    private void saveQuietly() {
        try {
            persistenceService.save(data);
        } catch (IOException ex) {
            Toast.show(rootStack, I18n.get("toast.save.failed", ex.getMessage()), Toast.Type.ERROR);
        }
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        rootStack.getChildren().add(scrollPane);
        return rootStack;
    }
}
