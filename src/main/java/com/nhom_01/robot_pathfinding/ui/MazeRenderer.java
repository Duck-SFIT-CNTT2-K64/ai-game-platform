package com.nhom_01.robot_pathfinding.ui;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.nhom_01.robot_pathfinding.core.CellType;
import com.nhom_01.robot_pathfinding.core.Maze;
import com.nhom_01.robot_pathfinding.core.State;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

public class MazeRenderer {

	/** Visible time per mystery-box frame (2 and 3) after touch; frame 1 is idle. */
	public static final long MYSTERY_OPEN_FRAME_MS = 280L;
	public static final long MYSTERY_OPEN_TOTAL_MS = MYSTERY_OPEN_FRAME_MS * 2;

	/** Visible time per bomb hit frame (2 and 3); frame 1 is idle on the map. */
	public static final long BOMB_HIT_FRAME_MS = 280L;
	public static final long BOMB_HIT_TOTAL_MS = BOMB_HIT_FRAME_MS * 2;

	private static final Color BG = Color.web("#88E3F3");
	private static final Color WALL = Color.web("#BDE86E");
	private static final Color EMPTY = Color.web("#88E3F3");
	private static final Color ITEM = Color.web("#2BD99F");
	private static final Color BOMB = Color.web("#FF6B6B");
	private static final Color START = Color.web("#5EA5FF");
	private static final Color GOAL = Color.web("#212121");
	private static final Color EXPLORED = Color.color(0.43, 0.60, 1.0, 0.20);
	private static final Color PATH = Color.color(0.62, 0.86, 0.64, 0.45);
	private static final Color ROBOT = Color.web("#F9D648");

	// Pre-load all Animated / Static assets
	private static final Image[] DUCK_IDLE = new Image[4];
	private static final Image[] DUCK_WALK = new Image[4];
	private static final Image DUCK_HURT = loadImage("/image/pixel_animation/duck_hurt.png");
	
	private static final Image GRASS_1 = loadImage("/image/pixel_animation/Grass_1.png");
	private static final Image WATER = loadImage("/image/pixel_animation/water.gif");
	/** Bomb_1 = idle on map; Bomb_2/3 = touch animation (per-frame PNGs). */
	private static final Image[] BOMB_FRAMES = new Image[4];
	
	private static final Image[] FINISH_LINE = new Image[3];

	private static final Image STOP_IMAGE = loadImage("/image/pixel_animation/Stop.png");
	private static final Image ITEM_IMAGE = loadImage("/image/pixel_animation/mysterybox_fr1.png");
	private static final Image[] MYSTERY_FRAMES = new Image[3];
	private static final Image FLAG_IMAGE = loadImage("/image/pixel_animation/flag.png");

	static {
		for (int i = 0; i < 4; i++) {
			DUCK_IDLE[i] = loadImage(String.format("/image/pixel_animation/duck_idle_frames/idle_%02d.png", i));
			DUCK_WALK[i] = loadImage(String.format("/image/pixel_animation/duck_walk_frames/walk_%02d.png", i));
		}
		// Use flag sprites for the finish line (flag, flag_left, flag_right)
		FINISH_LINE[0] = loadImage("/image/pixel_animation/FinishLine_1.png");
		FINISH_LINE[1] = loadImage("/image/pixel_animation/FinishLine_2.png");
		FINISH_LINE[2] = loadImage("/image/pixel_animation/FinishLine_3.png");

		// Mystery box: fr1 idle, fr2–fr3 play on pickup
		for (int i = 0; i < 3; i++) {
			MYSTERY_FRAMES[i] = loadImage(String.format("/image/pixel_animation/mysterybox_fr%d.png", i + 1));
		}
		for (int i = 0; i < 4; i++) {
			BOMB_FRAMES[i] = loadImage("/image/pixel_animation/Bomb_" + (i + 1) + ".png");
		}
		// Using single animated GIF (`water.gif`) for in-play water; no frame array needed
	}

	private MazeRenderer() {
	}

	// Track bomb positions and explosion animations
	private static final Set<String> LAST_BOMB_POS = new HashSet<>();
	private static final Map<String, Long> BOMB_EXPLODE_TS = new HashMap<>();

	public static void render(
		GraphicsContext gc,
		Maze maze,
		List<State> explored,
		List<State> path,
		State robotPosition,
		double width,
		double height
	) {
		double robotGridX = robotPosition == null ? Double.NaN : robotPosition.getX();
		double robotGridY = robotPosition == null ? Double.NaN : robotPosition.getY();
		render(gc, maze, explored, path, robotGridX, robotGridY, width, height, 0L, -1, -1, 0L, -1, -1);
	}

