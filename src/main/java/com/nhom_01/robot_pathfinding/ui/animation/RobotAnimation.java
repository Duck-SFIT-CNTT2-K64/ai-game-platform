package com.nhom_01.robot_pathfinding.ui.animation;

import javafx.animation.Animation;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.scene.Node;
import javafx.util.Duration;

public final class RobotAnimation {

	private RobotAnimation() {
	}

	public static void animate(Node robotNode, double distanceX, double runMillis,
							   double bounceY, double bounceMillis) {
		TranslateTransition run = new TranslateTransition(Duration.millis(runMillis), robotNode);
		run.setFromX(0);
		run.setToX(distanceX);
		run.setAutoReverse(true);
		run.setCycleCount(Animation.INDEFINITE);

		TranslateTransition bounce = new TranslateTransition(Duration.millis(bounceMillis), robotNode);
		bounce.setFromY(0);
		bounce.setToY(bounceY);
		bounce.setAutoReverse(true);
		bounce.setCycleCount(Animation.INDEFINITE);

		new ParallelTransition(run, bounce).play();
	}
}
