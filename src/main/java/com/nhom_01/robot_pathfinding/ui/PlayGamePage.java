package com.nhom_01.robot_pathfinding.ui;

import com.nhom_01.robot_pathfinding.ai.AStar;
import com.nhom_01.robot_pathfinding.ai.BFS;
import com.nhom_01.robot_pathfinding.ai.DFS;
import com.nhom_01.robot_pathfinding.ai.SearchAlgorithm;
import com.nhom_01.robot_pathfinding.core.*;
import com.nhom_01.robot_pathfinding.game.GameEngine;
import com.nhom_01.robot_pathfinding.game.GameState;
import com.nhom_01.robot_pathfinding.ui.components.InventoryPanel;
import com.nhom_01.robot_pathfinding.ui.components.ItemCardSelectionModal;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
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
import javafx.scene.control.Slider;
import javafx.scene.input.KeyCode;
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
		long[] lastMoveNanos = new long[] { 0L };
		double[] masterVolume = new double[] { 80 };
		double[] musicVolume = new double[] { 70 };
		double[] sfxVolume = new double[] { 85 };
		boolean[] pathHint = new boolean[] { false };
		boolean[] aiSuggest = new boolean[] { false };
		boolean[] highContrast = new boolean[] { false };
		boolean[] reducedMotion = new boolean[] { false };
		
		// Ranking tracking
		long[] gameStartTime = new long[] { System.currentTimeMillis() };
		int[] stepCounter = new int[] { 0 };
		boolean[] rankingRecorded = new boolean[] { false };
		
		// Inventory for player mode
		InventoryPanel inventory = mode == PlayMode.PLAYER ? new InventoryPanel() : null;
		Scene[] gameScene = new Scene[1];

		StackPane root = new StackPane();
		root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
		root.getChildren().add(createBackground());

		VBox page = new VBox(10);
		page.setPadding(new Insets(14, 34, 14, 34));
		page.setAlignment(Pos.TOP_CENTER);

		Text title = new Text("LIVE PLAYGROUND");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, 40));
		title.setFill(Color.web("#00FFFF"));

		String subtitleText = mode == PlayMode.BOT
			? "MODE: BOT  |  DIFFICULTY: " + difficulty + "  |  ALGORITHM: " + algorithmName
			: "MODE: PLAYER  |  DIFFICULTY: " + difficulty + "  |  USE ARROW KEYS TO MOVE";
		Text subtitle = new Text(subtitleText);
		subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
		subtitle.setFill(Color.web("#C9DCEA"));

		HBox statsRow = new HBox(26);
		statsRow.setAlignment(Pos.CENTER);

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
		statsRow.getChildren().addAll(stateText, scoreText, pathText, exploredText);

		Canvas mazeCanvas = new Canvas(CANVAS_WITH_LEGEND, 480);
		GraphicsContext gc = mazeCanvas.getGraphicsContext2D();

		VBox legendPanel = createLegendPanel();
		HBox arenaRow = new HBox(18, mazeCanvas, legendPanel);
		arenaRow.setAlignment(Pos.CENTER);

		// Inventory panel for PLAYER mode
		VBox inventorySection = new VBox(8);
		inventorySection.setAlignment(Pos.CENTER_LEFT);
		inventorySection.setPrefHeight(72);
		if (mode == PlayMode.PLAYER) {
			Text inventoryLabel = new Text("COLLECTED POWER-UPS");
			inventoryLabel.setFont(Font.font("Orbitron", FontWeight.BOLD, 13));
			inventoryLabel.setFill(Color.web("#00FFFF"));
			inventorySection.getChildren().addAll(inventoryLabel, inventory.getContainer());
		} else {
			inventorySection.setVisible(false);
			inventorySection.setManaged(false);
		}

		Text statusText = new Text();
		statusText.setFont(Font.font("Arial", FontWeight.BOLD, 17));
		statusText.setFill(Color.web("#E3EFF9"));

		HBox actions = new HBox(12);
		actions.setAlignment(Pos.CENTER_RIGHT);

		Button replay = new NeonButton("REPLAY", Color.web("#00FF9C"), 14, 8, 14, 8);
		Button optionsButton = new NeonButton("OPTIONS", Color.web("#CCCCCC"), 14, 8, 14, 8);
		Button toggleLegend = new NeonButton("HIDE LEGEND", Color.web("#67D5FF"), 14, 8, 14, 8);

		Runnable renderFrame = () -> {
			if (mode == PlayMode.BOT) {
				MazeRenderer.render(
					gc,
					maze,
					botEngine.getExplored(),
					botEngine.getPath(),
					botEngine.getRobotPosition(),
					mazeCanvas.getWidth(),
					mazeCanvas.getHeight()
				);
			} else {
				MazeRenderer.render(
					gc,
					maze,
					null,
					null,
					playerPos[0],
					mazeCanvas.getWidth(),
					mazeCanvas.getHeight()
				);
			}
		};

		Timeline loop = null;
		if (mode == PlayMode.BOT) {
			loop = new Timeline(new KeyFrame(Duration.millis(220), e -> {
				if (botEngine.getState() == GameState.MOVING) {
					botEngine.update();
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
				refreshBotStats(botEngine, stateText, scoreText, pathText, exploredText, statusText);
				renderFrame.run();
			}));
			loop.setCycleCount(Timeline.INDEFINITE);
			loop.play();
		} else {
			refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateText, scoreText, pathText, exploredText);
			statusText.setFill(Color.web("#AEE8FF"));
			statusText.setText("MOVE WITH ARROW KEYS - REACH THE GOAL FLAG");
		}

		Timeline finalLoop = loop;
		replay.setOnAction(e -> {
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
				finalLoop.pause();
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
					if (finalLoop != null) {
						finalLoop.play();
					}
					root.requestFocus();
				},
				() -> {
					optionsOpen[0] = false;
					if (finalLoop != null) {
						finalLoop.stop();
					}
					stage.setScene(previousScene);
				}
			);
		});
 
		toggleLegend.setOnAction(e -> {
			boolean show = !legendPanel.isVisible();
			legendPanel.setVisible(show);
			legendPanel.setManaged(show);
			mazeCanvas.setWidth(show ? CANVAS_WITH_LEGEND : CANVAS_NO_LEGEND);
			toggleLegend.setText(show ? "HIDE LEGEND" : "SHOW LEGEND");
			renderFrame.run();
		});

		actions.getChildren().addAll(toggleLegend, optionsButton, replay);
		page.getChildren().addAll(title, subtitle, statsRow, arenaRow, inventorySection, statusText, actions);

		Pane overlay = new Pane();
		overlay.setStyle("-fx-background-color: rgba(0,0,0,0.14);");
		overlay.setMouseTransparent(true);

		root.getChildren().addAll(page, overlay);

		if (mode == PlayMode.BOT) {
			refreshBotStats(botEngine, stateText, scoreText, pathText, exploredText, statusText);
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
					refreshPlayerStats(playerPos[0], playerScore[0], playerLives[0], stateText, scoreText, pathText, exploredText);
					renderFrame.run();
				}
			});
			root.requestFocus();
		}
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

		for (int y = 0; y < (int) VIEW_HEIGHT; y++) {
			double ratio = y / VIEW_HEIGHT;
			int r = (int) (26 + (48 - 26) * ratio);
			int g = (int) (46 + (80 - 46) * ratio);
			int b = (int) (64 + (110 - 64) * ratio);
			gc.setStroke(Color.rgb(r, g, b));
			gc.strokeLine(0, y, VIEW_WIDTH, y);
		}

		gc.setStroke(Color.color(0.45, 0.82, 1.0, 0.20));
		gc.setLineWidth(1);
		int grid = 42;
		for (int x = 0; x < VIEW_WIDTH; x += grid) {
			gc.strokeLine(x, 0, x, VIEW_HEIGHT);
		}
		for (int y = 0; y < VIEW_HEIGHT; y += grid) {
			gc.strokeLine(0, y, VIEW_WIDTH, y);
		}

		pane.getChildren().add(bg);
		return pane;
	}

	private static VBox createLegendPanel() {
		VBox panel = new VBox(12);
		panel.setPrefWidth(260);
		panel.setPadding(new Insets(14, 14, 14, 14));
		panel.setAlignment(Pos.TOP_LEFT);
		panel.setStyle(
			"-fx-background-color: rgba(22,34,52,0.90);" +
			"-fx-border-color: rgba(126,223,255,0.62);" +
			"-fx-border-width: 1.4;" +
			"-fx-border-radius: 10;" +
			"-fx-background-radius: 10;"
		);

		Text title = new Text("LEGEND");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, 22));
		title.setFill(Color.web("#00FFFF"));

		Text hint = new Text("Mau sac va ky hieu de nguoi moi de nhan biet:");
		hint.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		hint.setFill(Color.web("#C9DCEA"));

		VBox rows = new VBox(8,
			legendRow("S", Color.web("#5EA5FF"), "Diem xuat phat"),
			legendRow("F", Color.web("#FFD166"), "Dich den"),
			legendRow("B", Color.web("#EE5A7A"), "Bom - tranh de mat loi"),
			legendRow("D", Color.web("#2BD99F"), "Vat pham + diem"),
			legendRow("R", Color.web("#8BE9FD"), "Vi tri robot hien tai"),
			legendRow(".", Color.web("#BFD5FF"), "Duong di AI de xuat"),
			legendRow("~", Color.web("#8FDFFF"), "O da duoc duyet")
		);

		Text foot = new Text("Ban co the an legend de mo rong khong gian map khi da quen ky hieu.");
		foot.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
		foot.setFill(Color.web("#9FB7C8"));
		foot.setWrappingWidth(245);

		panel.getChildren().addAll(title, hint, rows, foot);
		return panel;
	}

	private static HBox legendRow(String symbol, Color color, String meaning) {
		Text badge = new Text(symbol);
		badge.setFont(Font.font("Orbitron", FontWeight.BOLD, 18));
		badge.setFill(color);

		Text text = new Text(meaning);
		text.setFont(Font.font("Arial", FontWeight.BOLD, 14));
		text.setFill(Color.web("#D6E5EF"));

		HBox row = new HBox(10, badge, text);
		row.setAlignment(Pos.CENTER_LEFT);
		return row;
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
		Runnable onExitToMenu
	) {
		if (root.lookup("#in-game-options-overlay") != null) {
			return;
		}

		StackPane overlay = new StackPane();
		overlay.setId("in-game-options-overlay");
		overlay.setStyle("-fx-background-color: rgba(0,0,0,0.74);");
		overlay.setPickOnBounds(true);

		VBox panel = new VBox(14);
		panel.setAlignment(Pos.TOP_LEFT);
		panel.setPadding(new Insets(22));
		panel.setPrefWidth(620);
		panel.setMaxWidth(620);
		panel.setStyle(
			"-fx-background-color: rgba(14,24,38,0.96);" +
			"-fx-border-color: rgba(0,255,255,0.45);" +
			"-fx-border-width: 1.8;" +
			"-fx-border-radius: 12;" +
			"-fx-background-radius: 12;"
		);

		Text title = new Text("OPTIONS");
		title.setFont(Font.font("Orbitron", FontWeight.BOLD, 32));
		title.setFill(Color.web("#00FFFF"));

		Text subtitle = new Text("Pause game settings: audio, gameplay support, and quick exit");
		subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		subtitle.setFill(Color.web("#C9DCEA"));

		Slider master = createOptionsSlider(masterVolume[0]);
		Slider music = createOptionsSlider(musicVolume[0]);
		Slider sfx = createOptionsSlider(sfxVolume[0]);

		VBox masterRow = createOptionsSliderRow("Master", master);
		VBox musicRow = createOptionsSliderRow("Music", music);
		VBox sfxRow = createOptionsSliderRow("SFX", sfx);

		master.valueProperty().addListener((obs, ov, nv) -> masterVolume[0] = nv.doubleValue());
		music.valueProperty().addListener((obs, ov, nv) -> musicVolume[0] = nv.doubleValue());
		sfx.valueProperty().addListener((obs, ov, nv) -> sfxVolume[0] = nv.doubleValue());

		CheckBox pathHintToggle = createOptionsToggle("Show path hint", pathHint[0]);
		CheckBox aiToggle = createOptionsToggle("Enable AI suggestion", aiSuggest[0]);
		CheckBox contrastToggle = createOptionsToggle("High contrast labels", highContrast[0]);
		CheckBox motionToggle = createOptionsToggle("Reduced motion", reducedMotion[0]);

		pathHintToggle.selectedProperty().addListener((obs, ov, nv) -> pathHint[0] = nv);
		aiToggle.selectedProperty().addListener((obs, ov, nv) -> aiSuggest[0] = nv);
		contrastToggle.selectedProperty().addListener((obs, ov, nv) -> highContrast[0] = nv);
		motionToggle.selectedProperty().addListener((obs, ov, nv) -> reducedMotion[0] = nv);

		Label gameplayLabel = new Label("Gameplay / Accessibility");
		gameplayLabel.setTextFill(Color.web("#9FC8DE"));
		gameplayLabel.setFont(Font.font("Arial", FontWeight.BOLD, 13));

		HBox actions = new HBox(10);
		actions.setAlignment(Pos.CENTER_RIGHT);
		Button resumeBtn = new NeonButton("RESUME", Color.web("#00FF9C"), 13, 7, 14, 6);
		Button exitBtn = new NeonButton("EXIT TO MENU", Color.web("#FF6B6B"), 13, 7, 14, 6);

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
			"-fx-control-inner-background: #0D1520;" +
			"-fx-accent: #00FFFF;"
		);
		return slider;
	}

	private static VBox createOptionsSliderRow(String label, Slider slider) {
		VBox row = new VBox(6);
		Label title = new Label(label + "  " + String.format(java.util.Locale.US, "%.0f%%", slider.getValue()));
		title.setTextFill(Color.web("#D4E4EF"));
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
		box.setTextFill(Color.web("#D4E4EF"));
		box.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		box.setStyle(
			"-fx-mark-color: #00FFFF;" +
			"-fx-focus-color: transparent;" +
			"-fx-faint-focus-color: transparent;"
		);
		return box;
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