	public static void render(
		GraphicsContext gc,
		Maze maze,
		List<State> explored,
		List<State> path,
		double robotGridX,
		double robotGridY,
		double width,
		double height
	) {
		render(gc, maze, explored, path, robotGridX, robotGridY, width, height, 0L, -1, -1, 0L, -1, -1);
	}

	/**
	 * @param mysteryOpenStartMs {@code System.currentTimeMillis()} when pickup started, or {@code 0} if none
	 * @param mysteryOpenGx      grid column of opening box (ignored if start is 0)
	 * @param mysteryOpenGy      grid row of opening box
	 */
	public static void render(
		GraphicsContext gc,
		Maze maze,
		List<State> explored,
		List<State> path,
		double robotGridX,
		double robotGridY,
		double width,
		double height,
		long mysteryOpenStartMs,
		int mysteryOpenGx,
		int mysteryOpenGy,
		long bombTouchStartMs,
		int bombTouchGx,
		int bombTouchGy
	) {
		gc.setFill(BG);
		gc.fillRect(0, 0, width, height);

		int mazeW = maze.getWidth();
		int mazeH = maze.getHeight();
		double cellSize = Math.min(width / mazeW, height / mazeH);
		double drawW = mazeW * cellSize;
		double drawH = mazeH * cellSize;
		double offsetX = (width - drawW) / 2.0;
		double offsetY = (height - drawH) / 2.0;

		long time = System.currentTimeMillis();

		// Draw base cells and collect bomb positions
		Set<String> currentBombs = new HashSet<>();
		for (int x = 0; x < mazeW; x++) {
			for (int y = 0; y < mazeH; y++) {
				CellType type = maze.getCell(x, y);
				double px = offsetX + x * cellSize;
				double py = offsetY + y * cellSize;

				if (type == CellType.WALL) {
					if (GRASS_1 != null && !GRASS_1.isError()) {
						// Grass tiles fill the entire cell in pixel style
						gc.drawImage(GRASS_1, px, py, cellSize, cellSize);
					} else {
						gc.setFill(WALL);
						gc.fillRect(px, py, cellSize, cellSize);
					}
				} else {
					if (WATER != null && !WATER.isError()) {
						gc.drawImage(WATER, px, py, cellSize, cellSize);
					} else {
						gc.setFill(colorFor(type));
						gc.fillRect(px, py, cellSize, cellSize);
					}
				}

				if (type == CellType.BOMB) currentBombs.add(x + "," + y);
			}
		}

		// detect bombs that were removed this frame -> start explode animation
		Set<String> removedBombs = new HashSet<>(LAST_BOMB_POS);
		removedBombs.removeAll(currentBombs);
		long now = System.currentTimeMillis();
		for (String k : removedBombs) {
			BOMB_EXPLODE_TS.put(k, now);
		}
		LAST_BOMB_POS.clear(); LAST_BOMB_POS.addAll(currentBombs);

		if (explored != null) {
			gc.setFill(EXPLORED);
			for (State s : explored) {
				double cellX = offsetX + s.getX() * cellSize;
				double cellY = offsetY + s.getY() * cellSize;
				gc.fillRect(cellX, cellY, cellSize, cellSize);
				if (FLAG_IMAGE != null && !FLAG_IMAGE.isError() && maze.getCell(s.getX(), s.getY()) != CellType.WALL) {
					gc.drawImage(FLAG_IMAGE, cellX + cellSize * 0.14, cellY + cellSize * 0.14, cellSize * 0.72, cellSize * 0.72);
				}
			}
		}

		if (path != null) {
			gc.setFill(PATH);
			for (State s : path) {
				gc.fillRect(
					offsetX + s.getX() * cellSize + cellSize * 0.16,
					offsetY + s.getY() * cellSize + cellSize * 0.16,
					cellSize * 0.68,
					cellSize * 0.68
				);
			}
		}

		for (int x = 0; x < mazeW; x++) {
			for (int y = 0; y < mazeH; y++) {
				drawCellIcon(
					gc, maze.getCell(x, y), x, y,
					offsetX + x * cellSize, offsetY + y * cellSize, cellSize, time,
					mysteryOpenStartMs, mysteryOpenGx, mysteryOpenGy,
					bombTouchStartMs, bombTouchGx, bombTouchGy
				);
			}
		}

		// Draw explosion animations for bombs that were removed this frame
		long now2 = System.currentTimeMillis();
		java.util.Iterator<Map.Entry<String, Long>> bit = BOMB_EXPLODE_TS.entrySet().iterator();
		while (bit.hasNext()) {
			Map.Entry<String, Long> e = bit.next();
			long start = e.getValue();
			long elapsed = now2 - start;
			if (elapsed >= BOMB_HIT_TOTAL_MS) { bit.remove(); continue; }
			int imgIdx = elapsed < BOMB_HIT_FRAME_MS ? 1 : 2;
			String[] parts = e.getKey().split(",");
			int bx = Integer.parseInt(parts[0]);
			int by = Integer.parseInt(parts[1]);
			double bxp = offsetX + bx * cellSize;
			double byp = offsetY + by * cellSize;
			drawBombPngFrame(gc, bxp, byp, cellSize, imgIdx);
		}

		if (!Double.isNaN(robotGridX) && !Double.isNaN(robotGridY)) {
			// determine if walking or idle based on fractional position
			boolean isMoving = (robotGridX % 1.0 != 0) || (robotGridY % 1.0 != 0);
			drawRobot(gc, offsetX + robotGridX * cellSize, offsetY + robotGridY * cellSize, cellSize, time, isMoving);
		}
	}

