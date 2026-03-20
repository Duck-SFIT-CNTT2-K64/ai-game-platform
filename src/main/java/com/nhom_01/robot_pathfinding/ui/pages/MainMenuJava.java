package com.nhom_01.robot_pathfinding.ui.pages;

import javafx.animation.TranslateTransition;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.Cursor;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import com.nhom_01.robot_pathfinding.ui.animation.ParticleSystem;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.UITheme;

import java.net.URL;

public class MainMenuJava extends Application {

	private StackPane quitConfirmOverlay;

	@Override
	public void start(Stage stage) {
		// Root with custom futuristic background
		StackPane root = new StackPane();
		root.setPrefSize(1400, 800);
		root.getChildren().add(createFuturisticBackground());

		// Main content layout: title + menu on the left, animated showcase on the right.
		HBox mainContent = new HBox(70);
		mainContent.setPadding(new javafx.geometry.Insets(55, 70, 55, 70));
		mainContent.setAlignment(Pos.CENTER_LEFT);

		VBox menuColumn = createMenuColumn(stage);
		StackPane animatedRobotPanel = createAnimatedRobotPanel();
		mainContent.getChildren().addAll(menuColumn, animatedRobotPanel);

		// Add semi-transparent overlay for readability
		Pane overlay = new Pane();
		overlay.setStyle("-fx-background-color: rgba(0,0,0,0.12);");
		overlay.setMouseTransparent(true);
		Pane particles = ParticleSystem.createParticles(1400, 800, 42, UITheme.PRIMARY);
		quitConfirmOverlay = createQuitConfirmOverlay(stage);
		root.getChildren().addAll(mainContent, particles, overlay, quitConfirmOverlay);
		StackPane.setAlignment(mainContent, Pos.CENTER_LEFT);

		Scene scene = new Scene(root, 1400, 800);
		stage.setTitle("Robot Maze");
		stage.setScene(scene);
		stage.show();
	}

	// Create futuristic background with grid and glow effects
	private Pane createFuturisticBackground() {
		Pane bgPane = new Pane();
		bgPane.setPrefSize(1400, 800);

		// Dark gradient background
		Canvas bgCanvas = new Canvas(1400, 800);
		GraphicsContext gc = bgCanvas.getGraphicsContext2D();

		// Draw gradient background (dark blue-purple)
		for (int y = 0; y < 800; y++) {
			double ratio = y / 800.0;
			int r = (int) (13 + (27 - 13) * ratio);
			int g = (int) (17 + (51 - 17) * ratio);
			int b = (int) (23 + (48 - 23) * ratio);
			gc.setStroke(Color.rgb(r, g, b));
			gc.strokeLine(0, y, 1400, y);
		}

		// Draw futuristic grid
		gc.setStroke(Color.color(0, 0.4, 0.8, 0.15));
		gc.setLineWidth(1);
		int gridSize = 50;
		for (int x = 0; x < 1400; x += gridSize) {
			gc.strokeLine(x, 0, x, 800);
		}
		for (int y = 0; y < 800; y += gridSize) {
			gc.strokeLine(0, y, 1400, y);
		}

		// Draw glowing grid intersections
		gc.setFill(Color.color(0, 1, 1, 0.3));
		for (int x = 0; x < 1400; x += gridSize) {
			for (int y = 0; y < 800; y += gridSize) {
				gc.fillOval(x - 3, y - 3, 6, 6);
			}
		}

		// Draw vertical glowing line in the middle
		gc.setStroke(Color.color(0, 1, 1, 0.25));
		gc.setLineWidth(2);
		gc.strokeLine(700, 0, 700, 800);

		bgPane.getChildren().add(bgCanvas);
		return bgPane;
	}

