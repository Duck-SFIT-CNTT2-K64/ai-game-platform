package com.nhom_01.robot_pathfinding.ui;

import java.util.function.BiConsumer;

import com.nhom_01.robot_pathfinding.ai.AStar;
import com.nhom_01.robot_pathfinding.ai.BFS;
import com.nhom_01.robot_pathfinding.ai.DFS;
import com.nhom_01.robot_pathfinding.ai.SearchAlgorithm;
import com.nhom_01.robot_pathfinding.core.CellType;
import com.nhom_01.robot_pathfinding.core.CollectedPowerUp;
import com.nhom_01.robot_pathfinding.core.GameSettings;
import com.nhom_01.robot_pathfinding.core.Maze;
import com.nhom_01.robot_pathfinding.core.MazeGenerator;
import com.nhom_01.robot_pathfinding.core.PlayerProfile;
import com.nhom_01.robot_pathfinding.core.PowerUp;
import com.nhom_01.robot_pathfinding.core.RankingEntry;
import com.nhom_01.robot_pathfinding.core.RankingManager;
import com.nhom_01.robot_pathfinding.core.SearchResult;
import com.nhom_01.robot_pathfinding.core.State;
import com.nhom_01.robot_pathfinding.game.GameEngine;
import com.nhom_01.robot_pathfinding.game.GameState;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.InventoryPanel;
import com.nhom_01.robot_pathfinding.ui.components.ItemCardSelectionModal;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class PlayGamePage {

	/** Minimum ms between bot engine steps (used as floor for delay slider). */
	private static final long BOT_CELL_ANIM_NANOS  = 16_000_000L;
	/** Time per grid-cell for player continuous motion (130 ms ≈ 7-8 cells/s). */
	private static final long PLAYER_STEP_NANOS    = 130_000_000L;

	private enum PlayMode {
		PLAYER,
		BOT
	}

	private PlayGamePage() {
	}

	public static void showOnStage(Stage stage, Scene previousScene, String difficulty, String algorithmName) {
		showBotOnStage(stage, previousScene, difficulty, algorithmName);
	}

	public static void showBotOnStage(Stage stage, Scene previousScene, String difficulty, String algorithmName) {
		stage.setScene(buildScene(stage, previousScene, difficulty, algorithmName, PlayMode.BOT, null, false));
	}

	public static void showBotOnStage(Stage stage, Scene previousScene, String difficulty, String algorithmName, Maze maze) {
		stage.setScene(buildScene(stage, previousScene, difficulty, algorithmName, PlayMode.BOT, maze, true));
	}

	public static void showPlayerOnStage(Stage stage, Scene previousScene, String difficulty) {
		stage.setScene(buildScene(stage, previousScene, difficulty, null, PlayMode.PLAYER, null, false));
	}

	public static void showPlayerOnStage(Stage stage, Scene previousScene, String difficulty, Maze maze) {
		stage.setScene(buildScene(stage, previousScene, difficulty, null, PlayMode.PLAYER, maze, false));
	}

	private static Scene buildScene(Stage stage, Scene previousScene, String difficulty, String algorithmName, PlayMode mode, Maze existingMaze, boolean isReplay) {
		MazeRenderer.clearState();
		MenuAudioManager.stopTheme();
		javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
		double W = screenBounds.getWidth();
		double H = screenBounds.getHeight();
		GameSettings settings = GameSettings.getInstance();

		// Generate maze with mode-aware configuration (BOT mode excludes items/bombs)
		Maze maze;
		if (existingMaze != null) {
			maze = existingMaze;
		} else {
			MazeGenerator.GameMode generatorMode = mode == PlayMode.BOT 
				? MazeGenerator.GameMode.BOT 
				: MazeGenerator.GameMode.PLAYER;
			maze = MazeGenerator.generate(difficulty, generatorMode);
		}
		
		// Preserve a pristine copy for See Bot Path replay (restores items/bombs)
		final Maze pristineMaze = maze.copy();

		GameEngine engine = null;
		if (mode == PlayMode.BOT) {
			SearchAlgorithm algorithm = resolveAlgorithm(algorithmName);
			engine = new GameEngine(maze, maze.getStart(), maze.getGoal(), algorithm);
			engine.startSearch();
		}
		final GameEngine botEngine = engine;

		State[] playerPos = new State[] { maze.getStart() };
		int[] playerScore = new int[] { 1000 };
		int[] playerLives = new int[] { Math.max(1, maze.getStart().getLives()) };
		boolean[] playerFinished = new boolean[] { false };
		boolean[] selectingPowerUp = new boolean[] { false };
		long[] mysteryPickupAnimStartMs = new long[] { 0L };
		int[] mysteryPickupGx = new int[] { 0 };
		int[] mysteryPickupGy = new int[] { 0 };
		boolean[] mysteryPickupNeedsModal = new boolean[] { false };
		final Runnable[] openItemAfterMysteryHold = new Runnable[1];
		long[] bombTouchAnimStartMs = new long[] { 0L };
		int[] bombTouchGx = new int[] { 0 };
		int[] bombTouchGy = new int[] { 0 };
		long[] moveCommitAtMs = new long[] { 0L };
		int[] moveCommitX = new int[] { 0 };
		int[] moveCommitY = new int[] { 0 };
		boolean[] optionsOpen = new boolean[] { false };
		// Single-step movement: one key press = one cell. No continuous motion from holding.
		// (Continuous movement is only used by the AI ASSIST power-up.)
		KeyCode[]  pendingKey  = new KeyCode[]  { null };
		boolean[]  keyConsumed = new boolean[]  { false }; // prevents key-repeat from re-firing
		double[] masterVolume = new double[] { settings.getMasterVolume() };
		double[] musicVolume = new double[] { settings.getMusicVolume() };
		double[] sfxVolume = new double[] { settings.getSFXVolume() };
		InGameAudio audio = InGameAudio.create();
		boolean[] pathHint = new boolean[] { settings.isShowPathHint() };
		boolean[] aiSuggest = new boolean[] { settings.isEnableAISuggestion() };
		boolean[] highContrast = new boolean[] { settings.isHighContrastLabels() };
		boolean[] reducedMotion = new boolean[] { settings.isReducedMotion() };
		
		// Ranking tracking
		long[] gameStartTime = new long[] { System.currentTimeMillis() };
		
		// Teleport Animation State
		long[] teleportAnimStartMs = new long[] { 0L };
		int[] teleportGx = new int[] { -1 };
		int[] teleportGy = new int[] { -1 };
		final State[] pendingTeleportPos = new State[1];
		int[] stepCounter = new int[] { 0 };
		boolean[] rankingRecorded = new boolean[] { false };

		// Countdown timer (player mode only): Easy=3m, Medium=6m, Hard=9m
		long countdownMs = switch (difficulty.toUpperCase()) {
			case "EASY"   -> 180_000L;
			case "MEDIUM" -> 360_000L;
			default       -> 540_000L;   // HARD
		};
		long[] countdownEndMs = new long[] { gameStartTime[0] + countdownMs };

		// Result-screen state
		boolean[] resultShown    = new boolean[] { false };
		boolean[] playerTimedOut = new boolean[] { false };

		// Ref to the player loop so the result overlay buttons can stop it
		AnimationTimer[] playerLoopRef = new AnimationTimer[] { null };
		
		// Inventory for player mode
		InventoryPanel inventory = mode == PlayMode.PLAYER ? new InventoryPanel() : null;
		// Power-up runtime state (only meaningful in PLAYER mode)
		PowerUpState pw = new PowerUpState();
		Scene[] gameScene = new Scene[1];
		MazeRenderer.DuckFacing[] botDuckFacing = new MazeRenderer.DuckFacing[] { MazeRenderer.DuckFacing.RIGHT };
		boolean[] botPastIntroIdle = new boolean[] { false };
		double[] botRenderX = new double[] { maze.getStart().getX() };
		double[] botRenderY = new double[] { maze.getStart().getY() };
		double[] botFromX = new double[] { maze.getStart().getX() };
		double[] botFromY = new double[] { maze.getStart().getY() };
		double[] botToX = new double[] { maze.getStart().getX() };
		double[] botToY = new double[] { maze.getStart().getY() };
		long[] botAccumulatorNanos = new long[] { 0L };
		long[] botLastFrameNanos = new long[] { 0L };
		MazeRenderer.DuckFacing[] duckFacing = new MazeRenderer.DuckFacing[] { MazeRenderer.DuckFacing.RIGHT };
		double[] playerRenderX = new double[] { maze.getStart().getX() };
		double[] playerRenderY = new double[] { maze.getStart().getY() };
		double[] playerFromX = new double[] { maze.getStart().getX() };
		double[] playerFromY = new double[] { maze.getStart().getY() };
		double[] playerToX = new double[] { maze.getStart().getX() };
		double[] playerToY = new double[] { maze.getStart().getY() };
		long[] playerAccumulatorNanos = new long[] { 0L };
		long[] playerLastFrameNanos   = new long[] { 0L };

		double sideW   = 360;
		double hPad    = 22;
		double hGap    = 18;
		double vPad    = 16;
		double canvasW = W - sideW - hPad * 2 - hGap - 20;
		double canvasH = H - vPad * 2 - 20;

		StackPane root = new StackPane();
		root.setPrefSize(W, H);
		root.getChildren().add(createBackground());

		HBox page = new HBox(hGap);
		page.setPadding(new Insets(vPad, hPad, vPad, hPad));
		page.setAlignment(Pos.TOP_CENTER);

		String initialState = mode == PlayMode.BOT ? String.valueOf(engine.getState()) : "READY";
		Text stateText = createStatText("STATE: " + initialState, Color.web("#00FFFF"));
		Text scoreText = createStatText(
			"SCORE: " + (mode == PlayMode.BOT ? engine.getScore() : playerScore[0]),
			Color.web("#00FF9C")
		);
		Text pathText = createStatText(
			mode == PlayMode.BOT ? "PATH: " + safeSize(engine.getPath()) + " | LIVES: " + engine.getLives() : "LIVES: " + playerLives[0],
			Color.web("#FFB800")
		);
		Text exploredText = createStatText(
			mode == PlayMode.BOT ? "EXPLORED: " + safeSize(engine.getExplored()) : "GOAL: FIND EXIT",
			Color.web("#C7D8E5")
		);

		Canvas mazeCanvas = new Canvas(canvasW, canvasH);
		GraphicsContext gc = mazeCanvas.getGraphicsContext2D();

		StackPane mazeBoard = new StackPane(mazeCanvas);
		mazeBoard.setPrefSize(canvasW + 20, canvasH + 20);
		mazeBoard.setMinSize(canvasW + 20, canvasH + 20);
		mazeBoard.setStyle(
			"-fx-background-color: rgba(138, 236, 255, 0.48);" +
			"-fx-border-color: rgba(130, 190, 140, 0.95);" +
			"-fx-border-width: 3;" +
			"-fx-border-radius: 8;" +
			"-fx-background-radius: 8;"
		);

		// ── HUD overlay (skill chips + toast) — non-blocking, on top of canvas ──
		HBox skillChipsBox = new HBox(10);
		skillChipsBox.setAlignment(Pos.CENTER);
		skillChipsBox.setPickOnBounds(false);

		Text toastMsgText = new Text();
		toastMsgText.setFont(AppFonts.vt323(16));
		toastMsgText.setFill(Color.WHITE);
		HBox toastBox = new HBox(8);
		toastBox.setAlignment(Pos.CENTER);
		toastBox.setPadding(new Insets(10, 24, 10, 24));
		toastBox.setStyle("-fx-background-color: rgba(10,8,28,0.88); -fx-background-radius: 26;");
		toastBox.setOpacity(0);
		toastBox.setMouseTransparent(true);
		toastBox.getChildren().add(toastMsgText);

		VBox mazeHud = new VBox(5, skillChipsBox, toastBox);
		mazeHud.setAlignment(Pos.TOP_CENTER);
		mazeHud.setPadding(new Insets(10, 8, 0, 8));
		mazeHud.setPickOnBounds(false);
		mazeHud.setMouseTransparent(true);
		StackPane.setAlignment(mazeHud, Pos.TOP_CENTER);
		mazeBoard.getChildren().add(mazeHud);

		// Inventory panel for PLAYER mode
		VBox inventorySection = new VBox(8);
		inventorySection.setAlignment(Pos.CENTER_LEFT);
		inventorySection.setPrefHeight(176);
		inventorySection.setMinHeight(176);
		inventorySection.setFillWidth(true);
		inventorySection.setStyle(
			"-fx-background-color: rgba(255,255,255,0.90);" +
			"-fx-border-color: rgba(0,0,0,0.12);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;" +
			"-fx-padding: 8 10 8 10;"
		);
		if (mode == PlayMode.PLAYER) {
			Text inventoryLabel = new Text("Collected Items");
			inventoryLabel.setFont(AppFonts.vt323(13));
			inventoryLabel.setFill(Color.web("#263238"));
			ScrollPane inventoryScroll = new ScrollPane(inventory.getContainer());
			inventoryScroll.setFitToWidth(true);
			inventoryScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
			inventoryScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
			inventoryScroll.setPrefViewportHeight(132);
			inventoryScroll.setMaxHeight(132);
			inventoryScroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
			inventorySection.getChildren().addAll(inventoryLabel, inventoryScroll);
		} else {
			inventorySection.setVisible(false);
			inventorySection.setManaged(false);
		}

		Text statusText = new Text();
		statusText.setFont(AppFonts.vt323(13));
		statusText.setFill(Color.web("#34495E"));
		statusText.setWrappingWidth(312);

		Text gameTimerText = new Text(mode == PlayMode.PLAYER ? "⏱  00:00" : "");
		gameTimerText.setFont(AppFonts.vt323(13));
		gameTimerText.setFill(Color.web("#1976D2"));

		Text currentPosText = new Text("Position: -");
		currentPosText.setFont(AppFonts.vt323(13));
		currentPosText.setFill(Color.web("#22303A"));

		HBox actions = new HBox(5);
		actions.setAlignment(Pos.CENTER_LEFT);

		// Smaller font+padding so all 3 labels fit without truncation inside the 360px panel
		Button replay        = new NeonButton("RESTART",   Color.web("#4E54E8"), 14, 5, 8, 4);
		Button optionsButton = new NeonButton("OPTIONS",   Color.web("#2F80ED"), 14, 5, 8, 4);
		Button backMenu      = new NeonButton("MAIN MENU", Color.web("#BBBBBB"), 14, 5, 8, 4);
		replay.setMaxWidth(Double.MAX_VALUE);
		optionsButton.setMaxWidth(Double.MAX_VALUE);
		backMenu.setMaxWidth(Double.MAX_VALUE);
		HBox.setHgrow(replay,        Priority.ALWAYS);
		HBox.setHgrow(optionsButton, Priority.ALWAYS);
		HBox.setHgrow(backMenu,      Priority.ALWAYS);

		Text settingsTitle = new Text("Settings");
		settingsTitle.setFont(AppFonts.vt323(28));
		settingsTitle.setFill(Color.web("#1F2D3A"));

		Text subtitle = new Text(mode == PlayMode.BOT
			? "BOT | " + difficulty + " | " + algorithmName
			: "PLAYER | " + difficulty);
		subtitle.setFont(AppFonts.vt323(12));
		subtitle.setFill(Color.web("#4F5B62"));

		Slider delaySlider = new Slider(120, 520, 220);
		delaySlider.setShowTickLabels(false);
		delaySlider.setShowTickMarks(false);
		delaySlider.setStyle("-fx-accent: #2f80ed;");
		Text delayText = new Text("Delay: 220ms");
		delayText.setFont(AppFonts.vt323(12));
		delayText.setFill(Color.web("#263238"));
		delaySlider.valueProperty().addListener((obs, ov, nv) -> {
			delayText.setText("Delay: " + Math.round(nv.doubleValue()) + "ms");
			botAccumulatorNanos[0] = 0L;
		});

		Text stateLabel = createStatText("STATE: " + initialState, Color.web("#1976D2"));
		stateLabel.setFont(AppFonts.vt323(13));
		Text scoreLabel = createStatText(
			"SCORE: " + (mode == PlayMode.BOT ? engine.getScore() : playerScore[0]),
			Color.web("#00897B")
		);
		scoreLabel.setFont(AppFonts.vt323(13));
		Text pathLabel = createStatText(
			mode == PlayMode.BOT ? "PATH: " + safeSize(engine.getPath()) + " | LIVES: " + engine.getLives() : "LIVES: " + playerLives[0],
			Color.web("#EF6C00")
		);
		pathLabel.setFont(AppFonts.vt323(13));
		Text exploredLabel = createStatText(
			mode == PlayMode.BOT ? "EXPLORED: " + safeSize(engine.getExplored()) : "GOAL: FIND EXIT",
			Color.web("#455A64")
		);
		exploredLabel.setFont(AppFonts.vt323(13));

		VBox settingsCard = new VBox(10,
			settingsTitle,
			subtitle,
			delayText,
			delaySlider,
			actions
		);
		settingsCard.setPadding(new Insets(16));
		settingsCard.setFillWidth(true);
		settingsCard.setMaxWidth(Double.MAX_VALUE);
		settingsCard.setStyle(
			"-fx-background-color: rgba(255,255,255,0.94);" +
			"-fx-border-color: rgba(0,0,0,0.08);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		Text infoTitle = new Text("Game Info");
		infoTitle.setFont(AppFonts.vt323(25));
		infoTitle.setFill(Color.web("#1F2D3A"));
		GridPane infoGrid = new GridPane();
		infoGrid.setHgap(18);
		infoGrid.setVgap(6);
		infoGrid.add(currentPosText, 0, 0);
		infoGrid.add(stateLabel, 0, 1);
		infoGrid.add(scoreLabel, 0, 2);
		infoGrid.add(pathLabel, 0, 3);
		infoGrid.add(exploredLabel, 1, 0);
		infoGrid.add(gameTimerText, 1, 1);
		infoGrid.add(statusText, 1, 2);
		infoGrid.setMaxWidth(Double.MAX_VALUE);
		ColumnConstraints ccLeft = new ColumnConstraints();
		ccLeft.setPercentWidth(50);
		ColumnConstraints ccRight = new ColumnConstraints();
		ccRight.setPercentWidth(50);
		infoGrid.getColumnConstraints().setAll(ccLeft, ccRight);
		statusText.setWrappingWidth(145);

		VBox infoCard = new VBox(8, infoTitle, infoGrid, inventorySection);
		infoCard.setPadding(new Insets(16));
		infoCard.setFillWidth(true);
		infoCard.setMaxWidth(Double.MAX_VALUE);
		infoCard.setStyle(
			"-fx-background-color: rgba(255,255,255,0.94);" +
			"-fx-border-color: rgba(0,0,0,0.08);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		VBox legendPanel = createLegendPanel();
		legendPanel.setMaxWidth(Double.MAX_VALUE);

		VBox sideColumn = new VBox(12, settingsCard, infoCard, legendPanel);
		sideColumn.setFillWidth(true);
		sideColumn.setPrefWidth(360);
		sideColumn.setMinWidth(360);
		sideColumn.setMaxWidth(360);
		sideColumn.setMaxHeight(Double.MAX_VALUE);
		ScrollPane sideScroll = new ScrollPane(sideColumn);
		sideScroll.setFitToWidth(true);
		sideScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
		sideScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
		sideScroll.setPrefWidth(360);
		sideScroll.setMinWidth(360);
		sideScroll.setMaxWidth(360);
		sideScroll.setStyle("-fx-background-color: transparent; -fx-background-insets: 0;");
		AppFonts.applyTo(sideColumn);

		// ── Toast notification system ────────────────────────────────────────
		Timeline[] toastTL = { null };
		BiConsumer<String, Color> showNotif = (msg, notifColor) -> {
			if (toastTL[0] != null) toastTL[0].stop();
			toastMsgText.setText(msg);
			toastMsgText.setFill(notifColor);
			toastBox.setOpacity(1.0);
			toastTL[0] = new Timeline(new KeyFrame(Duration.millis(2400), e -> {
				FadeTransition ft = new FadeTransition(Duration.millis(500), toastBox);
				ft.setToValue(0.0);
				ft.play();
			}));
			toastTL[0].play();
		};

		// ── Skill chips updater (Throttled for performance) ───────────────────
		long[] lastSkillUpdateMs = {0L};
		Runnable updateSkillChips = () -> {
			long nowMs = System.currentTimeMillis();
			if (nowMs - lastSkillUpdateMs[0] < 250) return; // Only update ~4 times per second
			lastSkillUpdateMs[0] = nowMs;

			skillChipsBox.getChildren().clear();
			if (pw.doubleScore && nowMs < pw.dblScoreUntil)
				skillChipsBox.getChildren().add(makeSkillChip("✨ Double Score: "    + ((pw.dblScoreUntil  - nowMs + 999) / 1000) + "s", "#F9A825"));
			if (pw.revealPath  && nowMs < pw.revealPathUntil)
				skillChipsBox.getChildren().add(makeSkillChip("🗺 Path Reveal: "        + ((pw.revealPathUntil - nowMs + 999) / 1000) + "s", "#43A047"));
			if (pw.bombDetect  && nowMs < pw.detectUntil)
				skillChipsBox.getChildren().add(makeSkillChip("🔍 Bomb Detector: "        + ((pw.detectUntil   - nowMs + 999) / 1000) + "s", "#EF6C00"));
			if (nowMs < pw.speedBoostUntil) {
				skillChipsBox.getChildren().add(makeSkillChip(
					"⚡ Speed Boost: " + ((pw.speedBoostUntil - nowMs + 999) / 1000) + "s", "#0097A7"
				));
			}
			if (nowMs < pw.speedSlowUntil) {
				skillChipsBox.getChildren().add(makeSkillChip(
					"🐢 Slow Mode: " + ((pw.speedSlowUntil - nowMs + 999) / 1000) + "s", "#78909C"
				));
			}
			if (pw.visionBoost && nowMs < pw.visionUntil)
				skillChipsBox.getChildren().add(makeSkillChip("📡 Sonar Radar: " + ((pw.visionUntil - nowMs + 999) / 1000) + "s", "#00ACC1"));
			if (pw.shield)      skillChipsBox.getChildren().add(makeSkillChip("🛡 Shield Active",     "#1E88E5"));
			if (pw.wallRemoval) skillChipsBox.getChildren().add(makeSkillChip("⛏ Wall Removal Readyt",    "#E64A19"));
			if (pw.aiRunning)   skillChipsBox.getChildren().add(makeSkillChip("🤖 AI ASSIST",  "#00695C"));
		};

		Runnable renderFrame = () -> {
			if (mode == PlayMode.BOT) {
				MazeRenderer.render(
					gc,
					maze,
					botEngine.getExplored(),
					botEngine.getPath(),
					botRenderX[0],
					botRenderY[0],
					mazeCanvas.getWidth(),
					mazeCanvas.getHeight(),
					mysteryPickupAnimStartMs[0],
					mysteryPickupGx[0],
					mysteryPickupGy[0],
					bombTouchAnimStartMs[0],
					bombTouchGx[0],
					bombTouchGy[0],
					botDuckFacing[0],
					!botPastIntroIdle[0],
					pw.isFrozen(),
					pw.isDetectingBombs(),
					1.0,
					calculateShieldVisible(pw),
					-1L,
					pw.isVisionBoostActive(),
					teleportAnimStartMs[0],
					(int)teleportGx[0],
					(int)teleportGy[0],
					-1,
					-1,
					pw.flyingItems,
					new MazeRenderer.RenderVfx(
						pw.lifeVfxUntil,
						pw.scoreVfxUntil,
						pw.scoreVfxValue,
						pw.timeVfxUntil,
						pw.iFramesUntil
					)
				);
			} else {
				// ── Determine visual overlays from active power-ups ──────────
				java.util.List<State> pathOverlay     = null;
				java.util.List<State> exploredOverlay = null;

				if (pw.isDetectingBombs()) {
					if (pw.cachedBombs == null) {
						pw.cachedBombs = bombPositions(maze);
					}
					exploredOverlay = pw.cachedBombs;
				} else {
					pw.cachedBombs = null; // Clear cache when not in use
				}

				if (pw.isRevealingPath() || pw.aiRunning) {
					boolean frozen = pw.isFrozen();
					if (pw.cachedPathToGoal == null || 
					    pw.lastPathSourceX != playerPos[0].getX() || 
					    pw.lastPathSourceY != playerPos[0].getY() ||
					    pw.lastPathFrozen != frozen) {
						
						pw.cachedPathToGoal = computePathToGoal(maze, playerPos[0], pw);
						pw.lastPathSourceX = playerPos[0].getX();
						pw.lastPathSourceY = playerPos[0].getY();
						pw.lastPathFrozen = frozen;
					}
					pathOverlay = pw.cachedPathToGoal;
				} else {
					pw.cachedPathToGoal = null;
				}

				long now = System.currentTimeMillis();
				long minTimer = Long.MAX_VALUE;
				long[] timers = {
					pw.shield ? pw.shieldUntil : 0,
					pw.freezeTime ? pw.freezeUntil : 0,
					Math.max(pw.speedBoostUntil, pw.speedSlowUntil),
					pw.revealPath ? pw.revealPathUntil : 0,
					pw.bombDetect ? pw.detectUntil : 0,
					pw.visionBoost ? pw.visionUntil : 0
				};
				boolean anyActive = false;
				for (long t : timers) {
					if (t > now) {
						long rem = t - now;
						if (rem < minTimer) minTimer = rem;
						anyActive = true;
					}
				}
				long finalHeadTimer = anyActive ? minTimer : -1L;

				MazeRenderer.render(
					gc,
					maze,
					exploredOverlay,
					pathOverlay,
					playerRenderX[0],
					playerRenderY[0],
					mazeCanvas.getWidth(),
					mazeCanvas.getHeight(),
					mysteryPickupAnimStartMs[0],
					mysteryPickupGx[0],
					mysteryPickupGy[0],
					bombTouchAnimStartMs[0],
					bombTouchGx[0],
					bombTouchGy[0],
					duckFacing[0],
					stepCounter[0] == 0,
					pw.isFrozen(),
					pw.isDetectingBombs(),
					(double)pw.effectiveStepNs / (double)PLAYER_STEP_NANOS,
					calculateShieldVisible(pw),
					finalHeadTimer,
					pw.isVisionBoostActive(),
					teleportAnimStartMs[0],
					(int)teleportGx[0],
					(int)teleportGy[0],
					(pendingTeleportPos[0] != null) ? pendingTeleportPos[0].getX() : -1,
					(pendingTeleportPos[0] != null) ? pendingTeleportPos[0].getY() : -1,
					pw.flyingItems,
					new MazeRenderer.RenderVfx(
						pw.lifeVfxUntil,
						pw.scoreVfxUntil,
						pw.scoreVfxValue,
						pw.timeVfxUntil,
						pw.iFramesUntil
					)
				);
			}
		};

		if (mode == PlayMode.BOT) {
			State initialBotPos = botEngine.getRobotPosition();
			if (initialBotPos != null) {
				botRenderX[0] = initialBotPos.getX();
				botRenderY[0] = initialBotPos.getY();
				botFromX[0] = initialBotPos.getX();
				botFromY[0] = initialBotPos.getY();
				botToX[0] = initialBotPos.getX();
				botToY[0] = initialBotPos.getY();
			}
		}

		// Ref used by the result overlay to stop the bot loop
		AnimationTimer[] botLoopRef = new AnimationTimer[] { null };

		AnimationTimer loop = null;
		if (mode == PlayMode.BOT) {
			loop = new AnimationTimer() {
				@Override
				public void handle(long now) {
					if (botLastFrameNanos[0] == 0L) {
						botLastFrameNanos[0] = now;
						return;
					}

					long frameDelta = Math.max(0L, now - botLastFrameNanos[0]);
					botLastFrameNanos[0] = now;

					// Accumulate elapsed time — each step spans the full delay interval
					botAccumulatorNanos[0] += frameDelta;
					long stepDurationNanos = Math.max(BOT_CELL_ANIM_NANOS,
							Math.round(delaySlider.getValue() * 1_000_000.0));

					// Advance engine one step per completed interval
					while (botAccumulatorNanos[0] >= stepDurationNanos
							&& botEngine.getState() == GameState.MOVING
							&& bombTouchAnimStartMs[0] == 0) {
						botAccumulatorNanos[0] -= stepDurationNanos;

						State prevPos = botEngine.getRobotPosition();
						int prevLives = botEngine.getLives();
						int prevScore = botEngine.getScore();
						botEngine.update();
						State nextPos = botEngine.getRobotPosition();
						int nextLives = botEngine.getLives();
						int nextScore = botEngine.getScore();

						// (Mystery box removal is handled directly by engine.update() without delay)

						if (nextLives < prevLives) {
							// Check iFrames for bot (though botEngine doesn't know iFrames, we override visually)
							if (System.currentTimeMillis() < pw.iFramesUntil) {
								// Revert life loss visually if botEngine somehow hit a bomb while we think it's invincible
								// (In practice, botEngine is a simple pathfinder, but for visual parity):
								botEngine.setLives(prevLives); 
							} else {
								bombTouchAnimStartMs[0] = System.currentTimeMillis();
								if (nextPos != null) {
									bombTouchGx[0] = nextPos.getX();
									bombTouchGy[0] = nextPos.getY();
								} else if (prevPos != null) {
									bombTouchGx[0] = prevPos.getX();
									bombTouchGy[0] = prevPos.getY();
								}
								// Set invincibility period for bot
								pw.iFramesUntil = System.currentTimeMillis() + 2000;
								showNotif.accept("HIT A BOMB — -1 LIFE!", Color.web("#FF8DA6"));
							}
						}

						if (nextPos != null) {
							// from = last rendered position (no jump even with sub-frame timing)
							botFromX[0] = botRenderX[0];
							botFromY[0] = botRenderY[0];
							botToX[0] = nextPos.getX();
							botToY[0] = nextPos.getY();
						} else if (prevPos != null) {
							botFromX[0] = prevPos.getX();
							botFromY[0] = prevPos.getY();
							botToX[0]   = prevPos.getX();
							botToY[0]   = prevPos.getY();
						}

						if (prevPos != null && nextPos != null
								&& (prevPos.getX() != nextPos.getX() || prevPos.getY() != nextPos.getY())) {
							botPastIntroIdle[0] = true;
							botDuckFacing[0] = MazeRenderer.facingFromGridDelta(
								nextPos.getX() - prevPos.getX(),
								nextPos.getY() - prevPos.getY());
							audio.playFootstep(masterVolume[0], sfxVolume[0]);
						}

						if (!rankingRecorded[0] && (botEngine.getState() == GameState.FINISHED
								|| botEngine.getState() == GameState.NO_PATH)) {
							recordGameRanking(
								difficulty,
								safeSize(botEngine.getPath()),
								gameStartTime[0],
								algorithmName,
								botEngine.getScore(),
								botEngine.getState() == GameState.FINISHED,
								mode
							);
							rankingRecorded[0] = true;
						}

						refreshBotStats(botEngine, stateLabel, scoreLabel, pathLabel, exploredLabel, statusText);
						State pos = botEngine.getRobotPosition();
						if (pos != null) {
							currentPosText.setText("Position: (" + pos.getX() + ", " + pos.getY() + ")");
						}
					}

					// Process bomb animation expiration so it cleans up correctly
					if (bombTouchAnimStartMs[0] > 0) {
						long bel = System.currentTimeMillis() - bombTouchAnimStartMs[0];
						if (bel >= MazeRenderer.BOMB_HIT_TOTAL_MS) {
							bombTouchAnimStartMs[0] = 0;
						}
					}

					// ── Linear interpolation: constant-velocity motion for the entire step ──
					// The duck glides from botFrom → botTo smoothly for the full step duration.
					// No easing = no perceived pause at start/end of each cell transition.
					double progress = stepDurationNanos <= 0
						? 1.0
						: Math.min(1.0, Math.max(0.0,
							(double) botAccumulatorNanos[0] / (double) stepDurationNanos));
				if (bombTouchAnimStartMs[0] > 0 || mysteryPickupAnimStartMs[0] > 0) progress = 0;
				botRenderX[0] = botFromX[0] + (botToX[0] - botFromX[0]) * progress;
				botRenderY[0] = botFromY[0] + (botToY[0] - botFromY[0]) * progress;

				renderFrame.run();

				// ── Show result overlay when bot finishes ─────────────────────────
					if (rankingRecorded[0] && !resultShown[0]) {
					resultShown[0] = true;
					boolean botWon    = botEngine.getState() == GameState.FINISHED;
					int     botScore  = botEngine.getScore();
					int     botSteps  = safeSize(botEngine.getPath());
					int     botExplrd = safeSize(botEngine.getExplored());
					long    botElapse = System.currentTimeMillis() - gameStartTime[0];
					if (botLoopRef[0] != null) botLoopRef[0].stop();
					showResultOverlay(
						root, stage, previousScene, difficulty,
						botWon, false,
						botScore, botSteps, botElapse,
						-(botExplrd + 1), // negative = bot mode; abs()-1 = explored count
						isReplay ? () -> showPlayerOnStage(stage, previousScene, difficulty) 
								 : () -> showBotOnStage(stage, previousScene, difficulty, algorithmName),
						() -> {
							stage.setScene(previousScene);
							MenuAudioManager.startTheme();
						},
						null // No "See Bot" button when already in bot mode
					);
				}
			}
		};
		loop.start();
		botLoopRef[0] = loop;
		} else {
			refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
			statusText.setFill(Color.web("#AEE8FF"));
			statusText.setText("MOVE TO GOAL");
			currentPosText.setText("Position: (" + playerPos[0].getX() + ", " + playerPos[0].getY() + ")");
			playerRenderX[0] = playerPos[0].getX();
			playerRenderY[0] = playerPos[0].getY();
			playerFromX[0]   = playerPos[0].getX();
			playerFromY[0]   = playerPos[0].getY();
			playerToX[0]     = playerPos[0].getX();
			playerToY[0]     = playerPos[0].getY();
			// Pre-fill so the first key press fires immediately
			playerAccumulatorNanos[0] = pw.effectiveStepNs;

			loop = new AnimationTimer() {
				@Override
				public void handle(long now) {
					if (playerLastFrameNanos[0] == 0L) {
						playerLastFrameNanos[0] = now;
						return;
					}

					long frameDelta = Math.max(0L, now - playerLastFrameNanos[0]);
					playerLastFrameNanos[0] = now;

					// ── Countdown timer ──────────────────────────────────────────────
					long remainingMs = Math.max(0, countdownEndMs[0] - System.currentTimeMillis());
					long secLeft = (remainingMs / 1000) % 60;
					long minLeft = remainingMs / 60000;
					gameTimerText.setText(String.format("⏱  %02d:%02d", minLeft, secLeft));
					gameTimerText.setFill(remainingMs < 30_000
						? Color.web("#FF5252")
						: remainingMs < 60_000 ? Color.web("#FF8F00") : Color.web("#1976D2"));

					// ── Timeout check ─────────────────────────────────────────────────
					if (!playerFinished[0] && !selectingPowerUp[0]
							&& mysteryPickupAnimStartMs[0] == 0
							&& remainingMs <= 0) {
						playerFinished[0] = true;
						playerTimedOut[0] = true;
						pendingKey[0]     = null;
						if (!rankingRecorded[0]) {
							recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], false, mode);
							rankingRecorded[0] = true;
						}
						showNotif.accept("⏰  TIME UP! Game over.", Color.web("#FF8F00"));
					}

					long frameDeltaMs = frameDelta / 1_000_000;

					// ── Detect skill expirations → show toast ─────────────────────────
					boolean wasDbl     = pw.doubleScore;
					boolean wasPath    = pw.revealPath;
					boolean wasDetect  = pw.bombDetect;
					boolean wasSpeed   = pw.isSpeedBoostActive() || pw.isSpeedSlowActive();

					if (selectingPowerUp[0]) {
						// Freeze all clocks by shifting their end-points forward by the frame duration
						countdownEndMs[0] += frameDeltaMs;
						gameStartTime[0]  += frameDeltaMs; // offset start time so elapsed stats are correct
						pw.pauseTimers(frameDeltaMs);
					} else {
						pw.tickExpiry();
					}

					if (!selectingPowerUp[0]) {
						if (wasDbl     && !pw.doubleScore)
							showNotif.accept("✨ Double Score has ended.", Color.web("#FFD54F"));
						if (wasPath    && !pw.revealPath)
							showNotif.accept("🗺 Path Reveal has ended.", Color.web("#A5D6A7"));
						if (wasDetect  && !pw.bombDetect)
							showNotif.accept("🔍 Bomb Detector has ended.", Color.web("#FFCC80"));
						if (wasSpeed   && !pw.isSpeedBoostActive() && !pw.isSpeedSlowActive())
							showNotif.accept("⏱ Speed effect has ended.", Color.web("#B3E5FC"));
					}

					// ── Update active-skill chips bar ─────────────────────────────────
					updateSkillChips.run();

					if (mysteryPickupAnimStartMs[0] > 0) {
						long mel = System.currentTimeMillis() - mysteryPickupAnimStartMs[0];
						if (mel >= MazeRenderer.MYSTERY_OPEN_TOTAL_MS) {
							int mx = mysteryPickupGx[0];
							int my = mysteryPickupGy[0];
							maze.setCell(mx, my, CellType.EMPTY);
							mysteryPickupAnimStartMs[0] = 0;
							boolean needModal = mysteryPickupNeedsModal[0];
							mysteryPickupNeedsModal[0] = false;
							if (needModal && openItemAfterMysteryHold[0] != null) {
								openItemAfterMysteryHold[0].run();
							}
						}
					}
					if (bombTouchAnimStartMs[0] > 0) {
						long bel = System.currentTimeMillis() - bombTouchAnimStartMs[0];
						if (bel >= MazeRenderer.BOMB_HIT_TOTAL_MS) {
							maze.setCell(bombTouchGx[0], bombTouchGy[0], CellType.EMPTY);
							bombTouchAnimStartMs[0] = 0;
						}
					}

					if (teleportAnimStartMs[0] > 0) {
						long tel = System.currentTimeMillis() - teleportAnimStartMs[0];
						if (tel >= MazeRenderer.TELEPORT_TOTAL_MS) {
							teleportAnimStartMs[0] = 0;
							State newPos = pendingTeleportPos[0];
							if (newPos != null) {
								playerPos[0] = new State(newPos.getX(), newPos.getY(), playerLives[0]);
								playerRenderX[0] = newPos.getX(); playerRenderY[0] = newPos.getY();
								playerFromX[0] = newPos.getX(); playerFromY[0] = newPos.getY();
								playerToX[0]   = newPos.getX(); playerToY[0]   = newPos.getY();
								refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
								currentPosText.setText("Position: (" + newPos.getX() + ", " + newPos.getY() + ")");
								statusText.setFill(Color.web("#CE93D8"));
								statusText.setText("TELEPORTED to a safe location!");
								pendingTeleportPos[0] = null;
								root.requestFocus();
							}
						}
					}
					if (moveCommitAtMs[0] > 0 && System.currentTimeMillis() >= moveCommitAtMs[0]) {
						int cx = moveCommitX[0];
						int cy = moveCommitY[0];
						moveCommitAtMs[0] = 0; // reset first to prevent re-triggering logic

						processCellLanding(cx, cy, maze, playerPos, playerScore, playerLives, playerFinished,
							statusText, audio, masterVolume, sfxVolume, inventory, gameScene, selectingPowerUp,
							renderFrame, gameStartTime, stepCounter, rankingRecorded, difficulty, algorithmName, pw,
							mysteryPickupAnimStartMs, mysteryPickupGx, mysteryPickupGy, mysteryPickupNeedsModal,
							openItemAfterMysteryHold, bombTouchAnimStartMs, bombTouchGx, bombTouchGy,
							moveCommitAtMs, moveCommitX, moveCommitY, duckFacing,
							playerFromX, playerFromY, playerToX, playerToY, playerRenderX, playerRenderY,
							mode);
					}

					// Use dynamic step duration (affected by SPEED_BOOST / SPEED_SLOW)
					long stepNs = pw.effectiveStepNs;

					// Cap at exactly 1 step so first key press never jumps 2 cells
					playerAccumulatorNanos[0] = Math.min(
						playerAccumulatorNanos[0] + frameDelta,
						stepNs
					);

					// ── AI ASSIST: auto-move along pre-computed path ─────────────────
					if (pw.aiRunning && pw.aiPath != null
							&& pw.aiPathIdx < pw.aiPath.size()
							&& pw.aiPathIdx <= 8) {
						if (playerAccumulatorNanos[0] >= stepNs && !playerFinished[0] && !selectingPowerUp[0]
								&& mysteryPickupAnimStartMs[0] == 0
								&& bombTouchAnimStartMs[0] == 0
								&& moveCommitAtMs[0] == 0) {
							
							playerAccumulatorNanos[0] -= stepNs;
							State nextAi = pw.aiPath.get(pw.aiPathIdx++);
							
							playerFromX[0] = playerRenderX[0];
							playerFromY[0] = playerRenderY[0];
							int prevGx = playerPos[0].getX();
							int prevGy = playerPos[0].getY();
							
							// Trigger core landing logic (Items, Bombs, Goal)
							processCellLanding(
								nextAi.getX(), nextAi.getY(),
								maze, playerPos, playerScore, playerLives, playerFinished,
								statusText, audio, masterVolume, sfxVolume,
								inventory, gameScene, selectingPowerUp, renderFrame,
								gameStartTime, stepCounter, rankingRecorded,
								difficulty, algorithmName, pw,
								mysteryPickupAnimStartMs, mysteryPickupGx, mysteryPickupGy,
								mysteryPickupNeedsModal, openItemAfterMysteryHold,
								bombTouchAnimStartMs, bombTouchGx, bombTouchGy,
								moveCommitAtMs, moveCommitX, moveCommitY,
								duckFacing,
								playerFromX, playerFromY, playerToX, playerToY, playerRenderX, playerRenderY,
								mode
							);

							playerToX[0] = playerPos[0].getX();
							playerToY[0] = playerPos[0].getY();
							duckFacing[0] = MazeRenderer.facingFromGridDelta(
								nextAi.getX() - prevGx, nextAi.getY() - prevGy);
							
							refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0],
								stateLabel, scoreLabel, pathLabel, exploredLabel);
							currentPosText.setText("Position: ("
								+ playerPos[0].getX() + ", " + playerPos[0].getY() + ")");

							if (pw.aiPathIdx >= pw.aiPath.size() || pw.aiPathIdx > 8 || playerFinished[0]) {
								pw.aiRunning = false;
								pw.aiPath = null;
								if (!playerFinished[0]) {
									statusText.setFill(Color.web("#A5D6A7"));
									statusText.setText("AI ASSIST complete — resume moving!");
								}
							}

							// ── Check if AI reached the GOAL ─────────────────────────
							if (maze.getCell(nextAi.getX(), nextAi.getY()) == CellType.GOAL) {
								playerFinished[0] = true;
								pw.aiRunning      = false;
								pw.aiPath         = null;
								statusText.setFill(Color.web("#00FF9C"));
								statusText.setText("AI ASSIST REACHED THE GOAL! 🏆");
							}
						}
					// ── Single-step player movement ──────────────────────────────────
					// One key press fires exactly one step. Key-repeat is ignored.
					} else {
						if (pw.aiRunning) { pw.aiRunning = false; pw.aiPath = null; }

						if (!keyConsumed[0]
								&& pendingKey[0] != null
								&& playerAccumulatorNanos[0] >= stepNs
								&& !playerFinished[0]
								&& !selectingPowerUp[0]
								&& mysteryPickupAnimStartMs[0] == 0
								&& bombTouchAnimStartMs[0] == 0
								&& moveCommitAtMs[0] == 0) {

							playerAccumulatorNanos[0] -= stepNs;
							keyConsumed[0] = true; // mark step as consumed for this press

							// Start from current render position (seamless glide)
							playerFromX[0] = playerRenderX[0];
							playerFromY[0] = playerRenderY[0];

							boolean moved = handlePlayerMove(
								pendingKey[0],
								maze, playerPos, playerScore, playerLives, playerFinished,
								statusText, audio, masterVolume, sfxVolume,
								inventory, gameScene, selectingPowerUp, renderFrame,
								gameStartTime, stepCounter, rankingRecorded,
								difficulty, algorithmName, pw,
								mysteryPickupAnimStartMs, mysteryPickupGx, mysteryPickupGy,
								mysteryPickupNeedsModal, openItemAfterMysteryHold,
								bombTouchAnimStartMs, bombTouchGx, bombTouchGy,
								moveCommitAtMs, moveCommitX, moveCommitY,
								duckFacing,
								playerFromX, playerFromY, playerToX, playerToY, playerRenderX, playerRenderY,
								mode
							);

							// ── SPEED BOOST: Handle double step ─────────────────────
							if (moved && pw.speedBoostUntil > System.currentTimeMillis() && pw.effectiveStepNs < PLAYER_STEP_NANOS) {
								// Calculate second position based on direction
								int dx = 0, dy = 0;
								if (pendingKey[0] == KeyCode.UP || pendingKey[0] == KeyCode.W) dy = -1;
								else if (pendingKey[0] == KeyCode.DOWN || pendingKey[0] == KeyCode.S) dy = 1;
								else if (pendingKey[0] == KeyCode.LEFT || pendingKey[0] == KeyCode.A) dx = -1;
								else if (pendingKey[0] == KeyCode.RIGHT || pendingKey[0] == KeyCode.D) dx = 1;
								
								int nextX = playerPos[0].getX() + dx;
								int nextY = playerPos[0].getY() + dy;
								
								// Validate second step
								if (nextX >= 0 && nextX < maze.getWidth() && nextY >= 0 && nextY < maze.getHeight() 
										&& maze.getCell(nextX, nextY) != CellType.WALL) {
									moveCommitAtMs[0] = System.currentTimeMillis() + (stepNs / 1_000_000 / 2);
									moveCommitX[0] = nextX;
									moveCommitY[0] = nextY;
								}
							}

							playerToX[0] = playerPos[0].getX();
							playerToY[0] = playerPos[0].getY();

							if (moved) {
								refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0],
									stateLabel, scoreLabel, pathLabel, exploredLabel);
								currentPosText.setText("Position: ("
									+ playerPos[0].getX() + ", " + playerPos[0].getY() + ")");
							}
						}
					}

					// ── Linear interpolation — constant-velocity glide ───────────────
					double progress = Math.min(1.0, Math.max(0.0,
						(double) playerAccumulatorNanos[0] / (double) stepNs));
					
					// BOM ANIMATION DELAY: If a bomb was touched, stay at 'from' until explosion finishes
					if (bombTouchAnimStartMs[0] > 0) {
						long elapsedBomb = System.currentTimeMillis() - bombTouchAnimStartMs[0];
						if (elapsedBomb < MazeRenderer.BOMB_HIT_TOTAL_MS) {
							progress = 0;
						}
					}

					playerRenderX[0] = playerFromX[0] + (playerToX[0] - playerFromX[0]) * progress;
					playerRenderY[0] = playerFromY[0] + (playerToY[0] - playerFromY[0]) * progress;

					// --- UPDATE FLYING ITEMS (Magnetic Pull) ---
					java.util.Iterator<FlyingItem> it = pw.flyingItems.iterator();
					while (it.hasNext()) {
						FlyingItem fi = it.next();
						fi.update(playerRenderX[0], playerRenderY[0]);
						if (fi.isArrived()) {
							playerScore[0] += fi.getReward();
							statusText.setFill(Color.web("#9FFFD8"));
							statusText.setText("RADAR VACUUM: Item collected! (+" + fi.getReward() + ")");
							if (openItemAfterMysteryHold[0] != null) {
								if (selectingPowerUp[0]) pw.pendingChoices++;
								else openItemAfterMysteryHold[0].run();
							}
							it.remove();
						}
					}

					renderFrame.run();

					// ── SONAR RADAR: MAGNETIC ITEM ATTRACTION (Hard Perk) ──
					if (pw.visionBoost && System.currentTimeMillis() % 150 < 20) {
						int px = (int)Math.round(playerRenderX[0]);
						int py = (int)Math.round(playerRenderY[0]);
						for (int dx = -2; dx <= 2; dx++) {
							for (int dy = -2; dy <= 2; dy++) {
								if (Math.abs(dx) + Math.abs(dy) > 2) continue; // Manhattan distance 2
								int tx = px + dx; int ty = py + dy;
								if (tx >= 0 && tx < maze.getWidth() && ty >= 0 && ty < maze.getHeight()) {
									if (maze.getCell(tx, ty) == com.nhom_01.robot_pathfinding.core.CellType.ITEM) {
										// Magnetic pull! Remove from grid, add to flying list
										maze.setCell(tx, ty, com.nhom_01.robot_pathfinding.core.CellType.EMPTY);
										int rwd = (pw.doubleScore) ? 360 : 180;
										pw.flyingItems.add(new FlyingItem(tx, ty, rwd));
									}
								}
							}
						}
					}

					// --- TELEPORT STATE TRANSITION ---
					if (teleportAnimStartMs[0] > 0) {
						long elapsed = System.currentTimeMillis() - teleportAnimStartMs[0];
						if (elapsed >= MazeRenderer.TELEPORT_TOTAL_MS) {
							// Finish teleport movement
							State newPos = pendingTeleportPos[0];
							if (newPos != null) {
								playerPos[0] = newPos;
								playerFromX[0] = newPos.getX(); playerFromY[0] = newPos.getY();
								playerToX[0] = newPos.getX(); playerToY[0] = newPos.getY();
								playerRenderX[0] = newPos.getX(); playerRenderY[0] = newPos.getY();
								stepCounter[0]++;
								refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0],
									stateLabel, scoreLabel, pathLabel, exploredLabel);
								currentPosText.setText("Position: (" + newPos.getX() + ", " + newPos.getY() + ")");
							}
							teleportAnimStartMs[0] = 0;
							pendingTeleportPos[0] = null;
						}
					}

					// ── Show result overlay on win/lose/timeout ───────────────────────
					if (playerFinished[0] && !resultShown[0]) {
						resultShown[0] = true;
						pendingKey[0]  = null;
						boolean won     = !playerTimedOut[0] && playerLives[0] > 0;
						boolean timedOut = playerTimedOut[0];
						long elapsedResult = System.currentTimeMillis() - gameStartTime[0];
						int  finalScore  = playerScore[0];
						int  finalSteps  = stepCounter[0];
						int  finalLives  = playerLives[0];
						if (playerLoopRef[0] != null) playerLoopRef[0].stop();
						showResultOverlay(
							root, stage, previousScene, difficulty,
							won, timedOut, finalScore, finalSteps, elapsedResult, finalLives,
							() -> showPlayerOnStage(stage, previousScene, difficulty),
							() -> {
								stage.setScene(previousScene);
								MenuAudioManager.startTheme();
							},
							() -> showBotOnStage(stage, previousScene, difficulty, "A*", pristineMaze.copy())
						);
					}
				}
			};
			loop.start();
			playerLoopRef[0] = loop;
		}

		AnimationTimer finalLoop = loop;
		replay.setOnAction(e -> {
			audio.stopAll();
			if (finalLoop != null) {
				finalLoop.stop();
			}
			if (mode == PlayMode.BOT) {
				showBotOnStage(stage, previousScene, difficulty, algorithmName);
			} else {
				showPlayerOnStage(stage, previousScene, difficulty);
			}
		});
		optionsButton.setOnAction(e -> {
			if (optionsOpen[0]) {
				return;
			}
			optionsOpen[0] = true;
			pendingKey[0]  = null; // discard any pending step while paused
			keyConsumed[0] = false;
			if (finalLoop != null) {
				finalLoop.stop();
			}
			long optionsOpenAtMs = System.currentTimeMillis();
			showInGameOptions(
				root,
				masterVolume,
				musicVolume,
				sfxVolume,
				pathHint,
				aiSuggest,
				highContrast,
				reducedMotion,
				() -> {
					optionsOpen[0] = false;
					long pausedMs = System.currentTimeMillis() - optionsOpenAtMs;
					countdownEndMs[0] += pausedMs;
					gameStartTime[0]  += pausedMs;
					pw.pauseTimers(pausedMs);

					audio.updateVolumes(masterVolume[0], musicVolume[0], sfxVolume[0]);
					if (finalLoop != null) {
						if (mode == PlayMode.BOT) {
							botLastFrameNanos[0] = 0L;
						} else {
							playerLastFrameNanos[0] = 0L;
							// Pre-fill so next key press fires instantly (uses dynamic step)
							playerAccumulatorNanos[0] = pw.effectiveStepNs;
						}
						finalLoop.start();
					}
					root.requestFocus();
				},
				() -> {
					audio.stopAll();
					optionsOpen[0] = false;
					if (finalLoop != null) {
						finalLoop.stop();
					}
					stage.setScene(previousScene);
					MenuAudioManager.startTheme();
				},
				() -> audio.updateVolumes(masterVolume[0], musicVolume[0], sfxVolume[0])
			);
		});

		backMenu.setOnAction(e -> {
			audio.stopAll();
			if (finalLoop != null) {
				finalLoop.stop();
			}
			stage.setScene(previousScene);
			MenuAudioManager.startTheme();
		});

		actions.getChildren().addAll(optionsButton, replay, backMenu);
		page.getChildren().addAll(mazeBoard, sideScroll);

		Pane overlay = new Pane();
		overlay.setStyle("-fx-background-color: rgba(0,0,0,0.14);");
		overlay.setMouseTransparent(true);

		root.getChildren().addAll(page, overlay);

		if (mode == PlayMode.BOT) {
			refreshBotStats(botEngine, stateLabel, scoreLabel, pathLabel, exploredLabel, statusText);
			State pos = botEngine.getRobotPosition();
			if (pos != null) {
				currentPosText.setText("Vi tri hien tai: (" + pos.getX() + ", " + pos.getY() + ")");
			}
		}
		renderFrame.run();

		Scene scene = new Scene(root, W, H);
		AppFonts.applyTo(root);
		gameScene[0] = scene;
		
		if (mode == PlayMode.PLAYER) {
			// Key PRESSED — schedule exactly one step; repeat events are ignored via keyConsumed
			scene.setOnKeyPressed(e -> {
				if (optionsOpen[0] || selectingPowerUp[0]) {
					e.consume();
					return;
				}
				if (isMovementKey(e.getCode())) {
					if (e.getCode() != pendingKey[0]) {
						// New direction pressed: schedule a fresh step
						pendingKey[0]  = e.getCode();
						keyConsumed[0] = false;
					}
					// Same key while already pending/consumed → ignore (prevents key-repeat)
					e.consume();
				}
			});

			// Key RELEASED — clear the pending step so the key must be pressed again
			scene.setOnKeyReleased(e -> {
				if (isMovementKey(e.getCode()) && e.getCode() == pendingKey[0]) {
					pendingKey[0]  = null;
					keyConsumed[0] = false;
				}
			});

			// ── Mirror important status messages to on-screen toast ─────────────
			statusText.textProperty().addListener((obs, ov, nv) -> {
				if (nv == null || nv.isEmpty() || nv.equals(ov)) return;
				// Skip high-frequency / low-importance messages
				if (nv.startsWith("MOVE WITH ARROW KEYS") || nv.startsWith("BLOCKED BY WALL")) return;
				Color fill = statusText.getFill() instanceof Color c ? c : Color.WHITE;
				showNotif.accept(nv, fill);
			});

			// ── Wire inventory: clicking an item in the panel activates its effect ──
			if (inventory != null) {
				inventory.setOnActivateCallback(collected -> {
					if (collected == null || !collected.isActive()) return;
					PowerUp type = collected.getPowerUp();
					handlePowerUpActivation(type, pw, playerScore, playerLives, playerPos, 
						stateLabel, scoreLabel, pathLabel, exploredLabel, statusText, countdownEndMs, 
						maze, playerRenderX, playerRenderY, playerFromX, playerFromY, playerToX, playerToY,
						currentPosText, playerAccumulatorNanos, inventory, root, gameScene, selectingPowerUp,
						teleportAnimStartMs, teleportGx, teleportGy, pendingTeleportPos);

					renderFrame.run();
					root.requestFocus();
				});
			}

			root.requestFocus();
		}

		scene.windowProperty().addListener((obs, oldWindow, newWindow) -> {
			if (oldWindow != null) {
				oldWindow.setOnHidden(null);
			}
			if (newWindow != null) {
				newWindow.setOnHidden(event -> {
					audio.stopAll();
					if (finalLoop != null) {
						finalLoop.stop();
					}
				});
			}
		});

		return scene;
	}

	private static Text createStatText(String text, Color color) {
		Text node = new Text(text);
		node.setFont(AppFonts.vt323(15));
		node.setFill(color);
		return node;
	}

	private static void refreshBotStats(
		GameEngine engine,
		Text stateText,
		Text scoreText,
		Text pathText,
		Text exploredText,
		Text statusText
	) {
		stateText.setText("STATE: " + engine.getState());
		scoreText.setText("SCORE: " + engine.getScore());
		pathText.setText("PATH: " + safeSize(engine.getPath()) + " | LIVES: " + engine.getLives());
		exploredText.setText("EXPLORED: " + safeSize(engine.getExplored()));

		if (engine.getState() == GameState.NO_PATH) {
			statusText.setFill(Color.web("#FF6B6B"));
			statusText.setText(engine.getLives() <= 0 
				? "GAME OVER - BOT OUT OF LIVES!" 
				: "NO PATH FOUND - TRY ANOTHER ALGORITHM OR REPLAY");
		} else if (engine.getState() == GameState.FINISHED) {
			statusText.setFill(Color.web("#00FF9C"));
			statusText.setText("ROBOT REACHED GOAL - FINAL SCORE: " + engine.getScore());
		} else if (engine.getState() == GameState.MOVING) {
			State pos = engine.getRobotPosition();
			if (pos != null) {
				statusText.setFill(Color.web("#C9DCEA"));
				statusText.setText("DUCK POSITION: (" + pos.getX() + ", " + pos.getY() + ")");
			}
		} else {
			statusText.setFill(Color.web("#C9DCEA"));
			statusText.setText("PREPARING SEARCH...");
		}
	}

	private static void refreshPlayerStats(
		State playerPos,
		int score,
		int lives,
		Text stateText,
		Text scoreText,
		Text pathText,
		Text exploredText
	) {
		stateText.setText("STATE: MANUAL");
		scoreText.setText("SCORE: " + Math.max(0, score));
		pathText.setText("LIVES: " + Math.max(0, lives));
		exploredText.setText("POS: (" + playerPos.getX() + ", " + playerPos.getY() + ")");
	}

	private static boolean handlePlayerMove(
		KeyCode code,
		Maze maze,
		State[] playerPos,
		int[] playerScore,
		int[] playerLives,
		boolean[] playerFinished,
		Text statusText,
		InGameAudio audio,
		double[] masterVolume,
		double[] sfxVolume,
		InventoryPanel inventory,
		Scene[] gameScene,
		boolean[] selectingPowerUp,
		Runnable renderFrame,
		long[] gameStartTime,
		int[] stepCounter,
		boolean[] rankingRecorded,
		String difficulty,
		String algorithmName,
		PowerUpState pw,
		long[] mysteryPickupAnimStartMs,
		int[] mysteryPickupGx,
		int[] mysteryPickupGy,
		boolean[] mysteryPickupNeedsModal,
		Runnable[] openItemAfterMysteryHold,
		long[] bombTouchAnimStartMs,
		int[] bombTouchGx,
		int[] bombTouchGy,
		long[] moveCommitAtMs,
		int[] moveCommitX,
		int[] moveCommitY,
		MazeRenderer.DuckFacing[] duckFacing,
		double[] playerFromX, double[] playerFromY,
		double[] playerToX, double[] playerToY,
		double[] playerRenderX, double[] playerRenderY,
		PlayMode mode
	) {
		if (playerFinished[0] || (selectingPowerUp != null && selectingPowerUp[0])) {
			return false;
		}

		int dx = 0, dy = 0;
		switch (code) {
			case UP    -> dy = -1;
			case DOWN  -> dy =  1;
			case LEFT  -> dx = -1;
			case RIGHT -> dx =  1;
			default    -> { return false; }
		}
		if (duckFacing != null && duckFacing.length > 0) {
			duckFacing[0] = MazeRenderer.facingFromGridDelta(dx, dy);
		}

		int nx = playerPos[0].getX() + dx;
		int ny = playerPos[0].getY() + dy;

		CellType nextCell = maze.getCell(nx, ny);

		// ── Wall check: REMOVE_WALL powerup can demolish 1 wall ───────────────
		if (nextCell == CellType.WALL || (nextCell == CellType.BOMB && pw.isFrozen())) {
			if (nextCell == CellType.WALL && pw.wallRemoval) {
				pw.wallRemoval = false;
				maze.setCell(nx, ny, CellType.EMPTY);
				nextCell = CellType.EMPTY;
				statusText.setFill(Color.web("#FFAB91"));
				statusText.setText("WALL DESTROYED!");
			} else {
				if (nextCell == CellType.BOMB) {
					statusText.setFill(Color.web("#81D4FA"));
					statusText.setText("FROZEN BOMB BLOCKED — wait for it to melt!");
				} else {
					statusText.setFill(Color.web("#FFD59A"));
					statusText.setText("BLOCKED BY WALL — try another direction");
				}
				return false;
			}
		}

		// Each step costs 3 score
		playerScore[0] = Math.max(0, playerScore[0] - 3);
		stepCounter[0]++;
		audio.playFootstep(masterVolume[0], sfxVolume[0]);

		processCellLanding(nx, ny, maze, playerPos, playerScore, playerLives, playerFinished,
			statusText, audio, masterVolume, sfxVolume, inventory, gameScene, selectingPowerUp,
			renderFrame, gameStartTime, stepCounter, rankingRecorded, difficulty, algorithmName, pw,
			mysteryPickupAnimStartMs, mysteryPickupGx, mysteryPickupGy, mysteryPickupNeedsModal,
			openItemAfterMysteryHold, bombTouchAnimStartMs, bombTouchGx, bombTouchGy,
			moveCommitAtMs, moveCommitX, moveCommitY, duckFacing,
			playerFromX, playerFromY, playerToX, playerToY, playerRenderX, playerRenderY,
			mode);

		return true;
	}

	/** Core logic for what happens when a player arrives at (or jumps onto) a specific grid cell. */
	private static void processCellLanding(
		int nx, int ny,
		Maze maze,
		State[] playerPos,
		int[] playerScore,
		int[] playerLives,
		boolean[] playerFinished,
		Text statusText,
		InGameAudio audio,
		double[] masterVolume,
		double[] sfxVolume,
		InventoryPanel inventory,
		Scene[] gameScene,
		boolean[] selectingPowerUp,
		Runnable renderFrame,
		long[] gameStartTime,
		int[] stepCounter,
		boolean[] rankingRecorded,
		String difficulty,
		String algorithmName,
		PowerUpState pw,
		long[] mysteryPickupAnimStartMs,
		int[] mysteryPickupGx,
		int[] mysteryPickupGy,
		boolean[] mysteryPickupNeedsModal,
		Runnable[] openItemAfterMysteryHold,
		long[] bombTouchAnimStartMs,
		int[] bombTouchGx,
		int[] bombTouchGy,
		long[] moveCommitAtMs,
		int[] moveCommitX,
		int[] moveCommitY,
		MazeRenderer.DuckFacing[] duckFacing,
		double[] playerFromX, double[] playerFromY,
		double[] playerToX, double[] playerToY,
		double[] playerRenderX, double[] playerRenderY,
		PlayMode mode
	) {
		CellType cell = maze.getCell(nx, ny);

		// ── Bomb ──────────────────────────────────────────────────────────────
		if (cell == CellType.BOMB) {
			// IFRAMES: No damage or score loss if invincible
			if (System.currentTimeMillis() < pw.iFramesUntil) {
				return;
			}
			if (pw.shield) {
				pw.shield = false;
				pw.shieldUntil = 0;
				statusText.setFill(Color.web("#64B5F6"));
				statusText.setText("SHIELD BROKEN — bomb neutralized!");
			} else {
				playerLives[0]--;
				playerScore[0] = Math.max(0, playerScore[0] - 120);
				// SET IFRAMES (2s)
				pw.iFramesUntil = System.currentTimeMillis() + 2000;
				
				statusText.setFill(Color.web("#FF8DA6"));
				statusText.setText("HIT A BOMB — -1 LIFE!");
				if (playerLives[0] <= 0) {
					playerFinished[0] = true;
					statusText.setFill(Color.web("#FF6B6B"));
					statusText.setText("GAME OVER — Out of lives!");
					if (!rankingRecorded[0]) {
						recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], false, mode);
						rankingRecorded[0] = true;
					}
				}
			}
			bombTouchAnimStartMs[0] = System.currentTimeMillis();
			bombTouchGx[0] = nx;
			bombTouchGy[0] = ny;
			moveCommitAtMs[0] = bombTouchAnimStartMs[0] + MazeRenderer.BOMB_HIT_TOTAL_MS;
			moveCommitX[0] = nx;
			moveCommitY[0] = ny;

		// ── Item ─────────────────────────────────────────────────────────────
		} else if (cell == CellType.ITEM) {
			int reward = pw.doubleScore ? 360 : 180;
			playerScore[0] += reward;
			
			if (mode == PlayMode.PLAYER) {
				statusText.setFill(Color.web("#9FFFD8"));
				statusText.setText("ITEM COLLECTED (+" + reward + ") — opening mystery box...");
				mysteryPickupAnimStartMs[0] = System.currentTimeMillis();
				mysteryPickupGx[0] = nx;
				mysteryPickupGy[0] = ny;
				moveCommitAtMs[0] = mysteryPickupAnimStartMs[0] + MazeRenderer.MYSTERY_OPEN_TOTAL_MS;
				moveCommitX[0] = nx;
				moveCommitY[0] = ny;
				mysteryPickupNeedsModal[0] = inventory != null && gameScene != null && gameScene.length > 0 && gameScene[0] != null;
			} else {
				// AI ASSIST / BOT mode (Replay): Item vanishes immediately, no delay, no modal
				maze.setCell(nx, ny, CellType.EMPTY);
				statusText.setFill(Color.web("#9FFFD8"));
				statusText.setText("ITEM COLLECTED (+" + reward + ")");
			}
			openItemAfterMysteryHold[0] = () -> {
				if (inventory == null || gameScene == null || gameScene.length == 0 || gameScene[0] == null) return;
				boolean shown = ItemCardSelectionModal.showOnScene(gameScene[0], selectedPowerUp -> {
					if (selectedPowerUp != null) inventory.addCollectedPowerUp(selectedPowerUp);
					if (renderFrame != null) renderFrame.run();
				}, () -> {
					if (selectingPowerUp != null) selectingPowerUp[0] = false;
					if (pw.pendingChoices > 0) {
						pw.pendingChoices--;
						if (openItemAfterMysteryHold[0] != null) openItemAfterMysteryHold[0].run();
					} else {
						gameScene[0].getRoot().requestFocus();
					}
				});
				if (shown && selectingPowerUp != null) selectingPowerUp[0] = true;
				if (!shown) {
					statusText.setFill(Color.web("#FFD59A"));
					statusText.setText("ITEM COLLECTED — continue moving");
				}
			};

		// ── Goal ─────────────────────────────────────────────────────────────
		} else if (cell == CellType.GOAL) {
			playerFinished[0] = true;
			statusText.setFill(Color.web("#00FF9C"));
			statusText.setText("GOAL REACHED! Final score: " + Math.max(0, playerScore[0]));
			if (!rankingRecorded[0]) {
				recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], true, mode);
				rankingRecorded[0] = true;
			}
		} else {
			statusText.setFill(Color.web("#AEE8FF"));
			statusText.setText("MOVE TO GOAL");
		}

		if (cell != CellType.BOMB && cell != CellType.ITEM) {
			playerPos[0] = new State(nx, ny, Math.max(0, playerLives[0]));
			playerFromX[0] = nx; playerFromY[0] = ny;
			playerToX[0] = nx; playerToY[0] = ny;
			playerRenderX[0] = nx; playerRenderY[0] = ny;
		}
	}

	// ── Power-up activation notification overlay ─────────────────────────────
	/** Dark full-screen overlay shown for ~2 s when a power-up is activated. */
	private static void showActivationNotif(StackPane root, PowerUp powerUp) {
		final String ID = "powerup-activation-notif";
		if (root.lookup("#" + ID) != null) return; // don't stack multiples

		// ── Dark backdrop (same look as item-selection modal) ────────────────
		StackPane overlay = new StackPane();
		overlay.setId(ID);
		overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		overlay.setStyle("-fx-background-color: rgba(6,4,18,0.72);");
		overlay.setPickOnBounds(true);

		String diffHex = switch (powerUp.getDifficulty()) {
			case EASY   -> "#00C853";
			case MEDIUM -> "#F57C00";
			case HARD   -> "#C62828";
		};
		Color diffColor = Color.web(diffHex);
		String diffBgRgba = switch (powerUp.getDifficulty()) {
			case EASY   -> "rgba(0,200,83,0.12)";
			case MEDIUM -> "rgba(245,124,0,0.12)";
			case HARD   -> "rgba(198,40,40,0.12)";
		};

		// ── Card ─────────────────────────────────────────────────────────────
		VBox card = new VBox(0);
		card.setPrefWidth(450);
		card.setMaxWidth(450);
		card.setMaxHeight(Region.USE_PREF_SIZE);
		javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
		shadow.setColor(Color.color(0, 0, 0, 0.55));
		shadow.setRadius(26); shadow.setOffsetY(7);
		card.setEffect(shadow);

		// ── Colored header ────────────────────────────────────────────────────
		VBox header = new VBox(5);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(20, 18, 18, 18));
		header.setStyle("-fx-background-color: " + diffHex + ";" +
		                "-fx-background-radius: 16 16 0 0;");

		Text badge = new Text("⚡  POWER-UP ACTIVATED");
		badge.setFont(AppFonts.vt323(11));
		badge.setFill(Color.color(1, 1, 1, 0.70));

		Text nameText = new Text(powerUp.getDisplayName());
		nameText.setFont(AppFonts.vt323(24));
		nameText.setFill(Color.WHITE);
		nameText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
		nameText.setWrappingWidth(406);
		nameText.setEffect(new javafx.scene.effect.DropShadow(5, Color.color(0, 0, 0, 0.3)));
		header.getChildren().addAll(badge, nameText);

		// ── White body ────────────────────────────────────────────────────────
		VBox body = new VBox(10);
		body.setAlignment(Pos.CENTER);
		body.setPadding(new Insets(18, 24, 20, 24));
		body.setStyle("-fx-background-color: rgba(255,255,255,0.97);" +
		              "-fx-background-radius: 0 0 16 16;");

		Text descText = new Text(powerUp.getEnglishDescription());
		descText.setFont(AppFonts.vt323(14));
		descText.setFill(Color.web("#344456"));
		descText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
		descText.setWrappingWidth(398);

		// Effect tag (colored tint)
		HBox effectTag = new HBox(4);
		effectTag.setAlignment(Pos.CENTER);
		effectTag.setPadding(new Insets(7, 14, 7, 14));
		effectTag.setStyle("-fx-background-color: " + diffBgRgba + ";" +
		                   "-fx-background-radius: 8;");
		Text effectText = new Text("✦  " + powerUp.getVietnameseDescription());
		effectText.setFont(AppFonts.vt323(13));
		effectText.setFill(diffColor);
		effectText.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);
		effectText.setWrappingWidth(388);
		effectTag.getChildren().add(effectText);

		Text dismissHint = new Text("Click anywhere or wait to dismiss");
		dismissHint.setFont(AppFonts.vt323(11));
		dismissHint.setFill(Color.color(0.50, 0.55, 0.62, 0.58));

		body.getChildren().addAll(descText, effectTag, dismissHint);
		card.getChildren().addAll(header, body);
		overlay.getChildren().add(card);
		AppFonts.applyTo(overlay);
		root.getChildren().add(overlay);

		// ── Dismiss helper ────────────────────────────────────────────────────
		Runnable dismiss = () -> {
			FadeTransition ft = new FadeTransition(Duration.millis(320), overlay);
			ft.setFromValue(overlay.getOpacity());
			ft.setToValue(0);
			ft.setOnFinished(e -> {
				root.getChildren().remove(overlay);
				root.requestFocus();
			});
			ft.play();
		};
		overlay.setOnMouseClicked(e -> dismiss.run());

		// ── Fade in → auto-dismiss after 2.2 s ───────────────────────────────
		overlay.setOpacity(0);
		FadeTransition fadeIn = new FadeTransition(Duration.millis(220), overlay);
		fadeIn.setFromValue(0); fadeIn.setToValue(1);
		fadeIn.setOnFinished(ev ->
			new Timeline(new KeyFrame(Duration.millis(2200), e -> dismiss.run())).play()
		);
		fadeIn.play();
	}

	// ── Win / Lose / Timeout result overlay ───────────────────────────────────
	private static void showResultOverlay(
		StackPane root,
		Stage stage,
		Scene previousScene,
		String difficulty,
		boolean won,
		boolean timedOut,
		int finalScore,
		int steps,
		long elapsedMs,
		int livesLeft,
		Runnable onRestart,
		Runnable onMenu,
		Runnable onSeeBot
	) {
		if (root.lookup("#result-overlay") != null) return;

		// ── Dark backdrop (same style as skill modal) ────────────────────────
		StackPane overlay = new StackPane();
		overlay.setId("result-overlay");
		overlay.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
		overlay.setStyle("-fx-background-color: rgba(6,4,18,0.82);");
		overlay.setPickOnBounds(true);

		// ── Result card ──────────────────────────────────────────────────────
		VBox card = new VBox(0);
		card.setPrefWidth(540);
		card.setMaxWidth(540);
		card.setMaxHeight(Region.USE_PREF_SIZE);

		javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
		shadow.setColor(Color.color(0, 0, 0, 0.55));
		shadow.setRadius(30);
		shadow.setOffsetY(8);
		card.setEffect(shadow);

		// ── Close Button (X) ────────────────────────────────────────────────
		Button closeX = new Button("×");
		closeX.setStyle(
			"-fx-background-color: rgba(255,255,255,0.2);" +
			"-fx-text-fill: white;" +
			"-fx-font-size: 22;" +
			"-fx-font-weight: bold;" +
			"-fx-background-radius: 20;" +
			"-fx-padding: 0 8 2 8;" +
			"-fx-cursor: hand;"
		);
		closeX.setOnAction(e -> {
			root.getChildren().remove(overlay);
			root.requestFocus();
		});
		
		StackPane.setAlignment(closeX, Pos.TOP_RIGHT);
		StackPane.setMargin(closeX, new Insets(10));

		// ── Colored header ───────────────────────────────────────────────────
		String headerHex   = won ? "#2E7D32" : timedOut ? "#E65100" : "#C62828";
		// livesLeft >= 0  → player mode (real lives)
		// livesLeft < 0  → bot mode (explored-count passed negated, or -1 as sentinel)
		boolean isBotMode  = livesLeft < 0;
		String titleLabel  = won ? "🏆  CONGRATULATIONS!" : timedOut ? "⏰  TIME'S UP!"
		                         : isBotMode ? "🤖  NO PATH FOUND" : "💀  GAME OVER";
		String subLabel    = won
		    ? (isBotMode ? "The bot reached the goal!" : "You found the exit successfully!")
		    : timedOut ? "You ran out of time. Better luck next time!"
		    : isBotMode ? "No valid path exists. Try a different algorithm."
		    : "You lost all your lives. Try again!";

		VBox header = new VBox(6);
		header.setAlignment(Pos.CENTER);
		header.setPadding(new Insets(22, 20, 18, 20));
		header.setStyle("-fx-background-color: " + headerHex + "; -fx-background-radius: 16 16 0 0;");

		Text titleText = new Text(titleLabel);
		titleText.setFont(AppFonts.vt323(26));
		titleText.setFill(Color.WHITE);

		Text subText = new Text(subLabel);
		subText.setFont(AppFonts.vt323(13));
		subText.setFill(Color.color(1, 1, 1, 0.75));
		header.getChildren().addAll(titleText, subText);

		// ── Stats grid ───────────────────────────────────────────────────────
		VBox statsSection = new VBox(10);
		statsSection.setPadding(new Insets(18, 24, 14, 24));
		statsSection.setStyle("-fx-background-color: #F8FAFF;");

		long sec = (elapsedMs / 1000) % 60;
		long min = elapsedMs / 60000;
		String timeStr = String.format("%02d:%02d", min, sec);

		HBox statsGrid = new HBox(16);
		statsGrid.setAlignment(Pos.CENTER);
		statsGrid.getChildren().addAll(
			statBox("SCORE",    String.valueOf(Math.max(0, finalScore)),             "#1565C0"),
			statBox("TIME",     timeStr,                                             "#00695C"),
			statBox("STEPS",    String.valueOf(steps),                               "#E65100"),
			isBotMode
				? statBox("EXPLORED", String.valueOf(Math.abs(livesLeft) - 1),      "#6A1B9A")
				: statBox("LIVES",    String.valueOf(Math.max(0, livesLeft)),        "#6A1B9A")
		);
		statsSection.getChildren().add(statsGrid);

		// ── Top rankings for this difficulty ─────────────────────────────────
		VBox rankSection = new VBox(8);
		rankSection.setPadding(new Insets(10, 24, 14, 24));
		rankSection.setStyle("-fx-background-color: #F0F4FF;");

		Text rankTitle = new Text("TOP 5 — " + difficulty.toUpperCase());
		rankTitle.setFont(AppFonts.vt323(13));
		rankTitle.setFill(Color.web("#1F2D3A"));
		rankSection.getChildren().add(rankTitle);

		java.util.List<RankingEntry> entries = RankingManager.getInstance()
			.getRankingsByDifficulty(difficulty);
		int maxShow = Math.min(entries.size(), 5);
		for (int i = 0; i < maxShow; i++) {
			RankingEntry e = entries.get(i);
			String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  " + (i + 1) + ".";
			HBox row = new HBox(8);
			row.setAlignment(Pos.CENTER_LEFT);
			Text rankNum = new Text(medal + "  " + e.getPlayerName());
			rankNum.setFont(AppFonts.vt323(13));
			rankNum.setFill(Color.web("#263238"));
			javafx.scene.layout.Region sp = new Region();
			HBox.setHgrow(sp, Priority.ALWAYS);
			Text scoreVal = new Text(String.valueOf(e.getScore()));
			scoreVal.setFont(AppFonts.vt323(13));
			scoreVal.setFill(Color.web("#1565C0"));
			row.getChildren().addAll(rankNum, sp, scoreVal);
			rankSection.getChildren().add(row);
		}
		if (maxShow == 0) {
			Text noData = new Text("No score yet.");
			noData.setFont(AppFonts.vt323(12));
			noData.setFill(Color.web("#607D8B"));
			rankSection.getChildren().add(noData);
		}

		// ── Action buttons ───────────────────────────────────────────────────
		HBox actions = new HBox(14);
		actions.setAlignment(Pos.CENTER);
		actions.setPadding(new Insets(16, 20, 20, 20));
		actions.setStyle("-fx-background-color: #F8FAFF; -fx-background-radius: 0 0 16 16;");

		Button restartBtn = new NeonButton("▶  PLAY AGAIN", Color.web("#2F80ED"), 14, 10, 16, 8);
		restartBtn.setPrefWidth(180);
		Button menuBtn    = new NeonButton("⌂  MAIN MENU",  Color.web("#607D8B"), 14, 10, 16, 8);
		menuBtn.setPrefWidth(180);

		restartBtn.setOnAction(ev -> {
			root.getChildren().remove(overlay);
			onRestart.run();
		});

		if (won && onSeeBot != null) {
			Button seeBotBtn = new NeonButton("🤖 BOT PLAY", Color.web("#7E57C2"), 14, 10, 16, 8);
			seeBotBtn.setPrefWidth(180);
			seeBotBtn.setOnAction(ev -> {
				root.getChildren().remove(overlay);
				onSeeBot.run();
			});
			actions.getChildren().add(seeBotBtn);
		}

		menuBtn.setOnAction(ev -> {
			root.getChildren().remove(overlay);
			onMenu.run();
		});
		actions.getChildren().addAll(restartBtn, menuBtn);

		card.getChildren().addAll(header, statsSection, rankSection, actions);

		overlay.getChildren().addAll(card, closeX);
		AppFonts.applyTo(overlay);
		root.getChildren().add(overlay);

		overlay.setOpacity(0);
		FadeTransition ft = new FadeTransition(Duration.millis(320), overlay);
		ft.setFromValue(0);
		ft.setToValue(1);
		ft.play();
	}

	/** Small stat box used inside the result overlay. */
	private static VBox statBox(String label, String value, String colorHex) {
		VBox box = new VBox(4);
		box.setAlignment(Pos.CENTER);
		box.setPadding(new Insets(10, 18, 10, 18));
		box.setStyle(
			"-fx-background-color: rgba(0,0,0,0.04);" +
			"-fx-background-radius: 10;"
		);
		Text val = new Text(value);
		val.setFont(AppFonts.vt323(20));
		val.setFill(Color.web(colorHex));
		Text lbl = new Text(label);
		lbl.setFont(AppFonts.vt323(11));
		lbl.setFill(Color.web("#607D8B"));
		box.getChildren().addAll(val, lbl);
		return box;
	}

	/** Compact colored chip displayed in the skill-timer HUD bar. */
	private static HBox makeSkillChip(String label, String bgHex) {
		HBox chip = new HBox();
		chip.setAlignment(Pos.CENTER);
		chip.setPadding(new Insets(4, 18, 4, 18));
		chip.setStyle("-fx-background-color: " + bgHex + "; -fx-background-radius: 12;");
		Text t = new Text(label);
		t.setFont(AppFonts.vt323(12));
		t.setFill(Color.WHITE);
		chip.getChildren().add(t);
		return chip;
	}

	private static int safeSize(java.util.List<?> list) {
		return list == null ? 0 : list.size();
	}

	private static double smoothStep(double t) {
		double clamped = Math.max(0.0, Math.min(1.0, t));
		return clamped * clamped * (3.0 - 2.0 * clamped);
	}

	private static boolean isMovementKey(KeyCode code) {
		return code == KeyCode.UP || code == KeyCode.DOWN || code == KeyCode.LEFT || code == KeyCode.RIGHT;
	}

	private static SearchAlgorithm resolveAlgorithm(String algorithmName) {
		if (algorithmName == null) {
			return new BFS();
		}
		return switch (algorithmName.toUpperCase()) {
			case "DFS" -> new DFS();
			case "A*", "ASTAR", "A-STAR" -> new AStar();
			default -> new BFS();
		};
	}

	private static Pane createBackground() {
		javafx.geometry.Rectangle2D sb = javafx.stage.Screen.getPrimary().getVisualBounds();
		double bgW = sb.getWidth();
		double bgH = sb.getHeight();

		Pane pane = new Pane();
		pane.setPrefSize(bgW, bgH);
		Canvas bg = new Canvas(bgW, bgH);
		GraphicsContext gc = bg.getGraphicsContext2D();

		Image landTexture = loadPlayImage("/image/pixel_MainMenu/Grass_mainmenu.png");
		if (landTexture != null && !landTexture.isError()) {
			double tile = 50;
			for (double x = 0; x < bgW; x += tile) {
				for (double y = 0; y < bgH; y += tile) {
					gc.drawImage(landTexture, x, y, tile, tile);
				}
			}
		} else {
			for (int y = 0; y < (int) bgH; y++) {
				double ratio = y / bgH;
				int r = (int) (248 + (236 - 248) * ratio);
				int g = (int) (209 + (198 - 209) * ratio);
				int b = (int) (142 + (130 - 142) * ratio);
				gc.setStroke(Color.rgb(r, g, b));
				gc.strokeLine(0, y, bgW, y);
			}

			gc.setFill(Color.color(0.92, 0.72, 0.45, 0.35));
			for (int x = 0; x < (int) bgW; x += 18) {
				for (int y = 0; y < (int) bgH; y += 18) {
					if (((x + y) / 18) % 3 == 0) {
						gc.fillOval(x + 2, y + 2, 3, 3);
					}
				}
			}
		}

		pane.getChildren().add(bg);
		return pane;
	}

	private static Image loadPlayImage(String resourcePath) {
		try {
			java.io.InputStream stream = PlayGamePage.class.getResourceAsStream(resourcePath);
			if (stream == null) {
				return null;
			}
			return new Image(stream);
		} catch (Exception ex) {
			return null;
		}
	}

	private static VBox createLegendPanel() {
		VBox panel = new VBox(10);
		panel.setPrefWidth(360);
		panel.setPadding(new Insets(14, 14, 14, 14));
		panel.setAlignment(Pos.TOP_LEFT);
		panel.setStyle(
			"-fx-background-color: rgba(255,255,255,0.95);" +
			"-fx-border-color: rgba(0,0,0,0.08);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		Text title = new Text("Legend");
		title.setFont(AppFonts.vt323(24));
		title.setFill(Color.web("#1F2D3A"));

		Text hint = new Text("Map keys");
		hint.setFont(AppFonts.vt323(12));
		hint.setFill(Color.web("#4F5B62"));

		GridPane rows = new GridPane();
		rows.setHgap(18);
		rows.setVgap(8);
		javafx.scene.Node[] legendNodes = new javafx.scene.Node[] {
			legendRow("/image/vit/Duck.png", "R", Color.web("#FFE082"), "Player"),
			legendRow("/image/vit/FinishLine.png", "F", Color.web("#212121"), "GOAL"),
			legendRow("/image/vit/Grass.png", "W", Color.web("#A3D977"), "Wall"),
			legendRow("/image/vit/Water.png", "~", Color.web("#8AE5F6"), "Path"),
			legendRow("/image/vit/Flag.png", ".", Color.web("#AFC7FF"), "Explored"),
			legendRow("/image/vit/Vit.png", "+", Color.web("#A5D6A7"), "Item"),
			legendRow("/image/vit/Stop.png", "X", Color.web("#FF6B6B"), "Hazard")
		};
		for (int i = 0; i < legendNodes.length; i++) {
			rows.add(legendNodes[i], i % 2, i / 2);
		}

		Text foot = new Text("Use OPTIONS for hints.");
		foot.setFont(AppFonts.vt323(11));
		foot.setFill(Color.web("#7B8A93"));
		foot.setWrappingWidth(330);

		panel.getChildren().addAll(title, hint, rows, foot);
		return panel;
	}

	private static HBox legendRow(String imagePath, String fallbackSymbol, Color fallbackColor, String meaning) {
		javafx.scene.Node iconNode = createLegendIcon(imagePath, fallbackSymbol, fallbackColor);

		Text text = new Text(meaning);
		text.setFont(AppFonts.vt323(13));
		text.setFill(Color.web("#22303A"));

		HBox row = new HBox(10, iconNode, text);
		row.setAlignment(Pos.CENTER_LEFT);
		return row;
	}

	private static javafx.scene.Node createLegendIcon(String imagePath, String fallbackSymbol, Color fallbackColor) {
		Image image = loadPlayImage(imagePath);
		if (image != null && !image.isError()) {
			ImageView icon = new ImageView(image);
			icon.setFitWidth(22);
			icon.setFitHeight(22);
			icon.setPreserveRatio(true);
			return icon;
		}

		Text fallback = new Text(fallbackSymbol);
		fallback.setFont(AppFonts.vt323(18));
		fallback.setFill(fallbackColor);
		return fallback;
	}

	private static void showInGameOptions(
		StackPane root,
		double[] masterVolume,
		double[] musicVolume,
		double[] sfxVolume,
		boolean[] pathHint,
		boolean[] aiSuggest,
		boolean[] highContrast,
		boolean[] reducedMotion,
		Runnable onResume,
		Runnable onExitToMenu,
		Runnable onAudioChanged
	) {
		if (root.lookup("#in-game-options-overlay") != null) {
			return;
		}

		StackPane overlay = new StackPane();
		overlay.setId("in-game-options-overlay");
		overlay.setStyle("-fx-background-color: rgba(35,30,20,0.42);");
		overlay.setPickOnBounds(true);

		VBox panel = new VBox(14);
		panel.setAlignment(Pos.TOP_LEFT);
		panel.setPadding(new Insets(22));
		panel.setPrefWidth(720);
		panel.setPrefHeight(520);
		panel.setMaxWidth(720);
		panel.setMaxHeight(520);
		panel.setStyle(
			"-fx-background-color: rgba(30, 41, 59, 0.95);" +
			"-fx-border-color: rgba(255,255,255,0.20);" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;"
		);

		Text title = new Text("OPTIONS");
		title.setFont(AppFonts.vt323(48));
		title.setFill(Color.WHITE);

		Text subtitle = new Text("Audio + controls");
		subtitle.setFont(AppFonts.vt323(14));
		subtitle.setFill(Color.web("#B0BEC5"));

		Slider master = createOptionsSlider(masterVolume[0]);
		Slider music = createOptionsSlider(musicVolume[0]);
		Slider sfx = createOptionsSlider(sfxVolume[0]);

		VBox masterRow = createOptionsSliderRow("Master", master);
		VBox musicRow = createOptionsSliderRow("Music", music);
		VBox sfxRow = createOptionsSliderRow("SFX", sfx);

		master.valueProperty().addListener((obs, ov, nv) -> {
			masterVolume[0] = nv.doubleValue();
			if (onAudioChanged != null) {
				onAudioChanged.run();
			}
		});
		music.valueProperty().addListener((obs, ov, nv) -> {
			musicVolume[0] = nv.doubleValue();
			if (onAudioChanged != null) {
				onAudioChanged.run();
			}
		});
		sfx.valueProperty().addListener((obs, ov, nv) -> {
			sfxVolume[0] = nv.doubleValue();
			if (onAudioChanged != null) {
				onAudioChanged.run();
			}
		});

		CheckBox pathHintToggle = createOptionsToggle("Show path hint", pathHint[0]);
		CheckBox aiToggle = createOptionsToggle("Enable AI suggestion", aiSuggest[0]);
		CheckBox contrastToggle = createOptionsToggle("High contrast labels", highContrast[0]);
		CheckBox motionToggle = createOptionsToggle("Reduced motion", reducedMotion[0]);

		pathHintToggle.selectedProperty().addListener((obs, ov, nv) -> pathHint[0] = nv);
		aiToggle.selectedProperty().addListener((obs, ov, nv) -> aiSuggest[0] = nv);
		contrastToggle.selectedProperty().addListener((obs, ov, nv) -> highContrast[0] = nv);
		motionToggle.selectedProperty().addListener((obs, ov, nv) -> reducedMotion[0] = nv);

		Label gameplayLabel = new Label("Gameplay / Accessibility");
		gameplayLabel.setTextFill(Color.web("#B0BEC5"));
		gameplayLabel.setFont(AppFonts.vt323(13));

		HBox actions = new HBox(10);
		actions.setAlignment(Pos.CENTER_RIGHT);
		Button resumeBtn = new NeonButton("RESUME", Color.web("#2E7D32"), 14, 7, 14, 6);
		Button exitBtn = new NeonButton("EXIT TO MENU", Color.web("#D84343"), 14, 7, 14, 6);

		Runnable closeOverlay = () -> {
			root.getChildren().remove(overlay);
			root.requestFocus();
		};

		resumeBtn.setOnAction(e -> {
			closeOverlay.run();
			if (onResume != null) {
				onResume.run();
			}
		});

		exitBtn.setOnAction(e -> {
			closeOverlay.run();
			if (onExitToMenu != null) {
				onExitToMenu.run();
			}
		});

		actions.getChildren().addAll(resumeBtn, exitBtn);
		panel.getChildren().addAll(
			title,
			subtitle,
			masterRow,
			musicRow,
			sfxRow,
			gameplayLabel,
			pathHintToggle,
			aiToggle,
			contrastToggle,
			motionToggle,
			actions
		);

		overlay.getChildren().add(panel);
		AppFonts.applyTo(overlay);
		overlay.setOnKeyPressed(e -> {
			if (e.getCode() == KeyCode.ESCAPE) {
				closeOverlay.run();
				if (onResume != null) {
					onResume.run();
				}
				e.consume();
			} else {
				e.consume();
			}
		});

		root.getChildren().add(overlay);
		overlay.requestFocus();
	}

	private static Slider createOptionsSlider(double initial) {
		Slider slider = new Slider(0, 100, initial);
		slider.setShowTickLabels(false);
		slider.setShowTickMarks(false);
		slider.setStyle(
			"-fx-control-inner-background: #F3E3C7;" +
			"-fx-accent: #2F80ED;"
		);
		return slider;
	}

	private static VBox createOptionsSliderRow(String label, Slider slider) {
		VBox row = new VBox(6);
		Label title = new Label(label + "  " + String.format(java.util.Locale.US, "%.0f%%", slider.getValue()));
		title.setTextFill(Color.web("#455A64"));
		title.setFont(AppFonts.vt323(14));
		slider.valueProperty().addListener((obs, ov, nv) -> {
			title.setText(label + "  " + String.format(java.util.Locale.US, "%.0f%%", nv.doubleValue()));
		});
		row.getChildren().addAll(title, slider);
		return row;
	}

	private static CheckBox createOptionsToggle(String text, boolean selected) {
		CheckBox box = new CheckBox(text);
		box.setSelected(selected);
		box.setTextFill(Color.web("#455A64"));
		box.setFont(AppFonts.vt323(14));
		box.setStyle(
			"-fx-mark-color: #2F80ED;" +
			"-fx-focus-color: transparent;" +
			"-fx-faint-focus-color: transparent;"
		);
		return box;
	}

	// ── Power-up runtime state ────────────────────────────────────────────────

	static final class PowerUpState {
		// 1-use protective flags
		boolean shield      = false;  long shieldUntil     = 0L;
		boolean wallRemoval = false;  // destroy next wall hit

		// Timed: score doubler
		boolean doubleScore = false;  long dblScoreUntil  = 0L;

		// Timed: visual overlays
		boolean revealPath  = false;  long revealPathUntil = 0L;
		boolean bombDetect  = false;  long detectUntil     = 0L;
		boolean visionBoost = false;  long visionUntil     = 0L;
		boolean freezeTime  = false;  long freezeUntil     = 0L;
		long lifeVfxUntil = 0L; // heart animation
		long scoreVfxUntil  = 0L; // +score animation
		int  scoreVfxValue  = 0;
		long timeVfxUntil   = 0L; // +15s animation

		// Magnetic pull effects
		java.util.List<FlyingItem> flyingItems = new java.util.ArrayList<>();

		// Speed modifiers
		long speedBoostUntil = 0L;
		long speedSlowUntil  = 0L;
		long effectiveStepNs = PLAYER_STEP_NANOS;

		// AI assist – follows BFS path for a few steps
		boolean             aiRunning = false;
		java.util.List<com.nhom_01.robot_pathfinding.core.State> aiPath = null;
		int                 aiPathIdx = 0;
		int                 pendingChoices = 0;
		long                iFramesUntil = 0L;

		// --- PERFORMANCE CACHE ---
		java.util.List<com.nhom_01.robot_pathfinding.core.State> cachedPathToGoal = null;
		int lastPathSourceX = -1, lastPathSourceY = -1;
		boolean lastPathFrozen = false;
		java.util.List<com.nhom_01.robot_pathfinding.core.State> cachedBombs = null;

		/** Call every frame to expire timed effects. */
		void tickExpiry() {
			long now = System.currentTimeMillis();
			if (doubleScore && now > dblScoreUntil)   doubleScore = false;
			if (shield      && now > shieldUntil)     shield      = false;
			if (revealPath  && now > revealPathUntil) revealPath  = false;
			if (bombDetect  && now > detectUntil)     bombDetect  = false;
			if (freezeTime  && now > freezeUntil)     freezeTime  = false;
			if (visionBoost && now > visionUntil)     visionBoost = false;

			// Dynamic Speed calculation
			boolean boostActive = now < speedBoostUntil;
			boolean slowActive  = now < speedSlowUntil;

			if (boostActive && slowActive) {
				effectiveStepNs = PLAYER_STEP_NANOS; // Cancel out to normal
			} else if (boostActive) {
				effectiveStepNs = PLAYER_STEP_NANOS / 2; // Fast
			} else if (slowActive) {
				effectiveStepNs = PLAYER_STEP_NANOS * 2; // Slow
			} else {
				effectiveStepNs = PLAYER_STEP_NANOS; // Default
			}
		}

		public void pauseTimers(long deltaMs) {
			if (deltaMs <= 0) return;
			if (dblScoreUntil > 0)   dblScoreUntil   += deltaMs;
			if (shieldUntil > 0)     shieldUntil     += deltaMs;
			if (revealPathUntil > 0) revealPathUntil += deltaMs;
			if (detectUntil > 0)     detectUntil     += deltaMs;
			if (freezeUntil > 0)     freezeUntil     += deltaMs;
			if (visionUntil > 0)     visionUntil     += deltaMs;
			if (speedBoostUntil > 0) speedBoostUntil += deltaMs;
			if (speedSlowUntil > 0)  speedSlowUntil  += deltaMs;
			if (lifeVfxUntil > 0)   lifeVfxUntil    += deltaMs;
		}

		boolean isScoreDoubled() {
			return doubleScore && System.currentTimeMillis() < dblScoreUntil;
		}
		boolean isRevealingPath() {
			return revealPath && System.currentTimeMillis() < revealPathUntil;
		}
		boolean isDetectingBombs() {
			return bombDetect && System.currentTimeMillis() < detectUntil;
		}
		boolean isFrozen() {
			return freezeTime && System.currentTimeMillis() < freezeUntil;
		}
		boolean isVisionBoostActive() {
			return visionBoost && System.currentTimeMillis() < visionUntil;
		}
		boolean isSpeedBoostActive() {
			return System.currentTimeMillis() < speedBoostUntil;
		}
		boolean isSpeedSlowActive() {
			return System.currentTimeMillis() < speedSlowUntil;
		}
	}

	private static void handlePowerUpActivation(
			PowerUp type, PowerUpState pw, int[] playerScore, int[] playerLives, State[] playerPos,
			Text stateLabel, Text scoreLabel, Text pathLabel, Text exploredLabel, Text statusText,
			long[] countdownEndMs, Maze maze, double[] playerRenderX, double[] playerRenderY,
			double[] playerFromX, double[] playerFromY, double[] playerToX, double[] playerToY,
			Text currentPosText, long[] playerAccumulatorNanos, InventoryPanel inventory,
			StackPane root, Scene[] gameScene, boolean[] selectingPowerUp,
			long[] teleportAnimStartMs, int[] teleportGx, int[] teleportGy, State[] pendingTeleportPos) {

		long NOW = System.currentTimeMillis();
		switch (type) {
			case EXTRA_LIFE -> {
				playerLives[0] = Math.min(playerLives[0] + 1, 9);
				pw.lifeVfxUntil = NOW + 1000;
				statusText.setFill(Color.web("#00FF9C"));
				statusText.setText("EXTRA LIFE! Lives remaining: " + playerLives[0]);
				refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
			}
			case SHIELD -> {
				pw.shield = true;
				pw.shieldUntil = Math.max(NOW, pw.shieldUntil) + 10_000;
				statusText.setFill(Color.web("#64B5F6"));
				statusText.setText("SHIELD active — protected from bombs!");
			}
			case FREEZE_TIME -> {
				pw.freezeTime = true; 
				pw.freezeUntil = Math.max(NOW, pw.freezeUntil) + 6_000;
				statusText.setFill(Color.web("#B3E5FC"));
				statusText.setText("BOMBS FROZEN — path blocked!");
			}
			case DOUBLE_SCORE -> {
				pw.scoreVfxValue = playerScore[0];
				pw.scoreVfxUntil = NOW + 1000;
				playerScore[0] *= 2;
				statusText.setFill(Color.web("#FFD54F"));
				statusText.setText("DOUBLE SCORE! Current score doubled!");
				refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
			}
			case REVEAL_PATH, SHORTEST_PATH_MODE -> {
				pw.revealPath = true; 
				pw.revealPathUntil = Math.max(NOW, pw.revealPathUntil) + 20_000;
				statusText.setFill(Color.web("#A5D6A7"));
				statusText.setText("PATH REVEALED!");
			}
			case BOMB_DETECTOR -> {
				pw.bombDetect = true; 
				pw.detectUntil = Math.max(NOW, pw.detectUntil) + 15_000;
				statusText.setFill(Color.web("#EF6C00"));
				statusText.setText("BOMB DETECTOR active!");
			}
			case SPEED_BOOST -> {
				pw.speedBoostUntil = Math.max(NOW, pw.speedBoostUntil) + 8_000;
				pw.tickExpiry(); // Update effectiveStepNs immediately
				playerAccumulatorNanos[0] = pw.effectiveStepNs;
				statusText.setFill(Color.web("#80DEEA"));
				statusText.setText("SPEED BOOST (2-Cells)!");
			}
			case SPEED_SLOW -> {
				pw.speedSlowUntil = Math.max(NOW, pw.speedSlowUntil) + 8_000;
				pw.tickExpiry(); // Update effectiveStepNs immediately
				playerAccumulatorNanos[0] = pw.effectiveStepNs;
				statusText.setFill(Color.web("#BCAAA4"));
				statusText.setText("SLOW MODE — safer movement!");
			}
			case TELEPORT -> {
				State newPos = findRandomSafeCell(maze, playerPos[0]);
				if (newPos != null) {
					// START animation first
					teleportAnimStartMs[0] = System.currentTimeMillis();
					teleportGx[0] = playerPos[0].getX();
					teleportGy[0] = playerPos[0].getY();
					pendingTeleportPos[0] = newPos;

					statusText.setFill(Color.web("#CE93D8"));
					statusText.setText("TELEPORTING...");
				}
			}
			case REMOVE_WALL -> {
				pw.wallRemoval = true;
				statusText.setFill(Color.web("#FFAB91"));
				statusText.setText("WALL REMOVAL ready — walk into a wall to break it!");
			}
			case TIME_BONUS -> {
				countdownEndMs[0] += 15_000;
				pw.timeVfxUntil = NOW + 1000;
				statusText.setFill(Color.web("#FFD54F"));
				statusText.setText("+15 SECONDS ADDED TO CLOCK!");
			}
			case LUCKY_FIND -> {
				// ADD a random beneficial power-up to inventory instead of triggering immediately
				PowerUp[] choices = {PowerUp.SHIELD, PowerUp.SPEED_BOOST, PowerUp.FREEZE_TIME, PowerUp.DOUBLE_SCORE, PowerUp.TELEPORT};
				PowerUp randomType = choices[new java.util.Random().nextInt(choices.length)];
				statusText.setFill(Color.web("#FFD54F"));
				statusText.setText("LUCKY FIND! Received a random " + randomType.getDisplayName() + "!");
				
				if (inventory != null) {
					inventory.addCollectedPowerUp(randomType);
				}
			}
			case AI_ASSIST -> {
				java.util.List<State> aiPath = computePathToGoal(maze, playerPos[0], pw);
				if (aiPath != null && aiPath.size() > 1) {
					pw.aiRunning = true;
					pw.aiPath = aiPath;
					pw.aiPathIdx = 1; 
					statusText.setFill(Color.web("#B388FF"));
					statusText.setText("AI ASSIST active — automatic movement for 8 steps!");
				}
			}
			case VISION_BOOST -> {
				pw.visionBoost = true; pw.visionUntil = Math.max(NOW, pw.visionUntil) + 15_000;
				pw.revealPath  = true; pw.revealPathUntil = Math.max(NOW, pw.revealPathUntil) + 15_000;
				pw.doubleScore = true; pw.dblScoreUntil = Math.max(NOW, pw.dblScoreUntil) + 15_000;
				statusText.setFill(Color.web("#80DEEA"));
				statusText.setText("SMART SONAR active!");
			}
			case ANOTHER_OPTIONS -> {
				if (gameScene != null && gameScene.length > 0 && gameScene[0] != null) {
					boolean shown = ItemCardSelectionModal.showOnScene(gameScene[0], extra -> {
						if (extra != null) inventory.addCollectedPowerUp(extra);
					}, () -> {
						selectingPowerUp[0] = false;
						root.requestFocus();
					});
					if (shown) {
						selectingPowerUp[0] = true;
						statusText.setFill(Color.web("#CE93D8"));
						statusText.setText("ANOTHER OPTIONS — pick a bonus item!");
					}
				}
			}
		}
		// Always return focus to the main container so arrow keys work immediately
		root.requestFocus();
	}

	/** BFS path from current player position to maze goal (used by REVEAL_PATH, AI_ASSIST). */
	private static java.util.List<com.nhom_01.robot_pathfinding.core.State> computePathToGoal(
			Maze maze,
			com.nhom_01.robot_pathfinding.core.State from,
			PowerUpState pw) {
		try {
			Maze targetMaze = maze;
			if (pw != null && pw.isFrozen()) {
				// Redesign: Treat bombs as walls if frozen
				targetMaze = maze.copy();
				for (int x = 0; x < targetMaze.getWidth(); x++) {
					for (int y = 0; y < targetMaze.getHeight(); y++) {
						if (targetMaze.getCell(x, y) == CellType.BOMB) {
							targetMaze.setCell(x, y, CellType.WALL);
						}
					}
				}
			}
			SearchResult result = new com.nhom_01.robot_pathfinding.ai.BFS().findPath(targetMaze, from, maze.getGoal());
			return (result != null) ? result.getPath() : null;
		} catch (Exception e) {
			return null;
		}
	}

	/** All bomb cell positions — used by BOMB_DETECTOR overlay. */
	private static java.util.List<com.nhom_01.robot_pathfinding.core.State> bombPositions(Maze maze) {
		java.util.List<com.nhom_01.robot_pathfinding.core.State> bombs = new java.util.ArrayList<>();
		for (int x = 0; x < maze.getWidth(); x++)
			for (int y = 0; y < maze.getHeight(); y++)
				if (maze.getCell(x, y) == CellType.BOMB)
					bombs.add(new com.nhom_01.robot_pathfinding.core.State(x, y, 0));
		return bombs;
	}

	/** Random safe (non-wall, non-bomb) cell for TELEPORT. */
	private static com.nhom_01.robot_pathfinding.core.State findRandomSafeCell(
			Maze maze,
			com.nhom_01.robot_pathfinding.core.State exclude) {
		java.util.List<int[]> candidates = new java.util.ArrayList<>();
		for (int x = 1; x < maze.getWidth() - 1; x++)
			for (int y = 1; y < maze.getHeight() - 1; y++) {
				CellType c = maze.getCell(x, y);
				if (c == CellType.EMPTY
						&& !(x == exclude.getX() && y == exclude.getY()))
					candidates.add(new int[]{x, y});
			}
		if (candidates.isEmpty()) return null;
		int[] chosen = candidates.get(new java.util.Random().nextInt(candidates.size()));
		return new com.nhom_01.robot_pathfinding.core.State(chosen[0], chosen[1], exclude.getLives());
	}

	private static final class InGameAudio {
		private final AudioClip footstep;

		private InGameAudio(AudioClip footstep) {
			this.footstep = footstep;
		}

		static InGameAudio create() {
			AudioClip footstep = loadClip("/audio/Swimming.mp3");
			return new InGameAudio(footstep);
		}

		void startTheme(double master, double music) {
			// Intentionally empty: gameplay should not play theme music.
		}

		void updateVolumes(double master, double music, double sfx) {
			setClipVolume(footstep, master, sfx);
		}

		void playFootstep(double master, double sfx) {
			playClip(footstep, master, sfx);
		}

		void stopAll() {
			if (footstep != null) {
				footstep.stop();
			}
		}

		private void playClip(AudioClip clip, double master, double sfx) {
			if (clip == null) {
				return;
			}
			clip.setVolume(normalized(master, sfx));
			clip.play();
		}

		private void setClipVolume(AudioClip clip, double master, double sfx) {
			if (clip == null) {
				return;
			}
			clip.setVolume(normalized(master, sfx));
		}

		private double normalized(double master, double channel) {
			double value = (master / 100.0) * (channel / 100.0);
			return Math.max(0.0, Math.min(1.0, value));
		}

		private static AudioClip loadClip(String resourcePath) {
			try {
				java.net.URL url = PlayGamePage.class.getResource(resourcePath);
				if (url == null) {
					return null;
				}
				return new AudioClip(url.toExternalForm());
			} catch (Exception ex) {
				return null;
			}
		}

	}

	private static void recordGameRanking(
		String difficulty,
		int steps,
		long gameStartTimeMs,
		String algorithmName,
		int finalScore,
		boolean won,
		PlayMode mode
	) {
		try {
			long currentTimeMs = System.currentTimeMillis();
			long elapsedTimeMs = currentTimeMs - gameStartTimeMs;
			
			// Calculate final score based on performance
			int calculatedScore = calculateFinalScore(finalScore, steps, elapsedTimeMs, won);
			
			// Decide which name to display on ranking (if Bot, use algorithm name, else player profile)
			String nameToRecord = (mode == PlayMode.BOT && algorithmName != null) 
				? algorithmName 
				: PlayerProfile.getCurrentPlayerName();

			RankingEntry entry = new RankingEntry(
				nameToRecord,
				difficulty,
				steps,
				elapsedTimeMs,
				algorithmName != null ? algorithmName : "MANUAL",
				calculatedScore
			);
			
			RankingManager.getInstance().addRanking(entry);
			System.out.println("Ranking recorded: " + entry);
		} catch (Exception e) {
			System.err.println("Error recording ranking: " + e.getMessage());
			e.printStackTrace();
		}
	}

	private static int calculateFinalScore(int baseScore, int steps, long timeMs, boolean won) {
		int score = Math.max(0, baseScore);
		
		if (won) {
			// Bonus for winning
			score += 500;
			// Time bonus: 1 point per 100ms (capped at 1000)
			score += Math.min(1000, (int) (timeMs / 100));
		} else {
			// Penalty for losing
			score = Math.max(0, score - 300);
		}
		
		// Step penalty: 1 point per 10 steps
		score = Math.max(0, score - (steps / 10));
		
		return Math.max(0, score);
	}

	private static boolean calculateShieldVisible(PowerUpState pw) {
		if (!pw.shield) return false;
		long remaining = pw.shieldUntil - System.currentTimeMillis();
		if (remaining > 2000) return true;
		if (remaining <= 0) return false;
		return (System.currentTimeMillis() / 200) % 2 == 0;
	}
}