	private static void drawCellIcon(
		GraphicsContext gc, CellType type, int gx, int gy, double x, double y, double size, long time,
		long mysteryOpenStartMs, int mysteryOpenGx, int mysteryOpenGy,
		long bombTouchStartMs, int bombTouchGx, int bombTouchGy
	) {
		switch (type) {
			case BOMB -> {
				int idx = 0;
				if (bombTouchStartMs > 0 && gx == bombTouchGx && gy == bombTouchGy) {
					long elapsed = System.currentTimeMillis() - bombTouchStartMs;
					if (elapsed < BOMB_HIT_FRAME_MS) {
						idx = 1;
					} else if (elapsed < BOMB_HIT_TOTAL_MS) {
						idx = 2;
					}
				}
				if (BOMB_FRAMES[idx] != null && !BOMB_FRAMES[idx].isError()) {
					gc.drawImage(BOMB_FRAMES[idx], x + size * 0.10, y + size * 0.10, size * 0.80, size * 0.80);
				} else {
					drawBomb(gc, x, y, size);
				}
			}
			case ITEM -> drawItem(gc, x, y, size, gx, gy, mysteryOpenStartMs, mysteryOpenGx, mysteryOpenGy);
			case GOAL -> drawGoal(gc, x, y, size, time);
			case START -> drawStart(gc, x, y, size);
			default -> {
			}
		}
	}

	/** @param imageIndex 1 = Bomb_2, 2 = Bomb_3 (0-based array index) */
	private static void drawBombPngFrame(GraphicsContext gc, double x, double y, double size, int imageIndex) {
		if (imageIndex < 0 || imageIndex >= BOMB_FRAMES.length) {
			drawBomb(gc, x, y, size);
			return;
		}
		Image img = BOMB_FRAMES[imageIndex];
		if (img != null && !img.isError()) {
			gc.drawImage(img, x + size * 0.10, y + size * 0.10, size * 0.80, size * 0.80);
			return;
		}
		drawBomb(gc, x, y, size);
	}

	private static void drawBomb(GraphicsContext gc, double x, double y, double size) {
		if (STOP_IMAGE != null && !STOP_IMAGE.isError()) {
			gc.drawImage(STOP_IMAGE, x + size * 0.10, y + size * 0.10, size * 0.80, size * 0.80);
			return;
		}

		double cx = x + size * 0.5;
		double cy = y + size * 0.52;
		double r = size * 0.2;

		gc.setFill(Color.web("#401A25"));
		gc.fillOval(cx - r, cy - r, r * 2, r * 2);
		gc.setStroke(Color.web("#FF8DA6"));
		gc.setLineWidth(Math.max(1.0, size * 0.05));
		gc.strokeLine(cx, cy - r, cx + r * 0.9, cy - r * 1.5);
		gc.setFill(Color.web("#FFC857"));
		gc.fillOval(cx + r * 0.8, cy - r * 1.65, size * 0.08, size * 0.08);
	}

