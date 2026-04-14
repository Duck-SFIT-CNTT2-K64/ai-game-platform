package com.nhom_01.robot_pathfinding.ui.components;

import com.nhom_01.robot_pathfinding.ui.theme.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.HBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;

public final class HUDBar {

	private HUDBar() {
	}

	public static HBox createHUD(String left, String center, String right) {
		HBox hud = new HBox(60);
		hud.setAlignment(Pos.CENTER);
		hud.setPadding(new Insets(8, 16, 8, 16));
		hud.setStyle(
			"-fx-background-color: rgba(255,255,255,0.94);" +
			"-fx-border-color: rgba(0,0,0,0.10);" +
			"-fx-border-width: 1.2;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		Text leftText = createHudText(left, UITheme.SECONDARY);
		Text centerText = createHudText(center, UITheme.PRIMARY);
		Text rightText = createHudText(right, UITheme.ACCENT);

		hud.getChildren().addAll(leftText, centerText, rightText);
		return hud;
	}

	private static Text createHudText(String value, javafx.scene.paint.Color color) {
		Text text = new Text(value);
		text.setFont(AppFonts.vt323(30));
		text.setFill(color);
		return text;
	}
}
