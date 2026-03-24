package com.nhom_01.robot_pathfinding.ui.pages;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
		javafx.geometry.Rectangle2D sb = javafx.stage.Screen.getPrimary().getVisualBounds();
		double W = sb.getWidth();
		double H = sb.getHeight();

		// Scale factor relative to 1400×800 reference design
		double sf = Math.min(W / 1400.0, H / 800.0);
		sf = Math.max(0.6, Math.min(1.4, sf)); // clamp for legibility

		double hPad    = Math.max(36, W * 0.045);
		double vPad    = Math.max(28, H * 0.055);
		double gap     = Math.max(28, W * 0.032);
		double menuColW = Math.min(440, Math.max(280, W * 0.33));
		double panelW  = W - menuColW - hPad * 2 - gap;
		double panelH  = H - vPad * 2;

		StackPane root = new StackPane();
		root.setPrefSize(W, H);
		root.getChildren().add(PlayToneBackground.create(W, H, MainMenuJava.class));

		// ── Main two-column layout ──
		HBox mainContent = new HBox(gap);
		mainContent.setPadding(new Insets(vPad, hPad, vPad, hPad));
		mainContent.setAlignment(Pos.CENTER_LEFT);
		mainContent.setMaxWidth(W);
		mainContent.setMaxHeight(H);

		VBox menuColumn = buildMenuColumn(stage, menuColW, panelH, sf);
		StackPane robotPanel = buildRobotPanel(panelW, panelH, sf);
		HBox.setHgrow(robotPanel, Priority.ALWAYS);

		mainContent.getChildren().addAll(menuColumn, robotPanel);

		Pane overlay = new Pane();
		overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
		overlay.setMouseTransparent(true);

		Pane particles = ParticleSystem.createParticles(W, H, 38, UITheme.PRIMARY);
		quitConfirmOverlay = buildQuitOverlay(stage, W, H, sf);

		root.getChildren().addAll(mainContent, particles, overlay, quitConfirmOverlay);
		StackPane.setAlignment(mainContent, Pos.CENTER_LEFT);

		Scene scene = new Scene(root, W, H);
		MenuAudioManager.wireScene(scene);
		MenuAudioManager.startTheme();
		stage.setTitle("Robot Maze");
		stage.setMaximized(true);
		stage.setScene(scene);
		stage.show();
	}

	// ── Left column: title + buttons ──────────────────────────────────────────
	private VBox buildMenuColumn(Stage stage, double colW, double colH, double sf) {
		VBox column = new VBox(Math.max(18, 28 * sf));
		column.setAlignment(Pos.CENTER_LEFT);
		column.setPrefWidth(colW);
		column.setPrefHeight(colH);

		column.getChildren().addAll(
			buildTitleSection(sf),
			buildButtonMenu(stage, colW, sf)
		);
		return column;
	}

	private VBox buildTitleSection(double sf) {
		VBox box = new VBox(Math.max(2, 5 * sf));
		box.setAlignment(Pos.CENTER_LEFT);

		double big  = Math.max(32, 68 * sf);
		double desc = Math.max(12, 17 * sf);

		Text mainTitle = new Text("ROBOT");
		mainTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, big));
		mainTitle.setFill(Color.web("#1F2D3A"));

		DropShadow tg = new DropShadow();
		tg.setColor(Color.color(0.18, 0.50, 0.93, 0.28));
		tg.setRadius(18 * sf);
		mainTitle.setEffect(tg);

		Text subTitle = new Text("MAZE");
		subTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, big));
		subTitle.setFill(Color.web("#EF6C00"));

		DropShadow sg = new DropShadow();
		sg.setColor(Color.color(0.94, 0.42, 0.00, 0.22));
		sg.setRadius(16 * sf);
		subTitle.setEffect(sg);

		Text description = new Text("SMART PATHFINDING · SMOOTH EXPERIENCE");
		description.setFont(Font.font("Arial", FontWeight.NORMAL, desc));
		description.setFill(Color.web("#4F5B62"));

		box.getChildren().addAll(mainTitle, subTitle, description);
		return box;
	}

	private VBox buildButtonMenu(Stage stage, double colW, double sf) {
		VBox box = new VBox(Math.max(10, 16 * sf));
		box.setAlignment(Pos.CENTER_LEFT);
		double btnW = Math.min(colW - 20, Math.max(180, 280 * sf));

		Button play     = btn("▶  PLAY",     UITheme.SECONDARY, btnW);
		Button tutorial = btn("📖  TUTORIAL", UITheme.PRIMARY,   btnW);
		Button ranking  = btn("🏆  RANKING",  Color.web("#F9A825"), btnW);
		Button options  = btn("⚙  OPTIONS",  Color.web("#607D8B"), btnW);
		Button quit     = btn("✖  QUIT",     UITheme.DANGER,     btnW);

		double playFont = Math.max(14, 22 * sf);
		play.setStyle(play.getStyle() + "-fx-font-size: " + playFont + "px;");

		play.setOnAction(e -> PlayDifficultyPageJava.showOnStage(stage, stage.getScene()));
		tutorial.setOnAction(e -> TutorialPageJava.showOnStage(stage, stage.getScene()));
		ranking.setOnAction(e -> RankingPageJava.showOnStage(stage, stage.getScene()));
		options.setOnAction(e -> OptionsPageJava.showOptionsOnStage(stage, stage.getScene()));
		quit.setOnAction(e -> showQuitConfirm());

		box.getChildren().addAll(play, tutorial, ranking, options, quit);
		return box;
	}

	private Button btn(String text, Color color, double prefW) {
		NeonButton b = new NeonButton(text, color);
		b.setPrefWidth(prefW);
		b.setMinWidth(prefW * 0.7);
		return b;
	}

	// ── Right panel: animated duck preview ────────────────────────────────────
	private StackPane buildRobotPanel(double pw, double ph, double sf) {
		StackPane panel = new StackPane();
		panel.setPrefSize(pw, ph);
		panel.setMinSize(pw * 0.5, ph * 0.6);
		panel.setMaxSize(pw, ph);
		panel.setStyle(
			"-fx-background-color: rgba(255,255,255,0.92);" +
			"-fx-border-color: rgba(0,0,0,0.10);" +
			"-fx-border-width: 1.6;" +
			"-fx-border-radius: 14;" +
			"-fx-background-radius: 14;"
		);

		DropShadow ds = new DropShadow();
		ds.setColor(Color.color(0.12, 0.16, 0.20, 0.16));
		ds.setRadius(14);
		panel.setEffect(ds);

		// Inner VBox: label + arena + feature chips
		VBox inner = new VBox(Math.max(12, 18 * sf));
		inner.setAlignment(Pos.CENTER);
		inner.setPadding(new Insets(Math.max(18, 28 * sf)));

		// Label
		Text label = new Text("RUNNING DUCK PREVIEW");
		label.setFont(Font.font("Orbitron", FontWeight.BOLD, Math.max(13, 20 * sf)));
		label.setFill(Color.web("#455A64"));
		label.setOpacity(0.88);

		// Animation arena
		double arenaH = ph * 0.55;
		double arenaW = pw - Math.max(36, 56 * sf) * 2;
		Pane arena = buildDuckArena(arenaW, arenaH, sf);

		// Feature chips row
		HBox chips = buildFeatureChips(sf);

		inner.getChildren().addAll(label, arena, chips);
		panel.getChildren().add(inner);
		return panel;
	}

	private Pane buildDuckArena(double arenaW, double arenaH, double sf) {
		Pane arena = new Pane();
		arena.setPrefSize(arenaW, arenaH);
		arena.setMinSize(arenaW, arenaH);
		arena.setMaxSize(arenaW, arenaH);
		arena.setStyle(
			"-fx-background-color: rgba(247,241,227,0.55);" +
			"-fx-background-radius: 10;"
		);

		double duckSize  = Math.max(70, Math.min(130, arenaH * 0.45));
		double trackH    = 12;
		double trackInset = Math.max(24, arenaW * 0.04);
		double trackW    = arenaW - trackInset * 2;
		double trackY    = arenaH * 0.70;
		double duckX     = trackInset + 4;
		double duckY     = trackY - duckSize - 8;

		// Track
		Pane track = new Pane();
		track.setPrefSize(trackW, trackH);
		track.setLayoutX(trackInset);
		track.setLayoutY(trackY);
		track.setStyle(
			"-fx-background-color: rgba(149,165,166,0.38);" +
			"-fx-background-radius: 6;"
		);

		// Duck node
		StackPane duck = new StackPane();
		duck.setPrefSize(duckSize, duckSize);
		duck.setLayoutX(duckX);
		duck.setLayoutY(duckY);

		URL url = getClass().getResource("/image/vit/Duck.png");
		if (url != null) {
			ImageView img = new ImageView(new Image(url.toExternalForm()));
			img.setFitWidth(duckSize);
			img.setPreserveRatio(true);
			duck.getChildren().add(img);
		} else {
			Text fallback = new Text("🦆");
			fallback.setFont(Font.font("Arial", FontWeight.BOLD, duckSize * 0.7));
			fallback.setFill(Color.web("#FBC02D"));
			duck.getChildren().add(fallback);
		}

		DropShadow dg = new DropShadow();
		dg.setColor(Color.color(0.18, 0.50, 0.93, 0.28));
		dg.setRadius(20 * sf);
		dg.setSpread(0.18);
		duck.setEffect(dg);

		// Animate: travel the full track length minus duck width
		double travelDist = trackW - duckSize - 8;
		double runMs = Math.max(1800, travelDist * 3.5);
		RobotAnimation.animate(duck, travelDist, runMs, -7, 360);

		arena.getChildren().addAll(track, duck);
		return arena;
	}

	private HBox buildFeatureChips(double sf) {
		HBox row = new HBox(Math.max(10, 16 * sf));
		row.setAlignment(Pos.CENTER);

		row.getChildren().addAll(
			chip("🤖  AI PATHFINDING", "#E3F2FD", "#1565C0", sf),
			chip("🎮  PLAYER MODE",    "#E8F5E9", "#2E7D32", sf),
			chip("🏆  LEADERBOARD",    "#FFF8E1", "#F57F17", sf)
		);
		return row;
	}

	private VBox chip(String text, String bg, String textColor, double sf) {
		VBox c = new VBox();
		c.setAlignment(Pos.CENTER);
		c.setPadding(new Insets(Math.max(6, 8 * sf), Math.max(10, 14 * sf),
		                        Math.max(6, 8 * sf), Math.max(10, 14 * sf)));
		c.setStyle(
			"-fx-background-color: " + bg + ";" +
			"-fx-border-color: rgba(0,0,0,0.10);" +
			"-fx-border-width: 1.2;" +
			"-fx-border-radius: 8;" +
			"-fx-background-radius: 8;"
		);

		Text t = new Text(text);
		t.setFont(Font.font("Arial", FontWeight.BOLD, Math.max(10, 13 * sf)));
		t.setFill(Color.web(textColor));
		c.getChildren().add(t);
		return c;
	}

	// ── Quit confirm overlay ──────────────────────────────────────────────────
	private StackPane buildQuitOverlay(Stage stage, double w, double h, double sf) {
		StackPane overlayRoot = new StackPane();
		overlayRoot.setVisible(false);
		overlayRoot.setManaged(false);
		overlayRoot.setPickOnBounds(true);

		Pane dimmer = new Pane();
		dimmer.setStyle("-fx-background-color: rgba(35,30,20,0.40);");
		dimmer.setPrefSize(w, h);

		double dlgW = Math.min(520, w * 0.38);
		double dlgH = Math.max(200, 230 * sf);
		VBox dialog = new VBox(Math.max(14, 18 * sf));
		dialog.setAlignment(Pos.CENTER);
		dialog.setPadding(new Insets(22, 26, 22, 26));
		dialog.setPrefSize(dlgW, dlgH);
		dialog.setMinSize(dlgW, dlgH);
		dialog.setMaxSize(dlgW, dlgH);
		dialog.setStyle(
			"-fx-background-color: rgba(255,255,255,0.98);" +
			"-fx-border-color: rgba(0,0,0,0.14);" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;"
		);

		DropShadow dg = new DropShadow();
		dg.setColor(Color.color(0.16, 0.20, 0.24, 0.18));
		dg.setRadius(12);
		dialog.setEffect(dg);

		Text title = new Text("EXIT CONFIRMATION");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, Math.max(20, 28 * sf)));
		title.setFill(Color.web("#1F2D3A"));

		Text message = new Text("Do you want to exit?");
		message.setFont(Font.font("Arial", FontWeight.NORMAL, Math.max(14, 18 * sf)));
		message.setFill(Color.web("#4F5B62"));

		HBox actions = new HBox(14);
		actions.setAlignment(Pos.CENTER);

		Button stay = new NeonButton("STAY", Color.web("#00FF9C"), 14, 8, 14, 8);
		stay.setMinWidth(120);
		Button exit = new NeonButton("EXIT", Color.web("#FF6B6B"), 14, 8, 14, 8);
		exit.setMinWidth(120);

		stay.setOnAction(e -> hideQuitConfirm());
		exit.setOnAction(e -> stage.close());

		actions.getChildren().addAll(stay, exit);
		dialog.getChildren().addAll(title, message, actions);
		overlayRoot.getChildren().addAll(dimmer, dialog);
		return overlayRoot;
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

	public static void main(String[] args) {
		launch();
	}
}
