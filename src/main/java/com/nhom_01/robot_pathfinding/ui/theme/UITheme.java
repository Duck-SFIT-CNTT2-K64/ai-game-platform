package com.nhom_01.robot_pathfinding.ui.theme;

import javafx.scene.paint.Color;

public final class UITheme {

    private UITheme() {
    }

    public static final Color PRIMARY = Color.web("#2F80ED");
    public static final Color SECONDARY = Color.web("#EF6C00");
    public static final Color ACCENT = Color.web("#00897B");
    public static final Color DANGER = Color.web("#D84343");
    public static final Color SURFACE_TEXT = Color.web("#4F5B62");

    public static final Color BACKGROUND_TOP = Color.web("#F8D18E");
    public static final Color BACKGROUND_BOTTOM = Color.web("#ECC682");

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