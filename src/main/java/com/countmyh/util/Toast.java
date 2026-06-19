package com.countmyh.util;

import javafx.animation.FadeTransition;
import javafx.animation.PauseTransition;
import javafx.animation.SequentialTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public final class Toast {

    public enum Type { SUCCESS, ERROR, WARNING }

    private static final double FADE_IN_MS = 250;
    private static final double DISPLAY_MS = 3500;
    private static final double FADE_OUT_MS = 500;

    private Toast() {}

    public static void show(StackPane container, String message, Type type) {
        var label = new Label(message);
        label.setWrapText(true);
        label.setMaxWidth(600);
        label.setStyle(labelStyle(type));

        var toast = new HBox(label);
        toast.setAlignment(Pos.CENTER);
        toast.setPadding(new Insets(12, 20, 12, 20));
        toast.setStyle(boxStyle(type));
        toast.setMouseTransparent(false);
        toast.setPickOnBounds(false);
        toast.setOpacity(0);

        StackPane.setAlignment(toast, Pos.BOTTOM_CENTER);
        StackPane.setMargin(toast, new Insets(0, 0, 24, 0));
        container.getChildren().add(toast);

        var fadeIn = new FadeTransition(Duration.millis(FADE_IN_MS), toast);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);

        var stay = new PauseTransition(Duration.millis(DISPLAY_MS));

        var fadeOut = new FadeTransition(Duration.millis(FADE_OUT_MS), toast);
        fadeOut.setFromValue(1);
        fadeOut.setToValue(0);
        fadeOut.setOnFinished(e -> container.getChildren().remove(toast));

        new SequentialTransition(fadeIn, stay, fadeOut).play();
    }

    private static String boxStyle(Type type) {
        String borderColor = switch (type) {
            case SUCCESS -> "#22c55e";
            case ERROR -> "#ef4444";
            case WARNING -> "#f59e0b";
        };
        return "-fx-background-color: #1a1d27;"
                + "-fx-border-color: " + borderColor + ";"
                + "-fx-border-width: 1;"
                + "-fx-border-radius: 10;"
                + "-fx-background-radius: 10;"
                + "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 12, 0, 0, 4);";
    }

    private static String labelStyle(Type type) {
        String color = switch (type) {
            case SUCCESS -> "#22c55e";
            case ERROR -> "#ef4444";
            case WARNING -> "#f59e0b";
        };
        return "-fx-text-fill: " + color + ";"
                + "-fx-font-size: 13px;"
                + "-fx-font-weight: bold;";
    }
}
