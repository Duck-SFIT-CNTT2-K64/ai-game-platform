package com.nhom_01.robot_pathfinding.ui.components;

import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import com.nhom_01.robot_pathfinding.ui.theme.UIEffects;
import com.nhom_01.robot_pathfinding.ui.theme.UITheme;
import javafx.animation.ParallelTransition;
import javafx.scene.Cursor;
import javafx.scene.control.Button;
import javafx.scene.control.OverrunStyle;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;

public class NeonButton extends Button {

	private final Color accent;
	private final String baseStyle;
	private final String hoverStyle;
	private final double hoverTranslateX;

	public NeonButton(String text, Color accent) {
		this(text, accent, 18, 10, 15, 12);
	}

	public NeonButton(String text, Color accent, int fontSize, int padV, int padH, double hoverTranslateX) {
		super(text);
		this.accent = accent;
		this.hoverTranslateX = hoverTranslateX;

		String rgb = UITheme.toRgb(accent);
		String textColor = UITheme.toRgb(Color.color(
			accent.getRed() * 0.72,
			accent.getGreen() * 0.72,
			accent.getBlue() * 0.72
		));
		String menuFont = AppFonts.getJerseyFamily().replace("'", "''");
		this.baseStyle =
			"-fx-background-color: rgba(255,255,255,0.92);" +
			"-fx-text-fill: " + textColor + ";" +
			"-fx-font-size: " + fontSize + "px;" +
			"-fx-font-weight: bold;" +
			"-fx-font-family: '" + menuFont + "';" +
			"-fx-padding: " + padV + " " + padH + " " + padV + " " + padH + ";" +
			"-fx-border-color: " + rgb + ";" +
			"-fx-border-width: 1.6;" +
			"-fx-border-radius: 8;" +
			"-fx-background-radius: 8;" +
			"-fx-cursor: hand;";

		this.hoverStyle =
			"-fx-background-color: " + UITheme.toRgba(accent, 0.16) + ";" +
			"-fx-text-fill: " + textColor + ";" +
			"-fx-font-size: " + fontSize + "px;" +
			"-fx-font-weight: bold;" +
			"-fx-font-family: '" + menuFont + "';" +
			"-fx-padding: " + padV + " " + padH + " " + padV + " " + padH + ";" +
			"-fx-border-color: " + rgb + ";" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 8;" +
			"-fx-background-radius: 8;" +
			"-fx-cursor: hand;";

		setCursor(Cursor.HAND);
		setTextOverrun(OverrunStyle.ELLIPSIS);
		setWrapText(false);
		setStyle(baseStyle);
		setEffect(UIEffects.createGlow(accent, 10, 0.16));

		setOnMouseEntered(e -> {
			setStyle(hoverStyle);
			ParallelTransition transition = UIEffects.createHoverTransition(this, 1.06, 1.06, hoverTranslateX);
			transition.playFromStart();
			setEffect(UIEffects.createGlow(accent, 16, 0.25));
		});

		setOnMouseExited(e -> {
			setStyle(baseStyle);
			ParallelTransition transition = UIEffects.createHoverTransition(this, 1.0, 1.0, 0);
			transition.playFromStart();
			setEffect(UIEffects.createGlow(accent, 10, 0.16));
		});
	}

	public Color getAccent() {
		return accent;
	}
}
