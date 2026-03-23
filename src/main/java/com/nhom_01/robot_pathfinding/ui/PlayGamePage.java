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

	private static final double VIEW_WIDTH = 1400;
	private static final double VIEW_HEIGHT = 800;
	private static final double CANVAS_WITH_LEGEND = 900;
	private static final double CANVAS_NO_LEGEND = 1160;
	private static final long MOVE_COOLDOWN_NANOS = 85_000_000L;

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
		boolean[] playerAnimating = new boolean[] { false };
		long[] lastMoveNanos = new long[] { 0L };
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
		long[] playerLastFrameNanos = new long[] { 0L };
		long[] playerMovementDurationNanos = new long[] { 200_000_000L };

		StackPane root = new StackPane();
		root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
		root.getChildren().add(createBackground());

		HBox page = new HBox(18);
		page.setPadding(new Insets(16, 22, 16, 22));
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

		Canvas mazeCanvas = new Canvas(860, 720);
		GraphicsContext gc = mazeCanvas.getGraphicsContext2D();

		StackPane mazeBoard = new StackPane(mazeCanvas);
		mazeBoard.setPrefSize(880, 740);
		mazeBoard.setMinSize(880, 740);
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
					mazeCanvas.getHeight()
				);
			} else {
				MazeRenderer.render(
					gc,
					maze,
					null,
					null,
					playerRenderX[0],
					playerRenderY[0],
					mazeCanvas.getWidth(),
					mazeCanvas.getHeight()
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

					long frameDelta = now - botLastFrameNanos[0];
					botLastFrameNanos[0] = now;
					botAccumulatorNanos[0] += Math.max(0L, frameDelta);

					long stepDurationNanos = Math.max(16_000_000L, Math.round(delaySlider.getValue() * 1_000_000.0));

					while (botAccumulatorNanos[0] >= stepDurationNanos && botEngine.getState() == GameState.MOVING) {
						botAccumulatorNanos[0] -= stepDurationNanos;

						int prevPathSize = safeSize(botEngine.getPath());
						int prevExploredSize = safeSize(botEngine.getExplored());
						GameState prevState = botEngine.getState();
						State prevPos = botEngine.getRobotPosition();

						botEngine.update();

						State nextPos = botEngine.getRobotPosition();
						if (nextPos != null) {
							botFromX[0] = botRenderX[0];
							botFromY[0] = botRenderY[0];
							botToX[0] = nextPos.getX();
							botToY[0] = nextPos.getY();
						} else if (prevPos != null) {
							botFromX[0] = prevPos.getX();
							botFromY[0] = prevPos.getY();
							botToX[0] = prevPos.getX();
							botToY[0] = prevPos.getY();
						}

						if (botEngine.getState() == GameState.MOVING && prevPos != null && nextPos != null) {
							if (prevPos.getX() != nextPos.getX() || prevPos.getY() != nextPos.getY()) {
								audio.playFootstep(masterVolume[0], sfxVolume[0]);
							}
						}

						if (!rankingRecorded[0] && (botEngine.getState() == GameState.FINISHED || botEngine.getState() == GameState.NO_PATH)) {
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

					double progress = stepDurationNanos <= 0
						? 1.0
						: Math.min(1.0, Math.max(0.0, (double) botAccumulatorNanos[0] / (double) stepDurationNanos));
					double t = reducedMotion[0] ? 1.0 : smoothStep(progress);
					botRenderX[0] = botFromX[0] + (botToX[0] - botFromX[0]) * t;
					botRenderY[0] = botFromY[0] + (botToY[0] - botFromY[0]) * t;

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
			playerFromX[0] = playerPos[0].getX();
			playerFromY[0] = playerPos[0].getY();
			playerToX[0] = playerPos[0].getX();
			playerToY[0] = playerPos[0].getY();
			loop = new AnimationTimer() {
				@Override
				public void handle(long now) {
					if (playerLastFrameNanos[0] == 0L) {
						playerLastFrameNanos[0] = now;
						return;
					}

					long frameDelta = Math.max(0L, now - playerLastFrameNanos[0]);
					playerLastFrameNanos[0] = now;

					boolean movingTween = playerFromX[0] != playerToX[0] || playerFromY[0] != playerToY[0];
					if (movingTween) {
						playerAccumulatorNanos[0] += frameDelta;
						double progress = playerMovementDurationNanos[0] <= 0
							? 1.0
							: Math.min(1.0, (double) playerAccumulatorNanos[0] / (double) playerMovementDurationNanos[0]);
						double t = reducedMotion[0] ? 1.0 : smoothStep(progress);
						playerRenderX[0] = playerFromX[0] + (playerToX[0] - playerFromX[0]) * t;
						playerRenderY[0] = playerFromY[0] + (playerToY[0] - playerFromY[0]) * t;

						if (progress >= 1.0) {
							playerRenderX[0] = playerToX[0];
							playerRenderY[0] = playerToY[0];
							playerFromX[0] = playerToX[0];
							playerFromY[0] = playerToY[0];
							playerAccumulatorNanos[0] = 0L;
							playerAnimating[0] = false;
						}
					} else {
						playerRenderX[0] = playerToX[0];
						playerRenderY[0] = playerToY[0];
						playerAccumulatorNanos[0] = 0L;
						playerAnimating[0] = false;
					}

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

		Scene scene = new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
		gameScene[0] = scene;
		
		if (mode == PlayMode.PLAYER) {
			scene.setOnKeyPressed(e -> {
				if (optionsOpen[0] || selectingPowerUp[0]) {
					e.consume();
					return;
				}

				if (isMovementKey(e.getCode()) && playerAnimating[0]) {
					e.consume();
					return;
				}

				if (isMovementKey(e.getCode())) {
					long now = System.nanoTime();
					if (now - lastMoveNanos[0] < MOVE_COOLDOWN_NANOS) {
						e.consume();
						return;
					}
					lastMoveNanos[0] = now;
				}

				boolean changed = handlePlayerMove(
					e.getCode(),
					maze,
					playerPos,
					playerScore,
					playerLives,
					playerFinished,
					statusText,
					audio,
					masterVolume,
					sfxVolume,
					inventory,
					scene,
					selectingPowerUp,
					renderFrame,
					gameStartTime,
					stepCounter,
					rankingRecorded,
					difficulty,
					algorithmName
				);

				if (changed) {
					playerFromX[0] = playerToX[0];
					playerFromY[0] = playerToY[0];
					playerToX[0] = playerPos[0].getX();
					playerToY[0] = playerPos[0].getY();
					playerAccumulatorNanos[0] = 0L;
					playerAnimating[0] = true;
					refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateLabel, scoreLabel, pathLabel, exploredLabel);
					currentPosText.setText("Vi tri hien tai: (" + playerPos[0].getX() + ", " + playerPos[0].getY() + ")");
				}
			});
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
		String algorithmName
	) {
		if (playerFinished[0] || selectingPowerUp[0]) {
			return false;
		}

		int dx = 0;
		int dy = 0;
		switch (code) {
			case UP -> dy = -1;
			case DOWN -> dy = 1;
			case LEFT -> dx = -1;
			case RIGHT -> dx = 1;
			default -> {
				return false;
			}
		}

		int nx = playerPos[0].getX() + dx;
		int ny = playerPos[0].getY() + dy;

		if (maze.getCell(nx, ny) == CellType.WALL) {
			statusText.setFill(Color.web("#FFD59A"));
			statusText.setText("BLOCKED BY WALL - TRY ANOTHER DIRECTION");
			return true;
		}

		CellType target = maze.getCell(nx, ny);
		playerScore[0] = Math.max(0, playerScore[0] - 5);
		stepCounter[0]++;
		audio.playFootstep(masterVolume[0], sfxVolume[0]);

		if (target == CellType.BOMB) {
			playerLives[0]--;
			playerScore[0] = Math.max(0, playerScore[0] - 120);
			maze.setCell(nx, ny, CellType.EMPTY);
			statusText.setFill(Color.web("#FF8DA6"));
			statusText.setText("HIT A BOMB -1 LIFE");
			if (playerLives[0] <= 0) {
				playerFinished[0] = true;
				statusText.setFill(Color.web("#FF6B6B"));
				statusText.setText("GAME OVER - OUT OF LIVES");
				if (!rankingRecorded[0]) {
					recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], false);
					rankingRecorded[0] = true;
				}
			}
		} else if (target == CellType.ITEM) {
			playerScore[0] += 180;
			maze.setCell(nx, ny, CellType.EMPTY);
			statusText.setFill(Color.web("#9FFFD8"));
			statusText.setText("ITEM COLLECTED - SELECT A POWER-UP");
			
			// Show in-scene overlay modal (do not replace scene to avoid focus freeze).
			if (inventory != null && gameScene != null) {
				boolean shown = ItemCardSelectionModal.showOnScene(gameScene, selectedPowerUp -> {
					if (selectedPowerUp != null) {
						inventory.addCollectedPowerUp(selectedPowerUp);
					}
					if (renderFrame != null) {
						renderFrame.run();
					}
				}, () -> selectingPowerUp[0] = false);
				selectingPowerUp[0] = shown;
				if (!shown) {
					statusText.setFill(Color.web("#FFD59A"));
					statusText.setText("ITEM COLLECTED - CONTINUE MOVING (MODAL UNAVAILABLE)");
				}
			}
		} else if (target == CellType.GOAL) {
			playerFinished[0] = true;
			statusText.setFill(Color.web("#00FF9C"));
			statusText.setText("YOU REACHED GOAL - FINAL SCORE: " + Math.max(0, playerScore[0]));
			if (!rankingRecorded[0]) {
				recordGameRanking(difficulty, stepCounter[0], gameStartTime[0], algorithmName, playerScore[0], true);
				rankingRecorded[0] = true;
			}
		} else {
			statusText.setFill(Color.web("#AEE8FF"));
			statusText.setText("MOVE WITH ARROW KEYS - REACH THE GOAL FLAG");
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
		Pane pane = new Pane();
		pane.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
		Canvas bg = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);
		GraphicsContext gc = bg.getGraphicsContext2D();

		Image landTexture = loadPlayImage("/image/vit/Land.png");
		if (landTexture != null && !landTexture.isError()) {
			double tile = 50;
			for (double x = 0; x < VIEW_WIDTH; x += tile) {
				for (double y = 0; y < VIEW_HEIGHT; y += tile) {
					gc.drawImage(landTexture, x, y, tile, tile);
				}
			}
		} else {
			for (int y = 0; y < (int) VIEW_HEIGHT; y++) {
				double ratio = y / VIEW_HEIGHT;
				int r = (int) (248 + (236 - 248) * ratio);
				int g = (int) (209 + (198 - 209) * ratio);
				int b = (int) (142 + (130 - 142) * ratio);
				gc.setStroke(Color.rgb(r, g, b));
				gc.strokeLine(0, y, VIEW_WIDTH, y);
			}

			gc.setFill(Color.color(0.92, 0.72, 0.45, 0.35));
			for (int x = 0; x < VIEW_WIDTH; x += 18) {
				for (int y = 0; y < VIEW_HEIGHT; y += 18) {
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
