package com.nhom_01.robot_pathfinding.ui.animation;

import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.util.concurrent.ThreadLocalRandom;

public final class ParticleSystem {

	private ParticleSystem() {
	}

	public static Pane createParticles(double width, double height, int count, Color color) {
		Pane layer = new Pane();
		layer.setPrefSize(width, height);
		layer.setMouseTransparent(true);

		ThreadLocalRandom random = ThreadLocalRandom.current();
		for (int i = 0; i < count; i++) {
			Circle dot = new Circle(random.nextDouble(1.2, 2.6));
			dot.setFill(Color.color(color.getRed(), color.getGreen(), color.getBlue(), 0.55));
			dot.setLayoutX(random.nextDouble(20, Math.max(21, width - 20)));
			dot.setLayoutY(random.nextDouble(20, Math.max(21, height - 20)));

			FadeTransition fade = new FadeTransition(Duration.seconds(random.nextDouble(1.8, 3.5)), dot);
			fade.setFromValue(0.18);
			fade.setToValue(0.85);
			fade.setAutoReverse(true);
			fade.setCycleCount(Animation.INDEFINITE);

			TranslateTransition drift = new TranslateTransition(Duration.seconds(random.nextDouble(4.5, 8.0)), dot);
			drift.setFromX(0);
			drift.setToX(random.nextDouble(-14, 14));
			drift.setFromY(0);
			drift.setToY(random.nextDouble(-10, 10));
			drift.setAutoReverse(true);
			drift.setCycleCount(Animation.INDEFINITE);

			new ParallelTransition(fade, drift).play();
			layer.getChildren().add(dot);
		}

		return layer;
	}
}
