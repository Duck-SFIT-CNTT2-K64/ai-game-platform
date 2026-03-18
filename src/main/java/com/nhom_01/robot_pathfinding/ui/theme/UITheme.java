package com.nhom_01.robot_pathfinding.ui.theme;

import javafx.scene.paint.Color;

public final class UITheme {

    private UITheme() {
    }

    public static final Color PRIMARY = Color.web("#00FFFF");
    public static final Color SECONDARY = Color.web("#FFB800");
    public static final Color ACCENT = Color.web("#00FF9C");
    public static final Color DANGER = Color.web("#FF4444");
    public static final Color SURFACE_TEXT = Color.web("#C9DCEA");

    public static final Color BACKGROUND_TOP = Color.web("#0D1117");
    public static final Color BACKGROUND_BOTTOM = Color.web("#1B2F48");

    public static String toRgb(Color c) {
        return String.format(
            "rgb(%d,%d,%d)",
            (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255),
            (int) (c.getBlue() * 255)
        );
    }

    public static String toRgba(Color c, double alpha) {
        return String.format(
            "rgba(%d,%d,%d,%.2f)",
            (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255),
            (int) (c.getBlue() * 255),
            alpha
        );
    }

    public static String toHex(Color c) {
        return String.format(
            "#%02X%02X%02X",
            (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255),
            (int) (c.getBlue() * 255)
        );
    }

}