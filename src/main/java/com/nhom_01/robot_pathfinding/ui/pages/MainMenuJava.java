package com.nhom_01.robot_pathfinding.ui.pages;

import javafx.animation.TranslateTransition;
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
import javafx.util.Duration;

import com.nhom_01.robot_pathfinding.ui.animation.ParticleSystem;
import com.nhom_01.robot_pathfinding.ui.animation.RobotAnimation;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
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
		overlay.setStyle("-fx-background-color: rgba(255,255,255,0.08);");
		overlay.setMouseTransparent(true);
		Pane particles = ParticleSystem.createParticles(1400, 800, 42, UITheme.PRIMARY);
		quitConfirmOverlay = createQuitConfirmOverlay(stage);
		root.getChildren().addAll(mainContent, particles, overlay, quitConfirmOverlay);
		StackPane.setAlignment(mainContent, Pos.CENTER_LEFT);

		Scene scene = new Scene(root, 1400, 800);
		MenuAudioManager.wireScene(scene);
		MenuAudioManager.startTheme();
		stage.setTitle("Robot Maze");
		stage.setScene(scene);
		stage.show();
	}

	// Create futuristic background with grid and glow effects
	private Pane createFuturisticBackground() {
		return PlayToneBackground.create(1400, 800, MainMenuJava.class);
	}

	// Create title section shown directly above menu buttons.
	private VBox createTitleSection() {
		VBox titleBox = new VBox(5);
		titleBox.setAlignment(Pos.CENTER_LEFT);

		Text mainTitle = new Text("ROBOT");
		mainTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 68));
		mainTitle.setFill(Color.web("#1F2D3A"));

		Text subtitle = new Text("MAZE");
		subtitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 68));
		subtitle.setFill(Color.web("#EF6C00"));

		Text description = new Text("SMART PATHFINDING - SMOOTH EXPERIENCE");
		description.setFont(Font.font("Arial", FontWeight.NORMAL, 17));
		description.setFill(Color.web("#4F5B62"));

		titleBox.getChildren().addAll(mainTitle, subtitle, description);

		DropShadow titleGlow = new DropShadow();
		titleGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.28));
		titleGlow.setRadius(18);
		mainTitle.setEffect(titleGlow);

		DropShadow subtitleGlow = new DropShadow();
		subtitleGlow.setColor(Color.color(0.94, 0.42, 0.00, 0.22));
		subtitleGlow.setRadius(16);
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
		Button ranking = neonButton("🏆 RANKING", Color.web("#F9A825"));
		Button options = neonButton("⚙ OPTIONS", Color.web("#607D8B"));
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
		dimmer.setStyle("-fx-background-color: rgba(35,30,20,0.40);");
		dimmer.setPrefSize(1400, 800);

		VBox dialog = new VBox(18);
		dialog.setAlignment(Pos.CENTER);
		dialog.setPadding(new javafx.geometry.Insets(22, 26, 22, 26));
		dialog.setPrefSize(500, 230);
		dialog.setMinSize(500, 230);
		dialog.setMaxSize(500, 230);
		dialog.setStyle(
			"-fx-background-color: rgba(255,255,255,0.98);" +
			"-fx-border-color: rgba(0,0,0,0.14);" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;"
		);

		DropShadow dialogGlow = new DropShadow();
		dialogGlow.setColor(Color.color(0.16, 0.20, 0.24, 0.18));
		dialogGlow.setRadius(12);
		dialog.setEffect(dialogGlow);

		Text title = new Text("EXIT CONFIRMATION");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));
		title.setFill(Color.web("#1F2D3A"));

		Text message = new Text("Do you want to exit?");
		message.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
		message.setFill(Color.web("#4F5B62"));

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
			"-fx-background-color: rgba(255,255,255,0.92);" +
			"-fx-border-color: rgba(0,0,0,0.10);" +
			"-fx-border-width: 1.6;" +
			"-fx-border-radius: 14;" +
			"-fx-background-radius: 14;"
		);

		DropShadow panelGlow = new DropShadow();
		panelGlow.setColor(Color.color(0.12, 0.16, 0.20, 0.16));
		panelGlow.setRadius(14);
		panel.setEffect(panelGlow);

		Pane arena = new Pane();
		arena.setPrefSize(700, 500);

		Text hint = new Text("RUNNING DUCK PREVIEW");
		hint.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
		hint.setFill(Color.web("#455A64"));
		hint.setOpacity(0.88);
		hint.setLayoutX(215);
		hint.setLayoutY(58);

		Pane track = new Pane();
		track.setPrefSize(620, 12);
		track.setLayoutX(40);
		track.setLayoutY(350);
		track.setStyle(
			"-fx-background-color: rgba(149, 165, 166, 0.34);" +
			"-fx-background-radius: 8;"
		);

		StackPane robotNode = new StackPane();
		robotNode.setPrefSize(120, 150);
		robotNode.setLayoutX(45);
		robotNode.setLayoutY(225);

		URL robotUrl = getClass().getResource("/image/vit/Duck.png");
		if (robotUrl != null) {
			ImageView robot = new ImageView(new Image(robotUrl.toExternalForm()));
			robot.setFitWidth(120);
			robot.setPreserveRatio(true);
			robotNode.getChildren().add(robot);
		} else {
			Text robotText = new Text("🦆");
			robotText.setFont(Font.font("Arial", FontWeight.BOLD, 86));
			robotText.setFill(Color.web("#FBC02D"));
			robotNode.getChildren().add(robotText);
		}

		DropShadow robotGlow = new DropShadow();
		robotGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.28));
		robotGlow.setRadius(20);
		robotGlow.setSpread(0.18);
		robotNode.setEffect(robotGlow);

		RobotAnimation.animate(robotNode, 500, 3000, -7, 360);

		arena.getChildren().addAll(hint, track, robotNode);
		panel.getChildren().add(arena);
		return panel;
	}

	private StackPane createCenterRobot() {
		StackPane robotPane = new StackPane();
		robotPane.setPrefSize(350, 450);
		URL robotUrl = getClass().getResource("/image/vit/Duck.png");

		if (robotUrl != null) {
			ImageView robot = new ImageView(new Image(robotUrl.toExternalForm()));
			robot.setFitWidth(350);
			robot.setPreserveRatio(true);

			DropShadow glow = new DropShadow();
			glow.setColor(Color.color(0.18, 0.50, 0.93, 0.26));
			glow.setRadius(26);
			glow.setSpread(0.2);
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
