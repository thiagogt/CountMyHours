package com.countmyh.view;

import com.countmyh.model.ImportRecord;
import com.countmyh.model.WorkHourItem;
import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.util.AppDirs;
import com.countmyh.util.I18n;
import com.countmyh.util.Toast;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.awt.Desktop;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
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
    private final StackPane rootStack;

    private static final String CSV_HEADER = "Data;Cliente;Projeto;Item;Hs";
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private TableView<WorkHourItem> table;
    private TableView<ImportRecord> importHistoryTable;
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
        this.rootStack = new StackPane();
        build();
    }

    private void build() {
        var title = new Label(I18n.get("data.title"));
        title.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        content.getChildren().addAll(title, buildImportSection(), buildImportHistorySection(), buildFilters(), buildTable());
    }

    private void toast(String message, Toast.Type type) {
        Toast.show(rootStack, message, type);
    }

    private Node buildImportSection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("data.import.title"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var btnImport = new Button(I18n.get("data.import.csv"));
        btnImport.setOnAction(e -> handleImport());

        var btnNewSheet = new Button(I18n.get("data.new.spreadsheet"));
        btnNewSheet.setStyle("-fx-background-color: #10b981;");
        btnNewSheet.setOnAction(e -> handleNewSpreadsheet());

        var btnExportEdit = new Button(I18n.get("data.export.edit"));
        btnExportEdit.setStyle("-fx-background-color: #f59e0b;");
        btnExportEdit.setOnAction(e -> handleExportAndEdit());

        var btnReimport = new Button(I18n.get("data.reimport.last"));
        btnReimport.setStyle("-fx-background-color: #8b5cf6;");
        btnReimport.setOnAction(e -> handleReimportLast());

        var desc = new Label(I18n.get("data.import.desc"));
        desc.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px; -fx-wrap-text: true;");
        desc.setWrapText(true);

        var info = new VBox(4);
        if (data.getLastImportDate() != null) {
            info.getChildren().add(new Label(I18n.get("data.last.import",
                    data.getLastImportDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))));
        }
        if (data.getLastSourceFile() != null) {
            var fileLabel = new Label(I18n.get("data.source", data.getLastSourceFile()));
            fileLabel.setStyle("-fx-text-fill: #8b8d97; -fx-font-size: 11px;");
            info.getChildren().add(fileLabel);
        }

        var totalLabel = new Label(I18n.get("data.total.entries", data.getEntries().size()));
        totalLabel.setStyle("-fx-text-fill: #8b8d97;");
        info.getChildren().add(totalLabel);

        var btnEraseAll = new Button(I18n.get("data.erase.all"));
        btnEraseAll.setStyle("-fx-background-color: #ef4444;");
        btnEraseAll.setOnAction(e -> handleEraseAllData());

        var buttons = new HBox(12, btnNewSheet, btnExportEdit, btnImport, btnReimport);
        buttons.setAlignment(Pos.CENTER_LEFT);

        var dangerZone = new HBox(12, btnEraseAll);
        dangerZone.setAlignment(Pos.CENTER_LEFT);

        container.getChildren().addAll(sectionTitle, buttons, desc, info, dangerZone);
        return container;
    }

    private Node buildImportHistorySection() {
        var container = new VBox(12);
        container.getStyleClass().add("chart-container");

        var sectionTitle = new Label(I18n.get("data.imported.files"));
        sectionTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        importHistoryTable = new TableView<>();
        importHistoryTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        importHistoryTable.setPlaceholder(new Label(I18n.get("data.no.files.imported")));

        var colFile = new TableColumn<ImportRecord, String>(I18n.get("data.col.file"));
        colFile.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().fileName()));

        var colDate = new TableColumn<ImportRecord, String>(I18n.get("data.col.imported.at"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().importDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
        ));
        colDate.setPrefWidth(140);

        var colEntries = new TableColumn<ImportRecord, Number>(I18n.get("data.col.entries"));
        colEntries.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().entriesImported()));
        colEntries.setPrefWidth(70);

        var colActions = new TableColumn<ImportRecord, Void>(I18n.get("data.col.actions"));
        colActions.setPrefWidth(180);
        colActions.setCellFactory(e -> new TableCell<>() {
            private final Button btnExport = new Button(I18n.get("data.export"));
            private final Button btnDelete = new Button(I18n.get("data.delete"));
            private final HBox box = new HBox(8, btnExport, btnDelete);

            {
                btnExport.setStyle("-fx-background-color: #f59e0b; -fx-font-size: 11px; -fx-padding: 4 10;");
                btnDelete.setStyle("-fx-background-color: #ef4444; -fx-font-size: 11px; -fx-padding: 4 10;");
                box.setAlignment(Pos.CENTER);

                btnExport.setOnAction(e -> {
                    ImportRecord record = getTableView().getItems().get(getIndex());
                    handleExportImportRecord(record);
                });

                btnDelete.setOnAction(e -> {
                    ImportRecord record = getTableView().getItems().get(getIndex());
                    handleDeleteImportRecord(record);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        importHistoryTable.getColumns().addAll(colFile, colDate, colEntries, colActions);
        refreshImportHistory();

        int rows = Math.max(2, Math.min(6, data.getImportHistory().size()));
        importHistoryTable.setPrefHeight(40 + rows * 36);

        container.getChildren().addAll(sectionTitle, importHistoryTable);
        return container;
    }

    private void refreshImportHistory() {
        importHistoryTable.getItems().setAll(data.getImportHistory());
        int rows = Math.max(2, Math.min(6, data.getImportHistory().size()));
        importHistoryTable.setPrefHeight(40 + rows * 36);
    }

    private void handleExportImportRecord(ImportRecord record) {
        var entries = data.getEntries().stream()
                .filter(e -> record.filePath().equals(e.sourceFile()))
                .sorted(Comparator.comparing(WorkHourItem::date))
                .toList();

        if (entries.isEmpty()) {
            toast(I18n.get("toast.no.entries.for", record.fileName()), Toast.Type.WARNING);
            return;
        }

        var fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("data.export.from", record.fileName()));
        fileChooser.setInitialFileName(record.fileName());
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(I18n.get("data.csv.files"), "*.csv"));

        File file = fileChooser.showSaveDialog(content.getScene().getWindow());
        if (file == null) return;

        try {
            writeItemsToCsv(file, entries);
            toast(I18n.get("toast.exported", entries.size(), file.getName()), Toast.Type.SUCCESS);
        } catch (IOException ex) {
            toast(I18n.get("toast.export.failed", ex.getMessage()), Toast.Type.ERROR);
        }
    }

    private void handleDeleteImportRecord(ImportRecord record) {
        long count = data.getEntries().stream()
                .filter(e -> record.filePath().equals(e.sourceFile()))
                .count();

        var alert = new Alert(Alert.AlertType.CONFIRMATION);
        styleAlert(alert);
        alert.setTitle(I18n.get("dialog.delete.title"));
        alert.setHeaderText(I18n.get("dialog.delete.header", record.fileName()));
        alert.setContentText(I18n.get("dialog.delete.content", count));

        var result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        int removed = data.removeEntriesBySource(record.filePath());
        data.removeImportRecord(record);

        try {
            persistenceService.save(data);
        } catch (IOException ex) {
            toast(I18n.get("toast.save.failed", ex.getMessage()), Toast.Type.ERROR);
            return;
        }

        toast(I18n.get("toast.deleted", removed, record.fileName()), Toast.Type.SUCCESS);

        refreshImportHistory();
        refreshFilters();
        applyFilters();
        onDataChanged.run();
    }

    private void writeItemsToCsv(File file, List<WorkHourItem> items) throws IOException {
        try (var writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            for (var entry : items) {
                writer.write(entry.date().format(DATE_FMT)
                        + ";" + entry.client()
                        + ";" + entry.project()
                        + ";" + entry.item()
                        + ";" + (entry.hours() % 1 == 0 ? String.valueOf((int) entry.hours()) : String.valueOf(entry.hours()))
                        + "\n");
            }
        }
    }

    private Node buildFilters() {
        String allYears = I18n.get("data.all.years");
        String allProjects = I18n.get("data.all.projects");

        filterYear = new ComboBox<>();
        filterYear.getItems().add(allYears);
        var years = data.getEntries().stream()
                .map(e -> String.valueOf(e.year()))
                .distinct()
                .sorted()
                .toList();
        filterYear.getItems().addAll(years);
        String currentYear = String.valueOf(java.time.LocalDate.now().getYear());
        if (years.contains(currentYear)) {
            filterYear.setValue(currentYear);
        } else if (!years.isEmpty()) {
            filterYear.setValue(years.getLast());
        } else {
            filterYear.setValue(allYears);
        }
        filterYear.setOnAction(e -> applyFilters());

        filterProject = new ComboBox<>();
        filterProject.getItems().add(allProjects);
        data.getEntries().stream()
                .map(WorkHourItem::project)
                .distinct()
                .sorted()
                .forEach(p -> filterProject.getItems().add(p));
        filterProject.setValue(allProjects);
        filterProject.setOnAction(e -> applyFilters());

        var box = new HBox(12,
                new Label(I18n.get("data.filter.year")), filterYear,
                new Label(I18n.get("data.filter.project")), filterProject
        );
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Node buildTable() {
        table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setPrefHeight(500);

        var colDate = new TableColumn<WorkHourItem, String>(I18n.get("data.col.date"));
        colDate.setCellValueFactory(cd -> new SimpleStringProperty(
                cd.getValue().date().format(DATE_FMT)
        ));
        colDate.setMinWidth(100);

        var colClient = new TableColumn<WorkHourItem, String>(I18n.get("data.col.client"));
        colClient.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().client()));
        colClient.setMinWidth(100);

        var colProject = new TableColumn<WorkHourItem, String>(I18n.get("data.col.project"));
        colProject.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().project()));
        colProject.setMinWidth(140);

        var colItem = new TableColumn<WorkHourItem, String>(I18n.get("data.col.item"));
        colItem.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().item()));
        colItem.setMinWidth(100);
        var colHours = new TableColumn<WorkHourItem, Number>(I18n.get("data.col.hours"));
        colHours.setCellValueFactory(cd -> new SimpleDoubleProperty(cd.getValue().hours()));
        colHours.setMinWidth(70);

        table.getColumns().addAll(colDate, colClient, colProject, colItem, colHours);
        applyFilters();

        return table;
    }

    private void applyFilters() {
        if (table == null) return;

        String allYears = I18n.get("data.all.years");
        String allProjects = I18n.get("data.all.projects");
        String yearFilter = filterYear.getValue();
        String projectFilter = filterProject.getValue();

        List<WorkHourItem> filtered = data.getEntries().stream()
                .filter(e -> allYears.equals(yearFilter) || String.valueOf(e.year()).equals(yearFilter))
                .filter(e -> allProjects.equals(projectFilter) || e.project().equals(projectFilter))
                .sorted(Comparator.comparing(WorkHourItem::date).reversed())
                .collect(Collectors.toList());

        table.getItems().setAll(filtered);
    }

    private void showImportFormatWarning() {
        if (Files.exists(AppDirs.DATA_DIR.resolve("import_warning_dismissed"))) return;

        var msgLabel = new Label(I18n.get("import.warning.message"));
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(380);
        msgLabel.setStyle("-fx-text-fill: #e4e4e7; -fx-font-size: 13px;");

        var neverAgain = new CheckBox(I18n.get("import.warning.never.again"));
        neverAgain.setStyle("-fx-text-fill: #8b8d97;");

        var box = new VBox(16, msgLabel, neverAgain);
        box.setPadding(new Insets(4, 0, 4, 0));

        var dialog = new Dialog<ButtonType>();
        dialog.setTitle(I18n.get("import.warning.title"));
        dialog.getDialogPane().setContent(box);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.OK);
        dialog.getDialogPane().setPrefWidth(440);
        dialog.getDialogPane().setStyle("-fx-background-color: #1a1d27;");
        var css = getClass().getResource("/com/countmyh/dark-theme.css");
        if (css != null) dialog.getDialogPane().getStylesheets().add(css.toExternalForm());

        dialog.showAndWait();

        if (neverAgain.isSelected()) {
            try {
                Files.createDirectories(AppDirs.DATA_DIR);
                Files.createFile(AppDirs.DATA_DIR.resolve("import_warning_dismissed"));
            } catch (IOException ignored) {}
        }
    }

    private void handleImport() {
        showImportFormatWarning();
        var fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("data.import.work.hours"));
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("data.csv.files"), "*.csv")
        );

        File file = fileChooser.showOpenDialog(content.getScene().getWindow());
        if (file == null) return;

        importFromFile(file);
    }

    private void refreshFilters() {
        String allYears = I18n.get("data.all.years");
        String allProjects = I18n.get("data.all.projects");
        String currentYear = filterYear.getValue();
        String currentProject = filterProject.getValue();

        filterYear.getItems().clear();
        filterYear.getItems().add(allYears);
        data.getEntries().stream()
                .map(e -> String.valueOf(e.year()))
                .distinct().sorted()
                .forEach(y -> filterYear.getItems().add(y));
        filterYear.setValue(filterYear.getItems().contains(currentYear) ? currentYear : allYears);

        filterProject.getItems().clear();
        filterProject.getItems().add(allProjects);
        data.getEntries().stream()
                .map(WorkHourItem::project)
                .distinct().sorted()
                .forEach(p -> filterProject.getItems().add(p));
        filterProject.setValue(filterProject.getItems().contains(currentProject) ? currentProject : allProjects);
    }

    private void handleNewSpreadsheet() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("data.create.spreadsheet"));
        fileChooser.setInitialFileName("hours_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM")) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("data.csv.files"), "*.csv")
        );

        File file = fileChooser.showSaveDialog(content.getScene().getWindow());
        if (file == null) return;

        try {
            writeCsvTemplate(file);
            lastOpenedSpreadsheet = file;
            openInSpreadsheetApp(file);
            toast(I18n.get("toast.spreadsheet.created", file.getName()), Toast.Type.SUCCESS);
        } catch (IOException ex) {
            toast(I18n.get("toast.spreadsheet.failed", ex.getMessage()), Toast.Type.ERROR);
        }
    }

    private void handleExportAndEdit() {
        if (data.getEntries().isEmpty()) {
            toast(I18n.get("toast.no.entries.export"), Toast.Type.WARNING);
            return;
        }

        var fileChooser = new FileChooser();
        fileChooser.setTitle(I18n.get("data.export.csv"));
        fileChooser.setInitialFileName("hours_export_" + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy_MM_dd")) + ".csv");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(I18n.get("data.csv.files"), "*.csv")
        );

        File file = fileChooser.showSaveDialog(content.getScene().getWindow());
        if (file == null) return;

        try {
            var sorted = data.getEntries().stream()
                    .sorted(Comparator.comparing(WorkHourItem::date))
                    .toList();
            writeItemsToCsv(file, sorted);
            lastOpenedSpreadsheet = file;
            openInSpreadsheetApp(file);
            toast(I18n.get("toast.exported", data.getEntries().size(), file.getName()), Toast.Type.SUCCESS);
        } catch (IOException ex) {
            toast(I18n.get("toast.export.failed", ex.getMessage()), Toast.Type.ERROR);
        }
    }

    private void handleReimportLast() {
        if (lastOpenedSpreadsheet == null || !lastOpenedSpreadsheet.exists()) {
            toast(I18n.get("toast.no.spreadsheet"), Toast.Type.WARNING);
            return;
        }

        importFromFile(lastOpenedSpreadsheet);
    }

    private void importFromFile(File file) {
        try {
            var rawItems = csvImportService.importFile(file);
            String sourcePath = file.getAbsolutePath();
            var items = rawItems.stream().map(i -> i.withSourceFile(sourcePath)).toList();

            int added = data.addEntriesWithDedup(items);
            data.setLastImportDate(LocalDateTime.now());
            data.setLastSourceFile(sourcePath);

            if (added > 0) {
                data.addImportRecord(new ImportRecord(
                        file.getName(), sourcePath, LocalDateTime.now(), added
                ));
            }

            persistenceService.save(data);
            toast(I18n.get("toast.imported", added, file.getName(), items.size()), Toast.Type.SUCCESS);

            refreshImportHistory();
            refreshFilters();
            applyFilters();
            onDataChanged.run();
        } catch (Exception ex) {
            toast(I18n.get("toast.import.failed", ex.getMessage()), Toast.Type.ERROR);
        }
    }

    private void handleEraseAllData() {
        if (data.getEntries().isEmpty()) {
            toast(I18n.get("toast.no.data.erase"), Toast.Type.WARNING);
            return;
        }

        var alert = new Alert(Alert.AlertType.WARNING);
        styleAlert(alert);
        alert.setTitle(I18n.get("dialog.erase.title"));
        alert.setHeaderText(I18n.get("dialog.erase.header"));
        alert.setContentText(I18n.get("dialog.erase.content"));
        alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);

        var result = alert.showAndWait();
        if (result.isEmpty() || result.get() != ButtonType.OK) return;

        int totalEntries = data.getEntries().size();
        data.clearAll();

        try {
            persistenceService.save(data);
        } catch (IOException ex) {
            toast(I18n.get("toast.save.failed", ex.getMessage()), Toast.Type.ERROR);
            return;
        }

        toast(I18n.get("toast.erased", totalEntries), Toast.Type.SUCCESS);

        refreshImportHistory();
        refreshFilters();
        applyFilters();
        onDataChanged.run();
    }

    private void writeCsvTemplate(File file) throws IOException {
        try (var writer = new FileWriter(file)) {
            writer.write(CSV_HEADER + "\n");
            String today = LocalDate.now().format(DATE_FMT);
            writer.write(today + ";Client;Project;Task description;8\n");
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
            toast(I18n.get("toast.file.open.failed", file.getAbsolutePath()), Toast.Type.WARNING);
        }
    }

    private void styleAlert(Alert alert) {
        var dialogPane = alert.getDialogPane();
        var css = getClass().getResource("/com/countmyh/dark-theme.css");
        if (css != null) {
            dialogPane.getStylesheets().add(css.toExternalForm());
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
