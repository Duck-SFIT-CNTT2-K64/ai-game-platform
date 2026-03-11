package com.nhom_01.robot_pathfinding.ui;

import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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
import javafx.util.Duration;

import java.net.URL;

public class MainMenuJava extends Application {

	@Override
	public void start(Stage stage) {
		// Root with custom futuristic background
		StackPane root = new StackPane();
		root.setPrefSize(1400, 800);
		root.getChildren().add(createFuturisticBackground());

		// Main content layout
		VBox mainContent = new VBox(40);
		mainContent.setPadding(new javafx.geometry.Insets(40, 60, 40, 60));
		mainContent.setAlignment(Pos.TOP_CENTER);

		// Title + subtitle
		VBox titleSection = createTitleSection();
		mainContent.getChildren().add(titleSection);

		// Game layout: left menu, center robot, right panel
		HBox gameLayout = new HBox(100);
		gameLayout.setAlignment(Pos.CENTER);
		gameLayout.setPrefHeight(500);

		VBox leftMenu = createLeftMenu();
		StackPane centerRobot = createCenterRobot();
		VBox rightPanel = createRightPanel();
		gameLayout.getChildren().addAll(leftMenu, centerRobot, rightPanel);

		mainContent.getChildren().add(gameLayout);

		// Add semi-transparent overlay for readability
		Pane overlay = new Pane();
		overlay.setStyle("-fx-background-color: rgba(0,0,0,0.3);");
		root.getChildren().addAll(mainContent, overlay);
		StackPane.setAlignment(mainContent, Pos.TOP_LEFT);

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

	// Create title section with main title and subtitle
	private VBox createTitleSection() {
		VBox titleBox = new VBox(5);
		titleBox.setAlignment(Pos.CENTER);

		Text mainTitle = new Text("ROBOT");
		mainTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 72));
		mainTitle.setFill(Color.web("#00FFFF")); // Cyan

		Text subtitle = new Text("MAZE");
		subtitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 72));
		subtitle.setFill(Color.web("#FFB800")); // Orange

		Text description = new Text("AI POWERED PATHFINDING");
		description.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
		description.setFill(Color.web("#AAAAAA"));

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

	private VBox createLeftMenu() {
		VBox box = new VBox(18);
		box.setPrefWidth(280);
		box.setAlignment(Pos.CENTER_LEFT);
		box.setPadding(new javafx.geometry.Insets(0, 0, 90, 0));

		Button play = neonButton("▶ PLAY", Color.web("#FFA500")); // Orange
		Button auto = neonButton("⚡ AI AUTO SOLVE", Color.web("#00FFFF")); // Cyan
		Button hint = neonButton("🎯 HINT MODE", Color.web("#00FF00")); // Lime
		Button algo = neonButton("⚙ SELECT ALGORITHM", Color.web("#FF00FF")); // Magenta
		Button options = neonButton("⚙ OPTIONS", Color.web("#CCCCCC")); // Light gray
		Button credits = neonButton("👥 CREDITS", Color.web("#CCCCCC")); // Light gray
		Button quit = neonButton("✖ QUIT", Color.web("#FF4444")); // Red

		play.setStyle(play.getStyle() + "-fx-font-size: 22px;");
		
		box.getChildren().addAll(play, auto, hint, algo, options, credits, quit);
		return box;
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

	private VBox createRightPanel() {
		VBox panel = new VBox(18);
		panel.setPrefWidth(280);
		panel.setAlignment(Pos.CENTER_LEFT);
		panel.setPadding(new javafx.geometry.Insets(20, 0, 20, 0));

		Text difficulty = new Text("⚡ DIFFICULTY");
		difficulty.setFont(Font.font("Orbitron", FontWeight.BOLD, 18));
		difficulty.setFill(Color.web("#AAAAAA"));

		Button easy = neonButton("EASY", Color.web("#00FF00")); // Lime
		Button normal = neonButton("NORMAL", Color.web("#FFA500")); // Orange
		Button hard = neonButton("HARD", Color.web("#FF4444")); // Red

		VBox statsBox = new VBox(8);
		statsBox.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

		Text heart = new Text("❤ LIVES: 5");
		heart.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		heart.setFill(Color.web("#FF6B6B"));

		Text level = new Text("📍 LEVEL: 01");
		level.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		level.setFill(Color.web("#00FFFF"));

		Text best = new Text("🏆 BEST: ----");
		best.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		best.setFill(Color.web("#FFD700"));

		statsBox.getChildren().addAll(heart, level, best);

		panel.getChildren().addAll(difficulty, easy, normal, hard, statsBox);
		return panel;
	}

	private Button neonButton(String text, Color color) {
		Button btn = new Button(text);
		String rgbColor = toRgbString(color);
		String hexColor = colorToHex(color);
		
		btn.setStyle(
			"-fx-background-color: transparent;" +
			"-fx-text-fill: " + rgbColor + ";" +
			"-fx-font-size: 18px;" +
			"-fx-font-weight: bold;" +
			"-fx-font-family: 'Arial';" +
			"-fx-padding: 10 15 10 15;" +
			"-fx-border-color: " + rgbColor + ";" +
			"-fx-border-width: 2;" +
			"-fx-border-radius: 4;" +
			"-fx-cursor: hand;"
		);

		// Glow effect
		DropShadow glow = new DropShadow();
		glow.setColor(color);
		glow.setRadius(15);
		glow.setSpread(0.3);
		btn.setEffect(glow);

		// Scale animation on hover
		ScaleTransition st = new ScaleTransition(Duration.millis(150), btn);

		btn.setOnMouseEntered(e -> {
			btn.setStyle(
				btn.getStyle() +
				"-fx-background-color: " + hexColor + "1A;" // 10% opacity background on hover
			);
			st.setToX(1.08);
			st.setToY(1.08);
			st.playFromStart();

			// Enhanced glow on hover
			DropShadow hoverGlow = new DropShadow();
			hoverGlow.setColor(color);
			hoverGlow.setRadius(25);
			hoverGlow.setSpread(0.5);
			btn.setEffect(hoverGlow);
		});

		btn.setOnMouseExited(e -> {
			btn.setStyle(
				"-fx-background-color: transparent;" +
				"-fx-text-fill: " + rgbColor + ";" +
				"-fx-font-size: 18px;" +
				"-fx-font-weight: bold;" +
				"-fx-font-family: 'Arial';" +
				"-fx-padding: 10 15 10 15;" +
				"-fx-border-color: " + rgbColor + ";" +
				"-fx-border-width: 2;" +
				"-fx-border-radius: 4;" +
				"-fx-cursor: hand;"
			);
			st.setToX(1.0);
			st.setToY(1.0);
			st.playFromStart();

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
