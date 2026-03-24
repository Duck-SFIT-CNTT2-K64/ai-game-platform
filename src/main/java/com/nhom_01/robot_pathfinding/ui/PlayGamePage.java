package com.nhom_01.robot_pathfinding.ui;

import com.nhom_01.robot_pathfinding.ai.AStar;
import com.nhom_01.robot_pathfinding.ai.BFS;
import com.nhom_01.robot_pathfinding.ai.DFS;
import com.nhom_01.robot_pathfinding.ai.SearchAlgorithm;
import com.nhom_01.robot_pathfinding.core.*;
import com.nhom_01.robot_pathfinding.game.GameEngine;
import com.nhom_01.robot_pathfinding.game.GameState;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.InventoryPanel;
import com.nhom_01.robot_pathfinding.ui.components.ItemCardSelectionModal;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import javafx.animation.AnimationTimer;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.AudioClip;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

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
		stage.setScene(buildScene(stage, previousScene, difficulty, algorithmName, PlayMode.BOT));
	}

	public static void showPlayerOnStage(Stage stage, Scene previousScene, String difficulty) {
		stage.setScene(buildScene(stage, previousScene, difficulty, null, PlayMode.PLAYER));
	}

	private static Scene buildScene(Stage stage, Scene previousScene, String difficulty, String algorithmName, PlayMode mode) {
		MenuAudioManager.stopTheme();
		javafx.geometry.Rectangle2D screenBounds = javafx.stage.Screen.getPrimary().getVisualBounds();
		double W = screenBounds.getWidth();
		double H = screenBounds.getHeight();
		GameSettings settings = GameSettings.getInstance();

		// Generate maze with mode-aware configuration (BOT mode excludes items/bombs)
		MazeGenerator.GameMode generatorMode = mode == PlayMode.BOT 
			? MazeGenerator.GameMode.BOT 
			: MazeGenerator.GameMode.PLAYER;
		Maze maze = MazeGenerator.generate(difficulty, generatorMode);

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
		boolean[] optionsOpen = new boolean[] { false };
		// Currently held movement key — drives continuous motion like bot
		KeyCode[] playerHeldKey = new KeyCode[] { null };
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
		int[] stepCounter = new int[] { 0 };
		boolean[] rankingRecorded = new boolean[] { false };
		
		// Inventory for player mode
		InventoryPanel inventory = mode == PlayMode.PLAYER ? new InventoryPanel() : null;
		// Power-up runtime state (only meaningful in PLAYER mode)
		PowerUpState pw = new PowerUpState();
		Scene[] gameScene = new Scene[1];
		double[] botRenderX = new double[] { maze.getStart().getX() };
		double[] botRenderY = new double[] { maze.getStart().getY() };
		double[] botFromX = new double[] { maze.getStart().getX() };
		double[] botFromY = new double[] { maze.getStart().getY() };
		double[] botToX = new double[] { maze.getStart().getX() };
		double[] botToY = new double[] { maze.getStart().getY() };
		long[] botAccumulatorNanos = new long[] { 0L };
		long[] botLastFrameNanos = new long[] { 0L };
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
			mode == PlayMode.BOT ? "PATH: " + safeSize(engine.getPath()) : "LIVES: " + playerLives[0],
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

		// Inventory panel for PLAYER mode
		VBox inventorySection = new VBox(8);
		inventorySection.setAlignment(Pos.CENTER_LEFT);
		inventorySection.setPrefHeight(74);
		inventorySection.setStyle(
			"-fx-background-color: rgba(255,255,255,0.90);" +
			"-fx-border-color: rgba(0,0,0,0.12);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;" +
			"-fx-padding: 8 10 8 10;"
		);
		if (mode == PlayMode.PLAYER) {
			Text inventoryLabel = new Text("Vat pham da nhat");
			inventoryLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));
			inventoryLabel.setFill(Color.web("#263238"));
			inventorySection.getChildren().addAll(inventoryLabel, inventory.getContainer());
		} else {
			inventorySection.setVisible(false);
			inventorySection.setManaged(false);
		}

		Text statusText = new Text();
		statusText.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		statusText.setFill(Color.web("#34495E"));
		statusText.setWrappingWidth(312);

		Text currentPosText = new Text("Vi tri hien tai: -");
		currentPosText.setFont(Font.font("Arial", FontWeight.BOLD, 15));
		currentPosText.setFill(Color.web("#22303A"));

		HBox actions = new HBox(12);
		actions.setAlignment(Pos.CENTER_RIGHT);

		Button replay = new NeonButton("AP DUNG", Color.web("#4E54E8"), 14, 8, 14, 8);
		Button optionsButton = new NeonButton("GOI Y", Color.web("#2F80ED"), 14, 8, 14, 8);
		Button backMenu = new NeonButton("BACK MENU", Color.web("#BBBBBB"), 14, 8, 14, 8);

		Text settingsTitle = new Text("Cai dat");
		settingsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 31));
		settingsTitle.setFill(Color.web("#1F2D3A"));

		Text subtitle = new Text(mode == PlayMode.BOT
			? "BOT | " + difficulty + " | " + algorithmName
			: "PLAYER | " + difficulty);
		subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
		subtitle.setFill(Color.web("#4F5B62"));

		Slider delaySlider = new Slider(120, 520, 220);
		delaySlider.setShowTickLabels(false);
		delaySlider.setShowTickMarks(false);
		delaySlider.setStyle("-fx-accent: #2f80ed;");
		Text delayText = new Text("Do tre: 220ms");
		delayText.setFont(Font.font("Arial", FontWeight.BOLD, 13));
		delayText.setFill(Color.web("#263238"));
		delaySlider.valueProperty().addListener((obs, ov, nv) -> {
			delayText.setText("Do tre: " + Math.round(nv.doubleValue()) + "ms");
			botAccumulatorNanos[0] = 0L;
		});

		Text stateLabel = createStatText("STATE: " + initialState, Color.web("#1976D2"));
		stateLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		Text scoreLabel = createStatText(
			"SCORE: " + (mode == PlayMode.BOT ? engine.getScore() : playerScore[0]),
			Color.web("#00897B")
		);
		scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		Text pathLabel = createStatText(
			mode == PlayMode.BOT ? "PATH: " + safeSize(engine.getPath()) : "LIVES: " + playerLives[0],
			Color.web("#EF6C00")
		);
		pathLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		Text exploredLabel = createStatText(
			mode == PlayMode.BOT ? "EXPLORED: " + safeSize(engine.getExplored()) : "GOAL: FIND EXIT",
			Color.web("#455A64")
		);
		exploredLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

		VBox settingsCard = new VBox(10,
			settingsTitle,
			subtitle,
			delayText,
			delaySlider,
			actions
		);
		settingsCard.setPadding(new Insets(16));
		settingsCard.setStyle(
			"-fx-background-color: rgba(255,255,255,0.94);" +
			"-fx-border-color: rgba(0,0,0,0.08);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		VBox infoCard = new VBox(8,
			new Text("Thong tin"),
			currentPosText,
			stateLabel,
			scoreLabel,
			pathLabel,
			exploredLabel,
			statusText,
			inventorySection
		);
		((Text) infoCard.getChildren().get(0)).setFont(Font.font("Arial", FontWeight.BOLD, 28));
		((Text) infoCard.getChildren().get(0)).setFill(Color.web("#1F2D3A"));
		infoCard.setPadding(new Insets(16));
		infoCard.setStyle(
			"-fx-background-color: rgba(255,255,255,0.94);" +
			"-fx-border-color: rgba(0,0,0,0.08);" +
			"-fx-border-width: 1;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		VBox legendPanel = createLegendPanel();

		VBox sideColumn = new VBox(12, settingsCard, infoCard, legendPanel);
		sideColumn.setPrefWidth(360);
		sideColumn.setMinWidth(360);
		sideColumn.setMaxWidth(360);
		sideColumn.setMaxHeight(Double.MAX_VALUE);

		Runnable renderFrame = () -> {
			if (mode == PlayMode.BOT) {
				MazeRenderer.render(
					gc, maze,
					botEngine.getExplored(), botEngine.getPath(),
					botRenderX[0], botRenderY[0],
					mazeCanvas.getWidth(), mazeCanvas.getHeight()
				);
			} else {
				// ── Determine visual overlays from active power-ups ──────────
				java.util.List<State> pathOverlay     = null;
				java.util.List<State> exploredOverlay = null;

				if (pw.isRevealingMap()) {
					exploredOverlay = allWalkable(maze);
				} else if (pw.isDetectingBombs()) {
					exploredOverlay = bombPositions(maze);
				}
				if (pw.isRevealingPath() || pw.aiRunning) {
					pathOverlay = computePathToGoal(maze, playerPos[0]);
				}

				MazeRenderer.render(
					gc, maze,
					exploredOverlay, pathOverlay,
					playerRenderX[0], playerRenderY[0],
					mazeCanvas.getWidth(), mazeCanvas.getHeight()
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
							&& botEngine.getState() == GameState.MOVING) {
						botAccumulatorNanos[0] -= stepDurationNanos;

						State prevPos = botEngine.getRobotPosition();
						botEngine.update();
						State nextPos = botEngine.getRobotPosition();

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
								botEngine.getState() == GameState.FINISHED
							);
							rankingRecorded[0] = true;
						}

						refreshBotStats(botEngine, stateLabel, scoreLabel, pathLabel, exploredLabel, statusText);
						State pos = botEngine.getRobotPosition();
						if (pos != null) {
							currentPosText.setText("Vi tri hien tai: (" + pos.getX() + ", " + pos.getY() + ")");
						}
					}

					// ── Linear interpolation: constant-velocity motion for the entire step ──
					// The duck glides from botFrom → botTo smoothly for the full step duration.
					// No easing = no perceived pause at start/end of each cell transition.
					double progress = stepDurationNanos <= 0
						? 1.0
						: Math.min(1.0, Math.max(0.0,
							(double) botAccumulatorNanos[0] / (double) stepDurationNanos));
					botRenderX[0] = botFromX[0] + (botToX[0] - botFromX[0]) * progress;
					botRenderY[0] = botFromY[0] + (botToY[0] - botFromY[0]) * progress;

					renderFrame.run();
				}
			};
			loop.start();
		} else {
			refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
			statusText.setFill(Color.web("#AEE8FF"));
			statusText.setText("MOVE WITH ARROW KEYS - REACH THE GOAL FLAG");
			currentPosText.setText("Vi tri hien tai: (" + playerPos[0].getX() + ", " + playerPos[0].getY() + ")");
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

					// ── Accumulator: identical pattern to bot ──────────────────────────
					// ── Expire timed power-ups every frame ─────────────────────────
					pw.tickExpiry();

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
						if (playerAccumulatorNanos[0] >= stepNs && !playerFinished[0] && !selectingPowerUp[0]) {
							playerAccumulatorNanos[0] -= stepNs;
							State nextAi = pw.aiPath.get(pw.aiPathIdx++);
							playerFromX[0] = playerRenderX[0];
							playerFromY[0] = playerRenderY[0];
							playerPos[0] = new State(nextAi.getX(), nextAi.getY(), playerLives[0]);
							playerToX[0] = nextAi.getX();
							playerToY[0] = nextAi.getY();
							stepCounter[0]++;
							playerScore[0] = Math.max(0, playerScore[0] - 3);
							audio.playFootstep(masterVolume[0], sfxVolume[0]);
							refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0],
								stateLabel, scoreLabel, pathLabel, exploredLabel);
							currentPosText.setText("Vi tri hien tai: ("
								+ nextAi.getX() + ", " + nextAi.getY() + ")");
							if (pw.aiPathIdx >= pw.aiPath.size() || pw.aiPathIdx > 8) {
								pw.aiRunning = false;
								pw.aiPath = null;
								statusText.setFill(Color.web("#A5D6A7"));
								statusText.setText("AI ASSIST hoan thanh — tiep tuc tu di chuyen!");
							}
						}
					// ── Normal player step ────────────────────────────────────────────
					} else {
						if (pw.aiRunning) { pw.aiRunning = false; pw.aiPath = null; }

						if (playerAccumulatorNanos[0] >= stepNs
								&& playerHeldKey[0] != null
								&& !playerFinished[0]
								&& !selectingPowerUp[0]) {

							playerAccumulatorNanos[0] -= stepNs;

							// Start from current render position (seamless)
							playerFromX[0] = playerRenderX[0];
							playerFromY[0] = playerRenderY[0];

							boolean moved = handlePlayerMove(
								playerHeldKey[0],
								maze, playerPos, playerScore, playerLives, playerFinished,
								statusText, audio, masterVolume, sfxVolume,
								inventory, gameScene[0], selectingPowerUp, renderFrame,
								gameStartTime, stepCounter, rankingRecorded,
								difficulty, algorithmName, pw
							);

							playerToX[0] = playerPos[0].getX();
							playerToY[0] = playerPos[0].getY();

							if (moved) {
								refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0],
									stateLabel, scoreLabel, pathLabel, exploredLabel);
								currentPosText.setText("Vi tri hien tai: ("
									+ playerPos[0].getX() + ", " + playerPos[0].getY() + ")");
							}
						}
					}

					// ── Linear interpolation — constant-velocity glide ───────────────
					double progress = Math.min(1.0, Math.max(0.0,
						(double) playerAccumulatorNanos[0] / (double) stepNs));
					playerRenderX[0] = playerFromX[0] + (playerToX[0] - playerFromX[0]) * progress;
					playerRenderY[0] = playerFromY[0] + (playerToY[0] - playerFromY[0]) * progress;

					renderFrame.run();
				}
			};
			loop.start();
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
			playerHeldKey[0] = null; // stop player movement while paused
			if (finalLoop != null) {
				finalLoop.stop();
			}
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
		page.getChildren().addAll(mazeBoard, sideColumn);

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
		gameScene[0] = scene;
		
		if (mode == PlayMode.PLAYER) {
			// Key PRESSED — register held direction; step fires in the AnimationTimer
			scene.setOnKeyPressed(e -> {
				if (optionsOpen[0] || selectingPowerUp[0]) {
					e.consume();
					return;
				}
				if (isMovementKey(e.getCode())) {
					playerHeldKey[0] = e.getCode();
					e.consume();
				}
			});

			// Key RELEASED — stop continuous motion
			scene.setOnKeyReleased(e -> {
				if (isMovementKey(e.getCode()) && e.getCode() == playerHeldKey[0]) {
					playerHeldKey[0] = null;
				}
			});

			// ── Wire inventory: clicking an item in the panel activates its effect ──
			if (inventory != null) {
				inventory.setOnActivateCallback(collected -> {
					if (collected == null || !collected.isActive()) return;
					PowerUp type = collected.getPowerUp();
					long NOW = System.currentTimeMillis();

					switch (type) {
						case EXTRA_LIFE -> {
							playerLives[0] = Math.min(playerLives[0] + 1, 9);
							statusText.setFill(Color.web("#00FF9C"));
							statusText.setText("EXTRA LIFE! Mang song: " + playerLives[0]);
							refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
						}
						case SHIELD -> {
							pw.shield = true;
							statusText.setFill(Color.web("#64B5F6"));
							statusText.setText("SHIELD kich hoat — bomb tiep theo duoc chan!");
						}
						case BOMB_IMMUNITY -> {
							pw.bombImmune = true; pw.immuneUntil = NOW + 8_000;
							statusText.setFill(Color.web("#80DEEA"));
							statusText.setText("MIEN NHIEM BOMB trong 8 giay!");
						}
						case FREEZE_TIME, SLOW_BOMBS -> {
							pw.bombImmune = true; pw.immuneUntil = NOW + 6_000;
							statusText.setFill(Color.web("#B3E5FC"));
							statusText.setText("BOMB BI DONG LANH trong 6 giay!");
						}
						case DOUBLE_SCORE -> {
							pw.doubleScore = true; pw.dblScoreUntil = NOW + 15_000;
							statusText.setFill(Color.web("#FFD54F"));
							statusText.setText("DIEM X2 trong 15 giay!");
						}
						case REVEAL_PATH, SHORTEST_PATH_MODE -> {
							pw.revealPath = true; pw.revealPathUntil = NOW + 20_000;
							statusText.setFill(Color.web("#A5D6A7"));
							statusText.setText("DUONG DI duoc hien thi trong 20 giay!");
						}
						case REVEAL_MAP -> {
							pw.revealMap = true; pw.revealMapUntil = NOW + 20_000;
							statusText.setFill(Color.web("#C5E1A5"));
							statusText.setText("TOAN BO BAN DO duoc mo trong 20 giay!");
						}
						case BOMB_DETECTOR -> {
							pw.bombDetect = true; pw.detectUntil = NOW + 15_000;
							statusText.setFill(Color.web("#FFCC80"));
							statusText.setText("PHAT HIEN BOMB trong 15 giay!");
						}
						case SPEED_BOOST -> {
							pw.effectiveStepNs = PLAYER_STEP_NANOS / 2;
							pw.speedUntil = NOW + 8_000;
							// reset accumulator so new speed takes effect immediately
							playerAccumulatorNanos[0] = pw.effectiveStepNs;
							statusText.setFill(Color.web("#80DEEA"));
							statusText.setText("TOC DO TANG x2 trong 8 giay!");
						}
						case SPEED_SLOW -> {
							pw.effectiveStepNs = PLAYER_STEP_NANOS * 2;
							pw.speedUntil = NOW + 8_000;
							playerAccumulatorNanos[0] = pw.effectiveStepNs;
							statusText.setFill(Color.web("#BCAAA4"));
							statusText.setText("CHE DO CHAM — an toan hon trong 8 giay!");
						}
						case TELEPORT -> {
							com.nhom_01.robot_pathfinding.core.State newPos =
								findRandomSafeCell(maze, playerPos[0]);
							if (newPos != null) {
								playerPos[0] = new State(newPos.getX(), newPos.getY(), playerLives[0]);
								playerRenderX[0] = newPos.getX(); playerRenderY[0] = newPos.getY();
								playerFromX[0] = newPos.getX(); playerFromY[0] = newPos.getY();
								playerToX[0]   = newPos.getX(); playerToY[0]   = newPos.getY();
								refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
								currentPosText.setText("Vi tri hien tai: (" + newPos.getX() + ", " + newPos.getY() + ")");
								statusText.setFill(Color.web("#CE93D8"));
								statusText.setText("DICH CHUYEN toi vi tri an toan!");
							}
						}
						case REMOVE_WALL -> {
							pw.wallRemoval = true;
							statusText.setFill(Color.web("#FFAB91"));
							statusText.setText("XOA TUONG san sang — di vao tuong de pha!");
						}
						case SAFE_STEP -> {
							pw.safeStep = true;
							statusText.setFill(Color.web("#A5D6A7"));
							statusText.setText("BUOC AN TOAN — bomb tiep theo bi vo hieu hoa!");
						}
						case TIME_BONUS -> {
							playerScore[0] += 500;
							statusText.setFill(Color.web("#FFD54F"));
							statusText.setText("+500 DIEM THUONG!");
							refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
						}
						case LUCKY_FIND -> {
							playerScore[0] += 300;
							statusText.setFill(Color.web("#FFF176"));
							statusText.setText("+300 DIEM MAY MAN!");
							refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
						}
						case AI_ASSIST -> {
							java.util.List<com.nhom_01.robot_pathfinding.core.State> aiPath =
								computePathToGoal(maze, playerPos[0]);
							if (aiPath != null && aiPath.size() > 1) {
								pw.aiRunning = true;
								pw.aiPath = aiPath;
								pw.aiPathIdx = 1;
								playerHeldKey[0] = null; // AI takes over
								statusText.setFill(Color.web("#80CBC4"));
								statusText.setText("AI TRO LY — tu dong di 8 buoc!");
							} else {
								statusText.setFill(Color.web("#FFAB91"));
								statusText.setText("AI TRO LY — khong tim duoc duong di.");
							}
						}
						case DOUBLE_CHOICE -> {
							if (gameScene[0] != null) {
								ItemCardSelectionModal.showOnScene(gameScene[0], extra -> {
									if (extra != null) inventory.addCollectedPowerUp(extra);
								}, () -> {});
								statusText.setFill(Color.web("#CE93D8"));
								statusText.setText("CHON THEM 1 VAT PHAM!");
							}
						}
						case VISION_BOOST -> {
							pw.revealMap = true; pw.revealMapUntil = NOW + 15_000;
							statusText.setFill(Color.web("#B3E5FC"));
							statusText.setText("TAM NHIN MO RONG trong 15 giay!");
						}
						default -> {
							statusText.setFill(Color.web("#FFD59A"));
							statusText.setText(type.getDisplayName() + " DA KICH HOAT!");
						}
					}
					renderFrame.run();
					// Note: inventory.updateDisplay() is already called inside activatePowerUp
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
		node.setFont(Font.font("Orbitron", FontWeight.BOLD, 15));
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
		pathText.setText("PATH: " + safeSize(engine.getPath()));
		exploredText.setText("EXPLORED: " + safeSize(engine.getExplored()));

		if (engine.getState() == GameState.NO_PATH) {
			statusText.setFill(Color.web("#FF6B6B"));
			statusText.setText("NO PATH FOUND - TRY ANOTHER ALGORITHM OR REPLAY");
		} else if (engine.getState() == GameState.FINISHED) {
			statusText.setFill(Color.web("#00FF9C"));
			statusText.setText("ROBOT REACHED GOAL - FINAL SCORE: " + engine.getScore());
		} else if (engine.getState() == GameState.MOVING) {
			State pos = engine.getRobotPosition();
			if (pos != null) {
				statusText.setFill(Color.web("#C9DCEA"));
				statusText.setText("ROBOT POSITION: (" + pos.getX() + ", " + pos.getY() + ")");
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
		Scene gameScene,
		boolean[] selectingPowerUp,
		Runnable renderFrame,
		long[] gameStartTime,
		int[] stepCounter,
		boolean[] rankingRecorded,
		String difficulty,
		String algorithmName,
		PowerUpState pw
	) {
		if (playerFinished[0] || selectingPowerUp[0]) {
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

		int nx = playerPos[0].getX() + dx;
		int ny = playerPos[0].getY() + dy;

		CellType nextCell = maze.getCell(nx, ny);

		// ── Wall check: REMOVE_WALL powerup can demolish 1 wall ───────────────
		if (nextCell == CellType.WALL) {
			if (pw.wallRemoval) {
				pw.wallRemoval = false;
				maze.setCell(nx, ny, CellType.EMPTY);
				nextCell = CellType.EMPTY;
				statusText.setFill(Color.web("#FFAB91"));
				statusText.setText("TUONG DA BI XOA!");
			} else {
				statusText.setFill(Color.web("#FFD59A"));
				statusText.setText("DUONG BI CHAN - thu huong khac");
				return false;
			}
		}

		// Each step costs 3 score (unchanged if double-score active on losses)
		playerScore[0] = Math.max(0, playerScore[0] - 3);
		stepCounter[0]++;
		audio.playFootstep(masterVolume[0], sfxVolume[0]);

		// ── Bomb ──────────────────────────────────────────────────────────────
		if (nextCell == CellType.BOMB) {
			maze.setCell(nx, ny, CellType.EMPTY);
			if (pw.isBombSafe() || pw.safeStep) {
				// Safe: bomb neutralised
				pw.safeStep = false;
				statusText.setFill(Color.web("#A5D6A7"));
				statusText.setText("BOMB BI VO HIEU HOA boi powerup!");
			} else if (pw.shield) {
				// Shield absorbs 1 bomb
				pw.shield = false;
				statusText.setFill(Color.web("#64B5F6"));
				statusText.setText("SHIELD DA CHAN BOMB!");
			} else {
				// Normal damage
				playerLives[0]--;
				playerScore[0] = Math.max(0, playerScore[0] - 120);
				statusText.setFill(Color.web("#FF8DA6"));
				statusText.setText("DINH BOMB -1 MANG SONG!");
				if (playerLives[0] <= 0) {
					playerFinished[0] = true;
					statusText.setFill(Color.web("#FF6B6B"));
					statusText.setText("GAME OVER - HET MANG SONG");
					if (!rankingRecorded[0]) {
						recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], false);
						rankingRecorded[0] = true;
					}
				}
			}

		// ── Item ─────────────────────────────────────────────────────────────
		} else if (nextCell == CellType.ITEM) {
			int reward = pw.isScoreDoubled() ? 360 : 180;
			playerScore[0] += reward;
			maze.setCell(nx, ny, CellType.EMPTY);
			statusText.setFill(Color.web("#9FFFD8"));
			statusText.setText("NHAT VAT PHAM (+" + reward + ") — chon power-up!");

			if (inventory != null && gameScene != null) {
				boolean shown = ItemCardSelectionModal.showOnScene(gameScene, selectedPowerUp -> {
					if (selectedPowerUp != null) {
						inventory.addCollectedPowerUp(selectedPowerUp);
					}
					if (renderFrame != null) renderFrame.run();
				}, () -> selectingPowerUp[0] = false);
				selectingPowerUp[0] = shown;
				if (!shown) {
					statusText.setFill(Color.web("#FFD59A"));
					statusText.setText("VAT PHAM DA NHAT — tiep tuc di chuyen");
				}
			}

		// ── Goal ─────────────────────────────────────────────────────────────
		} else if (nextCell == CellType.GOAL) {
			playerFinished[0] = true;
			statusText.setFill(Color.web("#00FF9C"));
			statusText.setText("DA THOAT! DIEM CUOI: " + Math.max(0, playerScore[0]));
			if (!rankingRecorded[0]) {
				recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], true);
				rankingRecorded[0] = true;
			}
		} else {
			statusText.setFill(Color.web("#AEE8FF"));
			statusText.setText("DI CHUYEN BANG MUI TEN — TIM CUA THOAT");
		}

		playerPos[0] = new State(nx, ny, Math.max(0, playerLives[0]));
		return true;
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

		Image landTexture = loadPlayImage("/image/vit/Land.png");
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

		Text title = new Text("Chu thich");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 31));
		title.setFill(Color.web("#1F2D3A"));

		Text hint = new Text("Nhan biet nhanh cac doi tuong trong ban do:");
		hint.setFont(Font.font("Arial", FontWeight.BOLD, 13));
		hint.setFill(Color.web("#4F5B62"));

		VBox rows = new VBox(8,
			legendRow("/image/vit/Duck.png", "R", Color.web("#FFE082"), "Nguoi choi / Robot"),
			legendRow("/image/vit/FinishLine.png", "F", Color.web("#212121"), "GOAL"),
			legendRow("/image/vit/Grass.png", "W", Color.web("#A3D977"), "Vat can"),
			legendRow("/image/vit/Water.png", "~", Color.web("#8AE5F6"), "Duong nuoc"),
			legendRow("/image/vit/Flag.png", ".", Color.web("#AFC7FF"), "Da di qua"),
			legendRow("/image/vit/Vit.png", "+", Color.web("#A5D6A7"), "Solution / Item"),
			legendRow("/image/vit/Stop.png", "X", Color.web("#FF6B6B"), "Duong cut / Nguy hiem")
		);

		Text foot = new Text("Co the mo options de bat goi y duong di trong qua trinh choi.");
		foot.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
		foot.setFill(Color.web("#7B8A93"));
		foot.setWrappingWidth(330);

		panel.getChildren().addAll(title, hint, rows, foot);
		return panel;
	}

	private static HBox legendRow(String imagePath, String fallbackSymbol, Color fallbackColor, String meaning) {
		javafx.scene.Node iconNode = createLegendIcon(imagePath, fallbackSymbol, fallbackColor);

		Text text = new Text(meaning);
		text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
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
		fallback.setFont(Font.font("Arial", FontWeight.BOLD, 18));
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
		panel.setPrefWidth(620);
		panel.setMaxWidth(620);
		panel.setStyle(
			"-fx-background-color: rgba(255,255,255,0.98);" +
			"-fx-border-color: rgba(0,0,0,0.12);" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;"
		);

		Text title = new Text("OPTIONS");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, 32));
		title.setFill(Color.web("#1F2D3A"));

		Text subtitle = new Text("Pause game settings: audio, gameplay support, and quick exit");
		subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		subtitle.setFill(Color.web("#4F5B62"));

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
		gameplayLabel.setTextFill(Color.web("#546E7A"));
		gameplayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

		HBox actions = new HBox(10);
		actions.setAlignment(Pos.CENTER_RIGHT);
		Button resumeBtn = new NeonButton("RESUME", Color.web("#2E7D32"), 13, 7, 14, 6);
		Button exitBtn = new NeonButton("EXIT TO MENU", Color.web("#D84343"), 13, 7, 14, 6);

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
		title.setFont(Font.font("Arial", FontWeight.BOLD, 14));
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
		box.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
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
		boolean shield      = false;  // absorb next bomb
		boolean wallRemoval = false;  // destroy next wall hit
		boolean safeStep    = false;  // neutralise next bomb cell

		// Timed: bomb protection
		boolean bombImmune  = false;  long immuneUntil    = 0L;

		// Timed: score doubler
		boolean doubleScore = false;  long dblScoreUntil  = 0L;

		// Timed: visual overlays
		boolean revealPath  = false;  long revealPathUntil = 0L;
		boolean revealMap   = false;  long revealMapUntil  = 0L;
		boolean bombDetect  = false;  long detectUntil     = 0L;

		// Speed modifier  (default = PLAYER_STEP_NANOS, set externally)
		long effectiveStepNs = PLAYER_STEP_NANOS;
		long speedUntil      = 0L;

		// AI assist – follows BFS path for a few steps
		boolean             aiRunning = false;
		java.util.List<com.nhom_01.robot_pathfinding.core.State> aiPath = null;
		int                 aiPathIdx = 0;

		/** Call every frame to expire timed effects. */
		void tickExpiry() {
			long now = System.currentTimeMillis();
			if (bombImmune  && now > immuneUntil)     bombImmune  = false;
			if (doubleScore && now > dblScoreUntil)   doubleScore = false;
			if (revealPath  && now > revealPathUntil) revealPath  = false;
			if (revealMap   && now > revealMapUntil)  revealMap   = false;
			if (bombDetect  && now > detectUntil)     bombDetect  = false;
			if (effectiveStepNs != PLAYER_STEP_NANOS && now > speedUntil)
				effectiveStepNs = PLAYER_STEP_NANOS;
		}

		boolean isBombSafe() {
			return bombImmune && System.currentTimeMillis() < immuneUntil;
		}
		boolean isScoreDoubled() {
			return doubleScore && System.currentTimeMillis() < dblScoreUntil;
		}
		boolean isRevealingPath() {
			return revealPath && System.currentTimeMillis() < revealPathUntil;
		}
		boolean isRevealingMap() {
			return revealMap && System.currentTimeMillis() < revealMapUntil;
		}
		boolean isDetectingBombs() {
			return bombDetect && System.currentTimeMillis() < detectUntil;
		}
	}

	/** BFS path from current player position to maze goal (used by REVEAL_PATH, AI_ASSIST). */
	private static java.util.List<com.nhom_01.robot_pathfinding.core.State> computePathToGoal(
			Maze maze,
			com.nhom_01.robot_pathfinding.core.State from) {
		try {
			SearchResult result = new com.nhom_01.robot_pathfinding.ai.BFS().findPath(maze, from, maze.getGoal());
			return (result != null) ? result.getPath() : null;
		} catch (Exception e) {
			return null;
		}
	}

	/** All walkable (non-wall) cells — used by REVEAL_MAP overlay. */
	private static java.util.List<com.nhom_01.robot_pathfinding.core.State> allWalkable(Maze maze) {
		java.util.List<com.nhom_01.robot_pathfinding.core.State> cells = new java.util.ArrayList<>();
		for (int x = 0; x < maze.getWidth(); x++)
			for (int y = 0; y < maze.getHeight(); y++)
				if (maze.getCell(x, y) != CellType.WALL)
					cells.add(new com.nhom_01.robot_pathfinding.core.State(x, y, 0));
		return cells;
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
				if (c != CellType.WALL && c != CellType.BOMB
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
		boolean won
	) {
		try {
			long currentTimeMs = System.currentTimeMillis();
			long elapsedTimeMs = currentTimeMs - gameStartTimeMs;
			
			// Calculate final score based on performance
			int calculatedScore = calculateFinalScore(finalScore, steps, elapsedTimeMs, won);
			
			RankingEntry entry = new RankingEntry(
				PlayerProfile.getCurrentPlayerName(),
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
}
