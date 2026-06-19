package com.countmyh.util;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.chart.Chart;
import javafx.scene.chart.XYChart;

public final class ChartStyler {

    private ChartStyler() {
    }

    public static void applyProjectColors(Chart chart) {
        Platform.runLater(() -> {
            if (chart instanceof XYChart<?, ?> xyChart) {
                colorSeriesBars(xyChart);
            }
        });
    }

    private static void colorSeriesBars(XYChart<?, ?> chart) {
        Platform.runLater(() -> {
            for (int si = 0; si < chart.getData().size(); si++) {
                var series = chart.getData().get(si);
                String color = ColorPalette.getColor(series.getName());
                for (var data : series.getData()) {
                    Node node = data.getNode();
                    if (node != null) {
                        node.setStyle("-fx-bar-fill: " + color + ";");
                    }
                }
                Node legendSymbol = chart.lookup(".chart-legend-item-symbol.series" + si);
                if (legendSymbol != null) {
                    legendSymbol.setStyle("-fx-background-color: " + color + ";");
                }
            }
        });
    }
}
