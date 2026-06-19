package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainView {

    private final BorderPane root;
    private final StackPane contentArea;
    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final JsonPersistenceService persistenceService;
    private final CsvImportService csvImportService;

    private DashboardView dashboardView;
    private TimelineView timelineView;
    private DataEntryView dataEntryView;
    private ToggleButton btnDashboard;

    public MainView(WorkPeriodTracker data, CalculationService calcService,
                    JsonPersistenceService persistenceService, CsvImportService csvImportService) {
        this.data = data;
        this.calcService = calcService;
        this.persistenceService = persistenceService;
        this.csvImportService = csvImportService;

        this.contentArea = new StackPane();
        this.root = new BorderPane();

        buildSidebar();
        root.setCenter(contentArea);
    }

    private void buildSidebar() {
        var logoImage = new Image(getClass().getResourceAsStream("/com/countmyh/logo.png"));
        var logoView = new ImageView(logoImage);
        logoView.setFitWidth(40);
        logoView.setFitHeight(40);
        logoView.setPreserveRatio(true);

        var title = new Label("CountMyHours");
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var logoBox = new HBox(10, logoView, title);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(16, 16, 6, 16));

        var subtitle = new Label("Work Hours Tracker");
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b8d97; -fx-padding: 0 20 20 20;");

        var group = new ToggleGroup();

        btnDashboard = createNavButton("Dashboard", group);
        var btnTimeline = createNavButton("Timeline", group);
        var btnData = createNavButton("Data", group);

        btnDashboard.setOnAction(e -> showView("dashboard"));
        btnTimeline.setOnAction(e -> showView("timeline"));
        btnData.setOnAction(e -> showView("data"));

        var sidebar = new VBox(0, logoBox, subtitle, btnDashboard, btnTimeline, btnData);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setAlignment(Pos.TOP_LEFT);

        root.setLeft(sidebar);

        btnDashboard.setSelected(true);
        showView("dashboard");
    }

    private ToggleButton createNavButton(String text, ToggleGroup group) {
        var btn = new ToggleButton(text);
        btn.getStyleClass().add("nav-button");
        btn.setToggleGroup(group);
        btn.setMaxWidth(Double.MAX_VALUE);
        return btn;
    }

    private void showView(String viewName) {
        contentArea.getChildren().clear();
        Node view = switch (viewName) {
            case "dashboard" -> getDashboardView();
            case "timeline" -> getTimelineView();
            case "data" -> getDataEntryView();
            default -> new Label("Unknown view");
        };
        contentArea.getChildren().add(view);
    }

    private Node getDashboardView() {
        dashboardView = new DashboardView(data, calcService);
        return dashboardView.getRoot();
    }

    private Node getTimelineView() {
        timelineView = new TimelineView(data, calcService);
        return timelineView.getRoot();
    }

    private Node getDataEntryView() {
        dataEntryView = new DataEntryView(data, csvImportService, persistenceService, this::refreshViews);
        return dataEntryView.getRoot();
    }

    public void refreshViews() {
        btnDashboard.setSelected(true);
        showView("dashboard");
    }

    public BorderPane getRoot() {
        return root;
    }
}
