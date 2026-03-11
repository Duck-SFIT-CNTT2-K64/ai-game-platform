package com.nhom_01.robot_pathfinding.ui;

import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.Animation;
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
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.animation.Interpolator;
import javafx.util.Duration;

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
		overlay.setStyle("-fx-background-color: rgba(0,0,0,0.24);");
		overlay.setMouseTransparent(true);
		quitConfirmOverlay = createQuitConfirmOverlay(stage);
		root.getChildren().addAll(mainContent, overlay, quitConfirmOverlay);
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

		Button play = neonButton("▶ PLAY", Color.web("#FFA500")); // Orange
		// Button auto = neonButton("⚡ AI AUTO SOLVE", Color.web("#00FFFF"));
		// Button hint = neonButton("🎯 HINT MODE", Color.web("#00FF00"));
		Button algo = neonButton("⚙ SELECT ALGORITHM", Color.web("#FF00FF")); // Magenta
		Button options = neonButton("⚙ OPTIONS", Color.web("#CCCCCC")); // Light gray
		// Button credits = neonButton("👥 CREDITS", Color.web("#CCCCCC"));
		Button quit = neonButton("✖ QUIT", Color.web("#FF4444")); // Red
		play.setOnAction(e -> PlayDifficultyPageJava.showOnStage(stage, stage.getScene()));
		algo.setOnAction(e -> AlgorithmSelectionPageJava.showOnStage(stage, stage.getScene()));
		options.setOnAction(e -> OptionsPageJava.showOptionsOnStage(stage, stage.getScene()));
		quit.setOnAction(e -> showQuitConfirm());

		play.setStyle(play.getStyle() + "-fx-font-size: 22px;");
		
		box.getChildren().addAll(play, algo, options, quit);
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
		Button button = new Button(text);
		String rgbColor = toRgbString(color);
		button.setCursor(Cursor.HAND);
		button.setStyle(
			"-fx-background-color: transparent;" +
			"-fx-text-fill: " + rgbColor + ";" +
			"-fx-font-size: 15px;" +
			"-fx-font-weight: bold;" +
			"-fx-font-family: 'Arial';" +
			"-fx-padding: 8 18 8 18;" +
			"-fx-border-color: " + rgbColor + ";" +
			"-fx-border-width: 1.6;" +
			"-fx-border-radius: 8;" +
			"-fx-background-radius: 8;"
		);

		DropShadow glow = new DropShadow();
		glow.setColor(color);
		glow.setRadius(12);
		glow.setSpread(0.2);
		button.setEffect(glow);

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

		Text hint = new Text("RUNNING ROBOT PREVIEW");
		hint.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
		hint.setFill(Color.web("#9ED9E5"));
		hint.setOpacity(0.88);
		hint.setLayoutX(200);
		hint.setLayoutY(58);

		Pane track = new Pane();
		track.setPrefSize(620, 12);
		track.setLayoutX(40);
		track.setLayoutY(350);
		track.setStyle(
			"-fx-background-color: rgba(135, 176, 191, 0.25);" +
			"-fx-background-radius: 8;"
		);

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

		TranslateTransition run = new TranslateTransition(Duration.millis(3000), robotNode);
		run.setFromX(0);
		run.setToX(500);
		run.setAutoReverse(true);
		run.setCycleCount(Animation.INDEFINITE);

		TranslateTransition bounce = new TranslateTransition(Duration.millis(360), robotNode);
		bounce.setFromY(0);
		bounce.setToY(-7);
		bounce.setAutoReverse(true);
		bounce.setCycleCount(Animation.INDEFINITE);

		run.play();
		bounce.play();

		arena.getChildren().addAll(hint, track, robotNode);
		panel.getChildren().add(arena);
		return panel;
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
		Button btn = new Button(text);
		String rgbColor = toRgbString(color);
		String hexColor = colorToHex(color);

		String baseStyle =
			"-fx-background-color: transparent;" +
			"-fx-text-fill: " + rgbColor + ";" +
			"-fx-font-size: 18px;" +
			"-fx-font-weight: bold;" +
			"-fx-font-family: 'Arial';" +
			"-fx-padding: 10 15 10 15;" +
			"-fx-border-color: " + rgbColor + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 4;" +
			"-fx-cursor: hand;";

		String hoverStyle =
			"-fx-background-color: " + hexColor + "1A;" + // 10% opacity background on hover
			"-fx-text-fill: " + rgbColor + ";" +
			"-fx-font-size: 18px;" +
			"-fx-font-weight: bold;" +
			"-fx-font-family: 'Arial';" +
			"-fx-padding: 10 15 10 15;" +
			"-fx-border-color: " + rgbColor + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 4;" +
			"-fx-cursor: hand;";

		btn.setStyle(baseStyle);
		btn.setCursor(Cursor.HAND);

		// Glow effect
		DropShadow glow = new DropShadow();
		glow.setColor(color);
		glow.setRadius(15);
		glow.setSpread(0.3);
		btn.setEffect(glow);

		// Animate both scale and position so hover intent is immediately noticeable.
		ScaleTransition scaleTransition = new ScaleTransition(Duration.millis(160), btn);
		scaleTransition.setInterpolator(Interpolator.EASE_BOTH);
		TranslateTransition moveTransition = new TranslateTransition(Duration.millis(160), btn);
		moveTransition.setInterpolator(Interpolator.EASE_BOTH);
		ParallelTransition hoverTransition = new ParallelTransition(scaleTransition, moveTransition);

		btn.setOnMouseEntered(e -> {
			btn.setStyle(hoverStyle);
			scaleTransition.setToX(1.06);
			scaleTransition.setToY(1.06);
			moveTransition.setToX(12);
			moveTransition.setToY(0);
			hoverTransition.playFromStart();

			// Enhanced glow on hover
			DropShadow hoverGlow = new DropShadow();
			hoverGlow.setColor(color);
			hoverGlow.setRadius(25);
			hoverGlow.setSpread(0.5);
			btn.setEffect(hoverGlow);
		});

		btn.setOnMouseExited(e -> {
			btn.setStyle(baseStyle);
			scaleTransition.setToX(1.0);
			scaleTransition.setToY(1.0);
			moveTransition.setToX(0);
			moveTransition.setToY(0);
			hoverTransition.playFromStart();

			// Reset glow
			DropShadow normalGlow = new DropShadow();
			normalGlow.setColor(color);
			normalGlow.setRadius(15);
			normalGlow.setSpread(0.3);
			btn.setEffect(normalGlow);
		});

		return btn;
	}

	private String toRgbString(Color c) {
		return String.format(
				"rgb(%d,%d,%d)",
				(int) (c.getRed() * 255),
				(int) (c.getGreen() * 255),
				(int) (c.getBlue() * 255)
		);
	}

	private String colorToHex(Color c) {
		return String.format(
			"#%02X%02X%02X",
			(int) (c.getRed() * 255),
			(int) (c.getGreen() * 255),
			(int) (c.getBlue() * 255)
		);
	}

	public static void main(String[] args) {
		launch();
	}
}
