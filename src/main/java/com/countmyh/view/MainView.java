package com.countmyh.view;

import com.countmyh.model.WorkPeriodTracker;
import com.countmyh.service.BusinessDayService;
import com.countmyh.service.CalculationService;
import com.countmyh.service.CsvImportService;
import com.countmyh.service.JsonPersistenceService;
import com.countmyh.util.I18n;
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
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class MainView {

    private final BorderPane root;
    private final StackPane contentArea;
    private final WorkPeriodTracker data;
    private final CalculationService calcService;
    private final BusinessDayService businessDayService;
    private final JsonPersistenceService persistenceService;
    private final CsvImportService csvImportService;

    private DashboardView dashboardView;
    private TimelineView timelineView;
    private ExtraHoursView extraHoursView;
    private HourSellingView hourSellingView;
    private DataEntryView dataEntryView;
    private SettingsView settingsView;
    private ToggleButton btnDashboard;
    private String currentView = "dashboard";
    private boolean viewsStale = false;

    public MainView(WorkPeriodTracker data, CalculationService calcService,
                    BusinessDayService businessDayService,
                    JsonPersistenceService persistenceService, CsvImportService csvImportService) {
        this.data = data;
        this.calcService = calcService;
        this.businessDayService = businessDayService;
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

        var title = new Label(I18n.get("app.title"));
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #e4e4e7;");

        var logoBox = new HBox(10, logoView, title);
        logoBox.setAlignment(Pos.CENTER_LEFT);
        logoBox.setPadding(new Insets(16, 16, 6, 16));

        var subtitle = new Label(I18n.get("app.subtitle"));
        subtitle.setStyle("-fx-font-size: 11px; -fx-text-fill: #8b8d97; -fx-padding: 0 20 20 20;");

        var group = new ToggleGroup();

        btnDashboard = createNavButton(I18n.get("nav.dashboard"), group);
        var btnTimeline = createNavButton(I18n.get("nav.timeline"), group);
        var btnExtra = createNavButton(I18n.get("nav.extra"), group);
        var btnSelling = createNavButton(I18n.get("nav.selling"), group);
        var btnData = createNavButton(I18n.get("nav.data"), group);

        var btnSettings = createNavButton(I18n.get("nav.settings"), group);

        btnDashboard.setOnAction(e -> showView("dashboard"));
        btnTimeline.setOnAction(e -> showView("timeline"));
        btnExtra.setOnAction(e -> showView("extra"));
        btnSelling.setOnAction(e -> showView("selling"));
        btnData.setOnAction(e -> showView("data"));
        btnSettings.setOnAction(e -> showView("settings"));

        var spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        var sidebar = new VBox(0, logoBox, subtitle, btnDashboard, btnTimeline, btnExtra, btnSelling, btnData, spacer, btnSettings);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPrefWidth(200);
        sidebar.setMinWidth(200);
        sidebar.setAlignment(Pos.TOP_LEFT);

        root.setLeft(sidebar);

        btnDashboard.setSelected(true);
        viewsStale = true;
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
        if (viewName.equals(currentView) && !viewsStale) return;
        currentView = viewName;
        viewsStale = false;
        contentArea.getChildren().clear();
        Node view = switch (viewName) {
            case "dashboard" -> getDashboardView();
            case "timeline" -> getTimelineView();
            case "extra" -> getExtraHoursView();
            case "selling" -> getHourSellingView();
            case "data" -> getDataEntryView();
            case "settings" -> getSettingsView();
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

    private Node getExtraHoursView() {
        extraHoursView = new ExtraHoursView(data, calcService, businessDayService, persistenceService);
        return extraHoursView.getRoot();
    }

    private Node getHourSellingView() {
        hourSellingView = new HourSellingView(data, calcService, persistenceService);
        return hourSellingView.getRoot();
    }

    private Node getDataEntryView() {
        dataEntryView = new DataEntryView(data, csvImportService, persistenceService, this::refreshViews);
        return dataEntryView.getRoot();
    }

    private Node getSettingsView() {
        settingsView = new SettingsView(data, persistenceService, this::rebuildUI);
        return settingsView.getRoot();
    }

    private void rebuildUI() {
        root.setLeft(null);
        buildSidebar();
        root.setCenter(contentArea);
    }

    public void refreshViews() {
        viewsStale = true;
    }

    public BorderPane getRoot() {
        return root;
    }
}