	// Create title section shown directly above menu buttons.
	private VBox createTitleSection() {
		VBox titleBox = new VBox(5);
		titleBox.setAlignment(Pos.CENTER_LEFT);

		Text mainTitle = new Text("ROBOT");
		mainTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 68));
		mainTitle.setFill(Color.web("#00FFFF")); // Cyan

		Text subtitle = new Text("MAZE");
		subtitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 68));
		subtitle.setFill(Color.web("#FFB800")); // Orange

		Text description = new Text("SMART PATHFINDING - SMOOTH EXPERIENCE");
		description.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
		description.setFill(Color.web("#B8C3CD"));

		titleBox.getChildren().addAll(mainTitle, subtitle, description);

		DropShadow titleGlow = new DropShadow();
		titleGlow.setColor(Color.web("#00FFFF"));
		titleGlow.setRadius(30);
		mainTitle.setEffect(titleGlow);

		DropShadow subtitleGlow = new DropShadow();
		subtitleGlow.setColor(Color.web("#FFB800"));
		subtitleGlow.setRadius(30);
		subtitle.setEffect(subtitleGlow);

		return titleBox;
	}

	private VBox createMenuColumn(Stage stage) {
		VBox column = new VBox(30);
		column.setAlignment(Pos.TOP_LEFT);
		column.setPrefWidth(460);

		VBox titleSection = createTitleSection();
		VBox leftMenu = createLeftMenu(stage);

		column.getChildren().addAll(titleSection, leftMenu);
		return column;
	}

	private VBox createLeftMenu(Stage stage) {
		VBox box = new VBox(16);
		box.setPrefWidth(350);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new javafx.geometry.Insets(0, 0, 0, 0));

		Button play = neonButton("▶ PLAY", UITheme.SECONDARY); // Orange
		Button tutorial = neonButton("📖 TUTORIAL", UITheme.PRIMARY); // Cyan
		Button ranking = neonButton("🏆 RANKING", Color.web("#FFD700")); // Gold
		Button options = neonButton("⚙ OPTIONS", Color.web("#CCCCCC")); // Light gray
		Button quit = neonButton("✖ QUIT", UITheme.DANGER); // Red
		
		play.setOnAction(e -> PlayDifficultyPageJava.showOnStage(stage, stage.getScene()));
		tutorial.setOnAction(e -> TutorialPageJava.showOnStage(stage, stage.getScene()));
		ranking.setOnAction(e -> RankingPageJava.showOnStage(stage, stage.getScene()));
		options.setOnAction(e -> OptionsPageJava.showOptionsOnStage(stage, stage.getScene()));
		quit.setOnAction(e -> showQuitConfirm());

		play.setStyle(play.getStyle() + "-fx-font-size: 22px;");
		
		box.getChildren().addAll(play, tutorial, ranking, options, quit);
		return box;
	}

	private StackPane createQuitConfirmOverlay(Stage stage) {
		StackPane overlayRoot = new StackPane();
		overlayRoot.setVisible(false);
		overlayRoot.setManaged(false);
		overlayRoot.setPickOnBounds(true);

		Pane dimmer = new Pane();
		dimmer.setStyle("-fx-background-color: rgba(0,0,0,0.62);");
		dimmer.setPrefSize(1400, 800);

		VBox dialog = new VBox(18);
		dialog.setAlignment(Pos.CENTER);
		dialog.setPadding(new javafx.geometry.Insets(22, 26, 22, 26));
		dialog.setPrefSize(500, 230);
		dialog.setMinSize(500, 230);
		dialog.setMaxSize(500, 230);
		dialog.setStyle(
			"-fx-background-color: rgba(10,18,30,0.95);" +
			"-fx-border-color: rgba(0,255,255,0.4);" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;"
		);

		DropShadow dialogGlow = new DropShadow();
		dialogGlow.setColor(Color.color(0, 1, 1, 0.24));
		dialogGlow.setRadius(24);
		dialog.setEffect(dialogGlow);

		Text title = new Text("EXIT CONFIRMATION");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));
		title.setFill(Color.web("#00FFFF"));

		Text message = new Text("Do you want to exit?");
		message.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
		message.setFill(Color.web("#C7D8E5"));

		HBox actions = new HBox(14);
		actions.setAlignment(Pos.CENTER);

		Button stayButton = createDialogButton("STAY", Color.web("#00FF9C"));
		Button exitButton = createDialogButton("EXIT", Color.web("#FF6B6B"));

		stayButton.setOnAction(e -> hideQuitConfirm());
		exitButton.setOnAction(e -> stage.close());

		actions.getChildren().addAll(stayButton, exitButton);
		dialog.getChildren().addAll(title, message, actions);

		overlayRoot.getChildren().addAll(dimmer, dialog);
		return overlayRoot;
	}

	private Button createDialogButton(String text, Color color) {
		Button button = new NeonButton(text, color, 14, 8, 14, 8);
		button.setMinWidth(120);
		return button;
	}

	private void showQuitConfirm() {
		if (quitConfirmOverlay != null) {
			quitConfirmOverlay.setManaged(true);
			quitConfirmOverlay.setVisible(true);
		}
	}

	private void hideQuitConfirm() {
		if (quitConfirmOverlay != null) {
			quitConfirmOverlay.setVisible(false);
			quitConfirmOverlay.setManaged(false);
		}
	}

	private StackPane createAnimatedRobotPanel() {
		StackPane panel = new StackPane();
		panel.setPrefSize(760, 560);
		panel.setStyle(
			"-fx-background-color: rgba(10,18,30,0.58);" +
			"-fx-border-color: rgba(0,255,255,0.26);" +
			"-fx-border-width: 1.6;" +
			"-fx-border-radius: 14;" +
			"-fx-background-radius: 14;"
		);

		DropShadow panelGlow = new DropShadow();
		panelGlow.setColor(Color.color(0, 1, 1, 0.22));
		panelGlow.setRadius(24);
		panel.setEffect(panelGlow);

		Pane arena = new Pane();
		arena.setPrefSize(700, 500);
		arena.setStyle("-fx-background-color: rgba(10,20,32,0.28);");

		Text hint = new Text("RUNNING ROBOT PREVIEW");
		hint.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
		hint.setFill(Color.web("#9ED9E5"));
		hint.setOpacity(0.88);
		hint.setLayoutX(200);
		hint.setLayoutY(58);

		Text stateText = new Text("MODE: PATROL  |  STATUS: ONLINE");
		stateText.setFont(Font.font("Arial", FontWeight.BOLD, 13));
		stateText.setFill(Color.web("#87D8E7"));
		stateText.setLayoutX(34);
		stateText.setLayoutY(84);

		Text interactHint = new Text("TIP: Click panel to drop beacon, click robot to boost speed");
		interactHint.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
		interactHint.setFill(Color.web("#6CB4C4"));
		interactHint.setLayoutX(34);
		interactHint.setLayoutY(104);

		Pane track = new Pane();
		track.setPrefSize(620, 130);
		track.setLayoutX(40);
		track.setLayoutY(300);

		double waveStartX = 65;
		double waveWidth = 500;
		double waveCenterY = 50;
		double waveAmplitude = 34;
		double waveCount = 4;

		Polyline waveGlow = new Polyline();
		Polyline waveLine = new Polyline();
		for (int i = 0; i <= 120; i++) {
			double progress = i / 120.0;
			double x = waveStartX + waveWidth * progress;
			double y = waveCenterY + waveAmplitude * Math.sin(progress * waveCount * Math.PI * 2);
			waveGlow.getPoints().addAll(x, y);
			waveLine.getPoints().addAll(x, y);
		}

		waveGlow.setStroke(Color.color(0.53, 0.86, 1.0, 0.26));
		waveGlow.setStrokeWidth(12);
		waveGlow.setFill(null);

		waveLine.setStroke(Color.color(0.7, 0.94, 1.0, 0.7));
		waveLine.setStrokeWidth(4.2);
		waveLine.setFill(null);

		track.getChildren().addAll(waveGlow, waveLine);

		Rectangle scanLine = new Rectangle(6, 110, Color.color(0, 1, 1, 0.26));
		scanLine.setLayoutX(48);
		scanLine.setLayoutY(270);
		scanLine.setArcWidth(8);
		scanLine.setArcHeight(8);

		TranslateTransition scanAnim = new TranslateTransition(Duration.millis(2400), scanLine);
		scanAnim.setFromX(0);
		scanAnim.setToX(585);
		scanAnim.setAutoReverse(true);
		scanAnim.setCycleCount(Animation.INDEFINITE);
		scanAnim.play();

		double[] markerProgress = new double[] { 0.08, 0.28, 0.5, 0.72, 0.92 };
		for (double progress : markerProgress) {
			double markerX = waveStartX + waveWidth * progress;
			double markerY = 300 + waveCenterY + waveAmplitude * Math.sin(progress * waveCount * Math.PI * 2);

			Circle node = new Circle(4, Color.color(0.58, 0.9, 1.0, 0.65));
			node.setLayoutX(markerX);
			node.setLayoutY(markerY);

			Circle ring = new Circle(7, Color.TRANSPARENT);
			ring.setStroke(Color.color(0.58, 0.9, 1.0, 0.35));
			ring.setLayoutX(markerX);
			ring.setLayoutY(markerY);

			node.setOnMouseEntered(e -> {
				node.setRadius(5.4);
				stateText.setText("MODE: PATROL  |  TARGET NODE: " + (int) markerX);
			});
			node.setOnMouseExited(e -> {
				node.setRadius(4);
				stateText.setText("MODE: PATROL  |  STATUS: ONLINE");
			});
			arena.getChildren().addAll(ring, node);
		}

		StackPane robotNode = new StackPane();
		robotNode.setPrefSize(120, 150);
		robotNode.setLayoutX(45);
		robotNode.setLayoutY(225);

		URL robotUrl = getClass().getResource("/images/robot.png");
		if (robotUrl != null) {
			ImageView robot = new ImageView(new Image(robotUrl.toExternalForm()));
			robot.setFitWidth(120);
			robot.setPreserveRatio(true);
			robotNode.getChildren().add(robot);
		} else {
			Text robotText = new Text("🤖");
			robotText.setFont(Font.font("Arial", FontWeight.BOLD, 86));
			robotText.setFill(Color.web("#7BE3FF"));
			robotNode.getChildren().add(robotText);
		}

		DropShadow robotGlow = new DropShadow();
		robotGlow.setColor(Color.web("#7BE3FF"));
		robotGlow.setRadius(34);
		robotGlow.setSpread(0.26);
		robotNode.setEffect(robotGlow);
		robotNode.setCursor(Cursor.HAND);

		// Trạng thái tốc độ
		boolean[] isBoost = { false };
		Text speedIndicator = new Text("● NORMAL");
		speedIndicator.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		speedIndicator.setFill(Color.web("#00FF9C"));
		speedIndicator.setLayoutX(540);
		speedIndicator.setLayoutY(84);


		// Container cho animation hiện tại
		Timeline[] currentAnimation = { null };

		// Tạo animation sóng uốn lượn
		currentAnimation[0] = createWavyAnimation(robotNode);
		currentAnimation[0].setRate(1.0);
		robotNode.setOnMouseEntered(e -> {
			robotGlow.setRadius(44);
			ScaleTransition grow = new ScaleTransition(Duration.millis(170), robotNode);
			grow.setToX(1.08);
			grow.setToY(1.08);
			grow.play();
			String boostText = isBoost[0] ? "BOOST" : "NORMAL";
			stateText.setText("MODE: PATROL  |  ROBOT: READY (" + boostText + ")");
		});

		robotNode.setOnMouseExited(e -> {
			robotGlow.setRadius(34);
			ScaleTransition shrink = new ScaleTransition(Duration.millis(170), robotNode);
			shrink.setToX(1.0);
			shrink.setToY(1.0);
			shrink.play();
			String boostText = isBoost[0] ? "BOOST" : "NORMAL";
			stateText.setText("MODE: PATROL  |  SPEED: " + boostText);
		});

		robotNode.setOnMouseClicked(e -> {
			isBoost[0] = !isBoost[0];
			if (currentAnimation[0] != null) {
				currentAnimation[0].setRate(isBoost[0] ? 1.8 : 1.0);
			}

			// Hiệu ứng flash khi boost
			FadeTransition flash = new FadeTransition(Duration.millis(150), speedIndicator);
			flash.setFromValue(0.5);
			flash.setToValue(1.0);
			flash.setCycleCount(2);
			flash.setAutoReverse(true);
			flash.play();
			
			String newState = isBoost[0] ? "⚡ BOOST: ACTIVATED" : "● NORMAL: ACTIVATED";
			stateText.setText("MODE: PATROL  |  " + newState);
			
			if (isBoost[0]) {
				speedIndicator.setText("⚡ BOOST");
				speedIndicator.setFill(Color.web("#FF6B6B"));
				robotGlow.setColor(Color.web("#FF7E7E"));
				robotGlow.setRadius(44);
			} else {
				speedIndicator.setText("● NORMAL");
				speedIndicator.setFill(Color.web("#00FF9C"));
				robotGlow.setColor(Color.web("#7BE3FF"));
				robotGlow.setRadius(34);
			}
			
			e.consume();
		});

		arena.setOnMouseClicked(e -> {
			if (e.getTarget() == robotNode) {
				return;
			}
			Circle pulse = new Circle(6, Color.color(0, 1, 1, 0.72));
			pulse.setLayoutX(e.getX());
			pulse.setLayoutY(e.getY());
			arena.getChildren().add(pulse);

			ScaleTransition pulseScale = new ScaleTransition(Duration.millis(460), pulse);
			pulseScale.setFromX(1);
			pulseScale.setFromY(1);
			pulseScale.setToX(7.4);
			pulseScale.setToY(7.4);

			FadeTransition pulseFade = new FadeTransition(Duration.millis(460), pulse);
			pulseFade.setFromValue(0.8);
			pulseFade.setToValue(0);

			ParallelTransition beaconAnim = new ParallelTransition(pulseScale, pulseFade);
			beaconAnim.setOnFinished(done -> arena.getChildren().remove(pulse));
			beaconAnim.play();

			stateText.setText(
				"MODE: PATROL  |  BEACON: (" + (int) e.getX() + ", " + (int) e.getY() + ")"
			);
		});

		arena.getChildren().addAll(hint, stateText, interactHint, speedIndicator, scanLine, track, robotNode);
		panel.getChildren().add(arena);
		return panel;
	}

	// Tạo animation sóng uốn lượn cho robot
	private Timeline createWavyAnimation(StackPane robotNode) {
		long duration = 3000;
		
		Timeline wavyTimeline = new Timeline();
		wavyTimeline.setCycleCount(Animation.INDEFINITE);
		wavyTimeline.setAutoReverse(true);
		
		int numPoints = 40;
		double amplitude = 34;
		double baseTranslateY = 50;
		double waveCount = 4;
		
		for (int i = 0; i <= numPoints; i++) {
			double progress = (double) i / numPoints;
			double x = progress * 500;
			double y = baseTranslateY + amplitude * Math.sin((progress * waveCount) * Math.PI * 2);
			
			long keyframeTime = (long) (progress * duration);
			
			KeyFrame kf = new KeyFrame(
				Duration.millis(keyframeTime),
				new KeyValue(robotNode.translateXProperty(), x),
				new KeyValue(robotNode.translateYProperty(), y)
			);
			wavyTimeline.getKeyFrames().add(kf);
		}
		
		wavyTimeline.play();
		return wavyTimeline;
	}

	private StackPane createCenterRobot() {
		StackPane robotPane = new StackPane();
		robotPane.setPrefSize(350, 450);
		URL robotUrl = getClass().getResource("/images/robot.png");

		if (robotUrl != null) {
			ImageView robot = new ImageView(new Image(robotUrl.toExternalForm()));
			robot.setFitWidth(350);
			robot.setPreserveRatio(true);

			DropShadow glow = new DropShadow();
			glow.setColor(Color.web("#00FFFF"));
			glow.setRadius(80);
			glow.setSpread(0.5);
			robot.setEffect(glow);

			robotPane.getChildren().add(robot);
		} else {
			VBox robotFallback = new VBox(10);
			robotFallback.setAlignment(Pos.CENTER);

			// Text robotText = new Text("ROBOT");
			// robotText.setFont(Font.font("Orbitron", FontWeight.BOLD, 72));
			// robotText.setFill(Color.web("#00FFFF"));

			// DropShadow glow = new DropShadow();
			// glow.setColor(Color.web("#00FFFF"));
			// glow.setRadius(80);
			// glow.setSpread(0.5);
			// robotText.setEffect(glow);

			// Add some visual indicator for robot
			// Text subtext = new Text("AI Pathfinder");
			// subtext.setFont(Font.font("Arial", 16));
			// subtext.setFill(Color.web("#00FFFF"));
			// subtext.setOpacity(0.6);

			// robotFallback.getChildren().addAll(robotText, subtext);
			// robotPane.getChildren().add(robotFallback);
		}

		return robotPane;
	}

	private Button neonButton(String text, Color color) {
		return new NeonButton(text, color);
	}

	public static void main(String[] args) {
		launch();
	}
}
