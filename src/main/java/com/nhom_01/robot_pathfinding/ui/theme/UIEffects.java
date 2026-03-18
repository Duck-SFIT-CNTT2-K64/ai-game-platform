package com.nhom_01.robot_pathfinding.ui.theme;

import javafx.animation.Interpolator;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

public final class UIEffects {

	private UIEffects() {
	}

	public static DropShadow createGlow(Color color, double radius, double spread) {
		DropShadow glow = new DropShadow();
		glow.setColor(color);
		glow.setRadius(radius);
		glow.setSpread(spread);
		return glow;
	}

	public static ParallelTransition createHoverTransition(Node node, double toX, double toY, double toTranslateX) {
		ScaleTransition scale = new ScaleTransition(Duration.millis(160), node);
		scale.setInterpolator(Interpolator.EASE_BOTH);
		scale.setToX(toX);
		scale.setToY(toY);

		TranslateTransition move = new TranslateTransition(Duration.millis(160), node);
		move.setInterpolator(Interpolator.EASE_BOTH);
		move.setToX(toTranslateX);
		move.setToY(0);

		return new ParallelTransition(scale, move);
	}
}
