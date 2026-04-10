package com.nhom_01.robot_pathfinding.ui.components;

import com.nhom_01.robot_pathfinding.ui.theme.UIEffects;
import com.nhom_01.robot_pathfinding.ui.theme.UITheme;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import javafx.animation.ParallelTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class GameCard extends VBox {

	private final VBox body;

	public GameCard(String title, String icon, Color accent, double width, double height) {
		setAlignment(Pos.TOP_LEFT);
		setPadding(new Insets(18));
		setPrefSize(width, height);
		setMinSize(width, height);

		String baseStyle =
			"-fx-background-color: rgba(255,255,255,0.94);" +
			"-fx-border-color: rgba(0,0,0,0.10);" +
			"-fx-border-width: 1.2;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;";

		String hoverStyle =
			"-fx-background-color: rgba(255,255,255,0.99);" +
			"-fx-border-color: " + UITheme.toRgba(accent, 0.70) + ";" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;";

		setStyle(baseStyle);

		DropShadow normalGlow = UIEffects.createGlow(
			Color.color(0.12, 0.16, 0.20, 0.18),
			10,
			0.04
		);
		setEffect(normalGlow);

		Text heading = new Text(icon + "  " + title);
		heading.setFont(AppFonts.jersey(30));
		heading.setFill(accent);

		this.body = new VBox(10);
		this.body.setAlignment(Pos.TOP_LEFT);

		getChildren().addAll(heading, body);

		setOnMouseEntered(e -> {
			setStyle(hoverStyle);
			ParallelTransition transition = UIEffects.createHoverTransition(this, 1.01, 1.01, 0);
			transition.playFromStart();
			setTranslateY(-4);
			setEffect(UIEffects.createGlow(
				Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.22),
				16,
				0.08
			));
		});

		setOnMouseExited(e -> {
			setStyle(baseStyle);
			ParallelTransition transition = UIEffects.createHoverTransition(this, 1.0, 1.0, 0);
			transition.playFromStart();
			setTranslateY(0);
			setEffect(normalGlow);
		});
	}

	public VBox body() {
		return body;
	}

	public Text createBodyText(String text, double wrapWidth) {
		Text line = new Text(text);
		line.setFont(AppFonts.jersey(15));
		line.setFill(UITheme.SURFACE_TEXT);
		line.setWrappingWidth(wrapWidth);
		return line;
	}

	public void addBodyNodes(Node... nodes) {
		body.getChildren().addAll(nodes);
	}
}