	private static void drawItem(
		GraphicsContext gc, double x, double y, double size,
		int gx, int gy, long mysteryOpenStartMs, int mysteryOpenGx, int mysteryOpenGy
	) {
		if (MYSTERY_FRAMES[0] != null && !MYSTERY_FRAMES[0].isError()) {
			int idx = 0;
			if (mysteryOpenStartMs > 0 && gx == mysteryOpenGx && gy == mysteryOpenGy) {
				long elapsed = System.currentTimeMillis() - mysteryOpenStartMs;
				if (elapsed < MYSTERY_OPEN_FRAME_MS) {
					idx = 1;
				} else if (elapsed < MYSTERY_OPEN_TOTAL_MS) {
					idx = 2;
				}
			}
			Image fr = MYSTERY_FRAMES[idx];
			if (fr != null && !fr.isError()) {
				gc.drawImage(fr, x + size * 0.10, y + size * 0.10, size * 0.80, size * 0.80);
				return;
			}
		}
		if (ITEM_IMAGE != null && !ITEM_IMAGE.isError()) {
			gc.drawImage(ITEM_IMAGE, x + size * 0.10, y + size * 0.10, size * 0.80, size * 0.80);
			return;
		}

		double pad = size * 0.24;
		double[] xs = {
			x + size * 0.5,
			x + size - pad,
			x + size * 0.5,
			x + pad
		};
		double[] ys = {
			y + pad,
			y + size * 0.5,
			y + size - pad,
			y + size * 0.5
		};
		gc.setFill(Color.web("#B7FFE4"));
		gc.fillPolygon(xs, ys, 4);
		gc.setStroke(Color.web("#2BD99F"));
		gc.setLineWidth(Math.max(1.0, size * 0.05));
		gc.strokePolygon(xs, ys, 4);
	}

	private static void drawGoal(GraphicsContext gc, double x, double y, double size, long time) {
		// Animated finish line
		int frame = (int) ((time / 150) % 3);
		Image goalImg = FINISH_LINE[frame];
		if (goalImg != null && !goalImg.isError()) {
			// Center the finish line
			gc.drawImage(goalImg, x + size * 0.05, y + size * 0.05, size * 0.90, size * 0.90);
			return;
		}

		double poleX = x + size * 0.36;
		double poleY = y + size * 0.22;
		double poleH = size * 0.56;
		gc.setStroke(Color.web("#FFE08A"));
		gc.setLineWidth(Math.max(1.1, size * 0.06));
		gc.strokeLine(poleX, poleY, poleX, poleY + poleH);

		gc.setFill(Color.web("#FFD166"));
		double[] fx = {poleX, poleX + size * 0.34, poleX};
		double[] fy = {poleY, poleY + size * 0.14, poleY + size * 0.28};
		gc.fillPolygon(fx, fy, 3);
	}

	private static void drawStart(GraphicsContext gc, double x, double y, double size) {
		double pad = size * 0.24;
		gc.setStroke(Color.web("#A7C8FF"));
		gc.setLineWidth(Math.max(1.0, size * 0.05));
		gc.strokeRect(x + pad, y + pad, size - pad * 2, size - pad * 2);
		gc.setFill(Color.web("#D6E6FF"));
		gc.setFont(javafx.scene.text.Font.font(Math.max(9, size * 0.42)));
		gc.fillText("S", x + size * 0.43, y + size * 0.65);
	}

	private static void drawRobot(GraphicsContext gc, double x, double y, double size, long time, boolean isMoving) {
		Image duckFrame = null;
		
		if (isMoving) {
			int frame = (int) ((time / 120) % 4);
			duckFrame = DUCK_WALK[frame];
		} else {
			int frame = (int) ((time / 200) % 4);
			duckFrame = DUCK_IDLE[frame];
		}
		
		if (duckFrame != null && !duckFrame.isError()) {
			// make the duck sprite fill the cell roughly
			gc.drawImage(duckFrame, x, y - size * 0.10, size, size * 1.10);
			return;
		}

		double bodyPad = size * 0.18;
		double bodyW = size - bodyPad * 2;
		double bodyH = size * 0.55;
		double bodyX = x + bodyPad;
		double bodyY = y + size * 0.24;

		gc.setFill(Color.web("#2A4C7A"));
		gc.fillRoundRect(bodyX, bodyY, bodyW, bodyH, size * 0.18, size * 0.18);
		gc.setStroke(ROBOT);
		gc.setLineWidth(Math.max(1.0, size * 0.05));
		gc.strokeRoundRect(bodyX, bodyY, bodyW, bodyH, size * 0.18, size * 0.18);

		gc.setFill(Color.web("#9EDCFF"));
		gc.fillOval(x + size * 0.32, y + size * 0.38, size * 0.12, size * 0.12);
		gc.fillOval(x + size * 0.56, y + size * 0.38, size * 0.12, size * 0.12);
	}

	private static Color colorFor(CellType type) {
		return switch (type) {
			case WALL -> WALL;
			case ITEM -> ITEM;
			case BOMB -> BOMB;
			case START -> START;
			case GOAL -> GOAL;
			case EMPTY -> EMPTY;
		};
	}

	private static Image loadImage(String resourcePath) {
		try {
			java.io.InputStream stream = MazeRenderer.class.getResourceAsStream(resourcePath);
			if (stream == null) {
				return null;
			}
			return new Image(stream);
		} catch (Exception ex) {
			return null;
		}
	}
}
