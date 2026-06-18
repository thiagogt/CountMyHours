package com.countmyh.view;

import com.countmyh.model.WorkHourItem;
import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class DataEntryView {

    private final WorkPeriodTracker data;
    private final CsvImportService csvImportService;
    private final JsonPersistenceService persistenceService;
    private final Runnable onDataChanged;
    private final VBox content;

    private static final String CSV_HEADER = "Data;Cliente;Projeto;Item;Hs";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private Label statusLabel;
    private TableView<WorkHourItem> table;
    private ComboBox<String> filterYear;
    private ComboBox<String> filterProject;
    private File lastOpenedSpreadsheet;

    public DataEntryView(WorkPeriodTracker data, CsvImportService csvImportService,
                         JsonPersistenceService persistenceService, Runnable onDataChanged) {
        this.data = data;
        this.csvImportService = csvImportService;
        this.persistenceService = persistenceService;
        this.onDataChanged = onDataChanged;
        this.content = new VBox(20);
        this.content.setPadding(new Insets(24));
        build();
    }

    private void build() {
        var title = new Label("Data");
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildImportSection(), buildFilters(), buildTable(), buildStatusSection());
    }

    private Node buildImportSection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label("Import Data");
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var btnImport = new Button("Import CSV / XLSX");
        btnImport.setOnAction(e -> handleImport());

        var btnNewSheet = new Button("New Spreadsheet");
        btnNewSheet.setStyle("-fx-background-color: #10b981;");
        btnNewSheet.setOnAction(e -> handleNewSpreadsheet());

        var btnExportEdit = new Button("Export & Edit");
        btnExportEdit.setStyle("-fx-background-color: #f59e0b;");
        btnExportEdit.setOnAction(e -> handleExportAndEdit());

        var btnReimport = new Button("Reimport Last");
        btnReimport.setStyle("-fx-background-color: #8b5cf6;");
        btnReimport.setOnAction(e -> handleReimportLast());

        var desc = new Label("New Spreadsheet: creates a template CSV and opens it in your spreadsheet app.  " +
                "Export & Edit: exports current entries to CSV for editing.  " +
                "After editing, use Import or Reimport Last.");
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px; -fx-wrap-text: true;");
        desc.setWrapText(true);

        var info = new VBox(4);
        if (data.getLastImportDate() != null) {
            info.getChildren().add(new Label("Last import: " +
                    data.getLastImportDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))));
        }
        if (data.getLastSourceFile() != null) {
            var fileLabel = new Label("Source: " + data.getLastSourceFile());
            fileLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");
            info.getChildren().add(fileLabel);
        }

        var totalLabel = new Label("Total entries: " + data.getEntries().size());
        totalLabel.setStyle("-fx-text-fill: #8b8d97;");
        info.getChildren().add(totalLabel);

        var buttons = new HBox(12, btnNewSheet, btnExportEdit, btnImport, btnReimport);
        buttons.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(sectionTitle, buttons, desc, info);
        return container;
    }

    private Node buildFilters() {
        filterYear = new ComboBox<>();
        filterYear.getItems().add("All years");
        data.getEntries().stream()
                .map(e -> String.valueOf(e.getYear()))
                .distinct()
                .sorted()
                .forEach(y -> filterYear.getItems().add(y));
        filterYear.setValue("All years");
        filterYear.setOnAction(e -> applyFilters());

        filterProject = new ComboBox<>();
        filterProject.getItems().add("All projects");
        data.getEntries().stream()
                .map(WorkHourItem::getProject)
                .distinct()
                .sorted()
                .forEach(p -> filterProject.getItems().add(p));
        filterProject.setValue("All projects");
        filterProject.setOnAction(e -> applyFilters());

        var box = new HBox(12,
                new Label("Year:"), filterYear,
                new Label("Project:"), filterProject
        );
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Node buildTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(500);

        var colDate = new TableColumn<WorkHourItem, String>("Date");
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        ));
        colDate.setPrefWidth(100);

        var colClient = new TableColumn<WorkHourItem, String>("Client");
        colClient.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getClient()));
        colClient.setPrefWidth(100);

        var colProject = new TableColumn<WorkHourItem, String>("Project");
        colProject.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getProject()));
        colProject.setPrefWidth(140);

        var colItem = new TableColumn<WorkHourItem, String>("Item");
        colItem.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().getItem()));

        var colHours = new TableColumn<WorkHourItem, Number>("Hours");
        colHours.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().getHours()));
        colHours.setPrefWidth(70);

        table.getColumns().addAll(colDate, colClient, colProject, colItem, colHours);
        applyFilters();

        return table;
    }

    private void applyFilters() {
        if (table == null) return;

        String yearFilter = filterYear.getValue();
        String projectFilter = filterProject.getValue();

        List<WorkHourItem> filtered = data.getEntries().stream()
                .filter(e -> "All years".equals(yearFilter) || String.valueOf(e.getYear()).equals(yearFilter))
                .filter(e -> "All projects".equals(projectFilter) || e.getProject().equals(projectFilter))
                .sorted(Comparator.comparing(WorkHourItem::getDate).reversed())
                .collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }

    private Node buildStatusSection() {
        statusLabel = new Label("");
        statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
        return statusLabel;
    }

    private void handleImport() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Import work hours");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("CSV / XLSX", "*.csv", "*.xlsx"),
                new FileChooser.ExtensionFilter("CSV files", "*.csv"),
                new FileChooser.ExtensionFilter("Excel files", "*.xlsx")
        );

        File file = fileChooser.showOpenDialog(content.getScene().getWindow());
        if (file == null) return;

        try {
            var items = csvImportService.importFile(file);
            int added = data.addEntriesWithDedup(items);
            data.setLastImportDate(LocalDateTime.now());
            data.setLastSourceFile(file.getAbsolutePath());
            persistenceService.save(data);

            statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            statusLabel.setText("Imported " + added + " new entries (" + items.size() + " total in file)");

            // Refresh filters and table
            refreshFilters();
            applyFilters();
            onDataChanged.run();
        } catch (Exception ex) {
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            statusLabel.setText("Import failed: " + ex.getMessage());
        }
    }

    private void refreshFilters() {
        String currentYear = filterYear.getValue();
        String currentProject = filterProject.getValue();

        filterYear.getItems().clear();
        filterYear.getItems().add("All years");
        data.getEntries().stream()
                .map(e -> String.valueOf(e.getYear()))
                .distinct().sorted()
                .forEach(y -> filterYear.getItems().add(y));
        filterYear.setValue(filterYear.getItems().contains(currentYear) ? currentYear : "All years");

        filterProject.getItems().clear();
        filterProject.getItems().add("All projects");
        data.getEntries().stream()
                .map(WorkHourItem::getProject)
                .distinct().sorted()
                .forEach(p -> filterProject.getItems().add(p));
        filterProject.setValue(filterProject.getItems().contains(currentProject) ? currentProject : "All projects");
    }

    private void handleNewSpreadsheet() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Create new spreadsheet");
        fileChooser.setInitialFileName("hours_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM")) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(content.getScene().getWindow());
        if (file == null) return;

        try {
            writeCsvTemplate(file);
            lastOpenedSpreadsheet = file;
            openInSpreadsheetApp(file);

            statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            statusLabel.setText("Spreadsheet created: " + file.getName() + " — edit, save, then use Import or Reimport Last");
        } catch (IOException ex) {
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            statusLabel.setText("Failed to create spreadsheet: " + ex.getMessage());
        }
    }

    private void handleExportAndEdit() {
        if (data.getEntries().isEmpty()) {
            statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            statusLabel.setText("No entries to export");
            return;
        }

        var fileChooser = new FileChooser();
        fileChooser.setTitle("Export entries to CSV");
        fileChooser.setInitialFileName("hours_export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("CSV files", "*.csv")
        );

        File file = fileChooser.showSaveDialog(content.getScene().getWindow());
        if (file == null) return;

        try {
            writeEntriesToCsv(file);
            lastOpenedSpreadsheet = file;
            openInSpreadsheetApp(file);

            statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            statusLabel.setText("Exported " + data.getEntries().size() + " entries to " + file.getName() + " — edit, save, then Reimport");
        } catch (IOException ex) {
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            statusLabel.setText("Export failed: " + ex.getMessage());
        }
    }

    private void handleReimportLast() {
        if (lastOpenedSpreadsheet == null || !lastOpenedSpreadsheet.exists()) {
            statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            statusLabel.setText("No spreadsheet to reimport. Use New Spreadsheet or Export & Edit first.");
            return;
        }

        try {
            var items = csvImportService.importFile(lastOpenedSpreadsheet);
            int added = data.addEntriesWithDedup(items);
            data.setLastImportDate(LocalDateTime.now());
            data.setLastSourceFile(lastOpenedSpreadsheet.getAbsolutePath());
            persistenceService.save(data);

            statusLabel.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
            statusLabel.setText("Reimported " + lastOpenedSpreadsheet.getName() + ": " + added + " new entries added");

            refreshFilters();
            applyFilters();
            onDataChanged.run();
        } catch (Exception ex) {
            statusLabel.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            statusLabel.setText("Reimport failed: " + ex.getMessage());
        }
    }

    private void writeCsvTemplate(File file) throws IOException {
        try (var writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            String today = LocalDate.now().format(DATE_FMT);
            writer.write(today + ";Client;Project;Task description;8\n");
        }
    }

    private void writeEntriesToCsv(File file) throws IOException {
        var sorted = data.getEntries().stream()
                .sorted(Comparator.comparing(WorkHourItem::getDate))
                .toList();

        try (var writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            for (var entry : sorted) {
                writer.write(entry.getDate().format(DATE_FMT)
                        + ";" + entry.getClient()
                        + ";" + entry.getProject()
                        + ";" + entry.getItem()
                        + ";" + (entry.getHours() % 1 == 0 ? String.valueOf((int) entry.getHours()) : String.valueOf(entry.getHours()))
                        + "\n");
            }
        }
    }

    private void openInSpreadsheetApp(File file) {
        try {
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(file);
            } else {
                new ProcessBuilder("open", file.getAbsolutePath()).start();
            }
        } catch (IOException e) {
            statusLabel.setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
            statusLabel.setText("File created but could not open automatically: " + file.getAbsolutePath());
        }
    }

    public Node getRoot() {
        var scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);
        scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        return scrollPane;
    }
}
