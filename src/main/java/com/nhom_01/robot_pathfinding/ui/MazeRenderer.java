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
import javafx.scene.text.FontWeight;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;

public class MazeRenderer {

	/** Sprite heading; order matches {@code duck_devil_<name>_1-removebg-preview.png} in {@link #DUCK_FRAMES}. */
	public enum DuckFacing {
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	public static DuckFacing facingFromGridDelta(int dx, int dy) {
		if (dx > 0) {
			return DuckFacing.RIGHT;
		}
		if (dx < 0) {
			return DuckFacing.LEFT;
		}
		if (dy > 0) {
			return DuckFacing.DOWN;
		}
		if (dy < 0) {
			return DuckFacing.UP;
		}
		return DuckFacing.RIGHT;
	}

	/** Visible time per mystery-box frame (2 and 3) after touch; frame 1 is idle. */
	public static final long MYSTERY_OPEN_FRAME_MS = 280L;
	public static final long MYSTERY_OPEN_TOTAL_MS = MYSTERY_OPEN_FRAME_MS * 2;

	/** Visible time per bomb hit frame (2 and 3); frame 1 is idle on the map. */
	public static final long BOMB_HIT_FRAME_MS = 280L;
	public static final long BOMB_HIT_TOTAL_MS = BOMB_HIT_FRAME_MS * 2;

	/** Visible time per teleport frame (1, 2, 3). */
	public static final long TELEPORT_FRAME_MS = 150L;
	/** Increased to handle 5-frame sequence: 3 at start, 2 at target. */
	public static final long TELEPORT_TOTAL_MS = TELEPORT_FRAME_MS * 5;

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

	// Pre-load duck frames for both selection types
	private static final Image[] YELLOW_DUCK_FRAMES = new Image[4];
	private static final Image[] PURPLE_DUCK_FRAMES = new Image[4];
	private static final Image DUCK_HURT = loadImage("/image/pixel_animation/duck_hurt.png");
	
	private static final Image GRASS_1 = loadImage("/image/vit/Grass.png");
	private static final Image WATER = loadImage("/image/pixel_animation/water.gif");
	/** Bomb_1 = idle on map; Bomb_2/3 = touch animation (per-frame PNGs). */
	private static final Image[] BOMB_FRAMES = new Image[4];
	
	private static final Image[] FINISH_LINE = new Image[3];

	private static final Image STOP_IMAGE = loadImage("/image/pixel_animation/Stop.png");
	private static final Image ITEM_IMAGE = loadImage("/image/pixel_animation/mysterybox_fr1.png");
	private static final Image[] MYSTERY_FRAMES = new Image[3];
	private static final Image FLAG_IMAGE = loadImage("/image/vit/Flag.png");
	private static final Image BUBBLE1_IMAGE = loadImage("/image/pixel_animation/bubble1.png");
	private static final Image BUBBLE2_IMAGE = loadImage("/image/pixel_animation/bubble2.png");
	private static final Image HEART_IMAGE = loadImage("/image/pixel_animation/heart.png");
	private static final Image FREEZE_BOMB_IMAGE = loadImage("/image/pixel_animation/freeze_bomb.png");
	private static final Image[] TELEPORT_FRAMES = new Image[3];

	static {
		String[] names = { "up", "down", "left", "right" };
		for (int d = 0; d < 4; d++) {
			PURPLE_DUCK_FRAMES[d] = loadImage(String.format(
				"/image/pixel_animation/duck_devil_%s_1-removebg-preview.png", names[d]));
			YELLOW_DUCK_FRAMES[d] = loadImage(String.format(
				"/image/pixel_animation/duck_%s_1.png", names[d]));
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
		for (int i = 0; i < 3; i++) {
			TELEPORT_FRAMES[i] = loadImage("/image/pixel_animation/teleport_" + (i + 1) + ".png");
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
		render(gc, maze, explored, path, robotGridX, robotGridY, width, height,
			0L, -1, -1, 0L, -1, -1, DuckFacing.RIGHT, true, false, false, 1.0,
			false, -1L, false, 0L, -1, -1, -1, -1, null, new RenderVfx(0L, 0L, 0, 0L, 0L));
	}

	public static void render(
		GraphicsContext gc,
		Maze maze,
		List<State> explored,
		List<State> path,
		double robotGridX,
		double robotGridY,
		double width,
		double height,
		DuckFacing duckFacing,
		boolean duckIntroRightIdle
	) {
		render(gc, maze, explored, path, robotGridX, robotGridY, width, height,
			0L, -1, -1, 0L, -1, -1, duckFacing, duckIntroRightIdle, false, false, 1.0,
			false, -1L, false, 0L, -1, -1, -1, -1, null, new RenderVfx(0L, 0L, 0, 0L, 0L));
	}

	/**
	 * @param mysteryOpenStartMs {@code System.currentTimeMillis()} when pickup started, or {@code 0} if none
	 * @param mysteryOpenGx      grid column of opening box (ignored if start is 0)
	 * @param mysteryOpenGy      grid row of opening box
	 * @param duckFacing         current heading (after first move, idle uses this)
	 * @param duckIntroRightIdle when true and duck is idle, force right-facing idle (start of level)
	 */
	/** Helper for VFX timers/values to keep render signature manageable. */
	public record RenderVfx(
		long lifeUntil,
		long scoreUntil,
		int scoreValue,
		long timeUntil,
		long iFramesUntil
	) {}

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
		int bombTouchGy,
		DuckFacing duckFacing,
		boolean duckIntroRightIdle,
		boolean freezeTimeActive,
		boolean bombDetectorActive,
		double speedMultiplier,
		boolean shieldVisible,
		long activeTimerMs,
		boolean visionBoostActive,
		long teleportStartMs,
		int teleportGx,
		int teleportGy,
		int teleportDestX,
		int teleportDestY,
		List<FlyingItem> flyingItems,
		RenderVfx vfx
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

		if (visionBoostActive) {
			drawSonarWave(gc, offsetX, offsetY, robotGridX, robotGridY, cellSize, width, height);
		}

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

		// Convert path to Set for efficient lookup during exploration/path rendering
		Set<String> pathSet = new HashSet<>();
		if (path != null) {
			for (State s : path) pathSet.add(s.getX() + "," + s.getY());
		}

		if (explored != null) {
			gc.setFill(EXPLORED);
			for (State s : explored) {
				double cellX = offsetX + s.getX() * cellSize;
				double cellY = offsetY + s.getY() * cellSize;
				gc.fillRect(cellX, cellY, cellSize, cellSize);

				if (FLAG_IMAGE != null && !FLAG_IMAGE.isError()) {
					CellType cellType = maze.getCell(s.getX(), s.getY());
					if (cellType == CellType.EMPTY || cellType == CellType.START || cellType == CellType.GOAL) {
						boolean isFinalPath = pathSet.contains(s.getX() + "," + s.getY());
						if (!isFinalPath) {
							// Area explored by bot but not on final path -> dimmed flag
							gc.setGlobalAlpha(0.40);
							gc.drawImage(FLAG_IMAGE, cellX + cellSize * 0.14, cellY + cellSize * 0.14, cellSize * 0.72, cellSize * 0.72);
							gc.setGlobalAlpha(1.0);
						}
					}
				}
			}
		}

		if (path != null) {
			gc.setFill(PATH);
			for (State s : path) {
				double cellX = offsetX + s.getX() * cellSize;
				double cellY = offsetY + s.getY() * cellSize;
				
				// Keep the tint "hover"
				gc.fillRect(
					cellX + cellSize * 0.16,
					cellY + cellSize * 0.16,
					cellSize * 0.68,
					cellSize * 0.68
				);

				// Draw final path flags with full opacity (only on empty/start/goal)
				if (FLAG_IMAGE != null && !FLAG_IMAGE.isError()) {
					CellType cellType = maze.getCell(s.getX(), s.getY());
					if (cellType == CellType.EMPTY || cellType == CellType.START || cellType == CellType.GOAL) {
						gc.drawImage(FLAG_IMAGE, cellX + cellSize * 0.14, cellY + cellSize * 0.14, cellSize * 0.72, cellSize * 0.72);
					}
				}
			}
		}

		for (int x = 0; x < mazeW; x++) {
			for (int y = 0; y < mazeH; y++) {
				drawCellIcon(
					gc, maze.getCell(x, y), x, y,
					offsetX + x * cellSize, offsetY + y * cellSize, cellSize, time,
					mysteryOpenStartMs, mysteryOpenGx, mysteryOpenGy,
					bombTouchStartMs, bombTouchGx, bombTouchGy,
					freezeTimeActive, bombDetectorActive, visionBoostActive
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

		// --- Draw Teleport Animation if active ---
		long teleportTotal = TELEPORT_TOTAL_MS; // 1200ms
		long teleportHalf = teleportTotal / 2;     // 600ms
		int teleportFrame;
		if (teleportStartMs > 0) {
			long elapsed = now - teleportStartMs;
			if (elapsed < teleportHalf) {
				// Departure (0-50%): Portal grows 1 -> 2 -> 3
				teleportFrame = 1 + (int)(elapsed / (teleportHalf / 3));
				if (teleportFrame > 3) teleportFrame = 3;
				gc.drawImage(TELEPORT_FRAMES[teleportFrame - 1],
					offsetX + teleportGx * cellSize, offsetY + teleportGy * cellSize, cellSize, cellSize);
			} else {
				// Arrival (50-100%): Portal shrinks 2 -> 1
				long arrivalElapsed = elapsed - teleportHalf;
				teleportFrame = 2 - (int)(arrivalElapsed / (teleportHalf / 2));
				if (teleportFrame > 0) {
					gc.drawImage(TELEPORT_FRAMES[teleportFrame - 1],
						offsetX + teleportDestX * cellSize, offsetY + teleportDestY * cellSize, cellSize, cellSize);
				}
			}
		}

		// Draw flying items (Sonar Radar magnetic pull)
		if (flyingItems != null && !flyingItems.isEmpty()) {
			for (FlyingItem fi : flyingItems) {
				gc.drawImage(ITEM_IMAGE, 
					offsetX + fi.currentX() * cellSize, 
					offsetY + fi.currentY() * cellSize, 
					cellSize * 0.85, cellSize * 0.85);
			}
		}

		if (!Double.isNaN(robotGridX) && !Double.isNaN(robotGridY)) {
			// --- Draw VFX (Score, Time) ---
			// Heart (+life) is already handled by renderFloatingMessage in drawRobot
			if (now < vfx.scoreUntil()) {
				gc.setFill(Color.GOLD);
				gc.setFont(AppFonts.vt323(20));
				gc.fillText("+" + vfx.scoreValue(), offsetX + robotGridX * cellSize + cellSize * 0.5, offsetY + robotGridY * cellSize);
			}
			if (now < vfx.timeUntil()) {
				gc.setFill(Color.AQUA);
				gc.setFont(AppFonts.vt323(20));
				gc.fillText("+15s", offsetX + robotGridX * cellSize, offsetY + robotGridY * cellSize - 10);
			}

			// determine if walking or idle based on fractional position
			boolean isMoving = (robotGridX % 1.0 != 0) || (robotGridY % 1.0 != 0);

			boolean isTeleporting = false;
			if (teleportStartMs > 0) {
				long elapsedTP = now - teleportStartMs;
				if (elapsedTP < TELEPORT_TOTAL_MS) {
					isTeleporting = true;
				}
			}

			drawRobot(
				gc, offsetX + robotGridX * cellSize, offsetY + robotGridY * cellSize, cellSize,
				isMoving, duckFacing, duckIntroRightIdle, shieldVisible, activeTimerMs, speedMultiplier, isTeleporting,
				vfx, maze.getWidth()
			);
		}

		// Draw speed-based vignette/border
		if (speedMultiplier != 1.0) {
			gc.setLineWidth(5);
			if (speedMultiplier < 1.0) {
				gc.setStroke(Color.web("#00E5FF", 0.4)); // Speed boost (Cyan)
			} else {
				gc.setStroke(Color.web("#546E7A", 0.4)); // SLOW mode (Slate Blue)
			}
			gc.strokeRect(offsetX + 2.5, offsetY + 2.5, drawW - 5, drawH - 5);
		}
	}

	private static void drawCellIcon(
		GraphicsContext gc, CellType type, int gx, int gy, double x, double y, double size, long time,
		long mysteryOpenStartMs, int mysteryOpenGx, int mysteryOpenGy,
		long bombTouchStartMs, int bombTouchGx, int bombTouchGy,
		boolean freezeTimeActive, boolean bombDetectorActive, boolean visionBoostActive
	) {
		switch (type) {
			case BOMB -> {
				if (freezeTimeActive) {
					if (FREEZE_BOMB_IMAGE != null && !FREEZE_BOMB_IMAGE.isError()) {
						gc.drawImage(FREEZE_BOMB_IMAGE, x, y, size, size);
						return;
					}
				}

				int idx = 0;
				if (bombTouchStartMs > 0 && gx == bombTouchGx && gy == bombTouchGy) {
					long elapsed = System.currentTimeMillis() - bombTouchStartMs;
					if (elapsed < BOMB_HIT_FRAME_MS) {
						idx = 1;
					} else if (elapsed < BOMB_HIT_TOTAL_MS) {
						idx = 2;
					}
				}
				
				double drawSize = size;
				if (bombDetectorActive && idx == 0) {
					// Pulsate the bomb scale for detector clarity
					double pulse = Math.sin(System.currentTimeMillis() * 0.008) * 0.12;
					drawSize = size * (1.0 + pulse);
					x -= (drawSize - size) / 2;
					y -= (drawSize - size) / 2;
				}

				if (BOMB_FRAMES[idx] != null && !BOMB_FRAMES[idx].isError()) {
					gc.drawImage(BOMB_FRAMES[idx], x + drawSize * 0.10, y + drawSize * 0.10, drawSize * 0.80, drawSize * 0.80);
				} else {
					drawBomb(gc, x, y, drawSize);
				}
			}
			case ITEM -> {
				if (visionBoostActive) {
					double pulse = Math.abs(Math.sin(System.currentTimeMillis() * 0.005));
					gc.setGlobalAlpha(0.6 + 0.4 * pulse);
					javafx.scene.effect.DropShadow sonar = new javafx.scene.effect.DropShadow(20 * pulse, Color.CYAN);
					gc.setEffect(sonar);
				}
				drawItem(gc, x, y, size, gx, gy, mysteryOpenStartMs, mysteryOpenGx, mysteryOpenGy);
				if (visionBoostActive) {
					gc.setGlobalAlpha(1.0);
					gc.setEffect(null);
				}
			}
			case GOAL -> {
				if (visionBoostActive) {
					double pulse = Math.abs(Math.sin(System.currentTimeMillis() * 0.007));
					javafx.scene.effect.DropShadow sonar = new javafx.scene.effect.DropShadow(30 * pulse, Color.GOLD);
					gc.setEffect(sonar);
				}
				drawGoal(gc, x, y, size, time);
				if (visionBoostActive) gc.setEffect(null);
			}
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
		gc.setFont(AppFonts.vt323(Math.max(9, size * 0.42)));
		gc.fillText("S", x + size * 0.43, y + size * 0.65);
	}

	private static void drawRobot(
		GraphicsContext gc, double x, double y, double size,
		boolean isMoving, DuckFacing facing, boolean introRightIdle,
		boolean shieldVisible, long activeTimerMs, double speedMultiplier,
		boolean isTeleporting, 
		RenderVfx vfx, 
		int mazeW
	) {
		if (isTeleporting) return; // Skip duck rendering during teleport animation
		long now = System.currentTimeMillis();
		// IFRAMES FLASHING EFFECT: skip drawing some frames to flicker
		if (vfx != null && vfx.iFramesUntil() > now && (now / 100) % 2 == 0) return;
		DuckFacing useFacing = facing;
		if (!isMoving && introRightIdle) {
			useFacing = DuckFacing.RIGHT;
		}
		int dir = useFacing.ordinal();
		Image duckFrame = null;
		Image[] targetFrames = "YELLOW".equals(com.nhom_01.robot_pathfinding.core.PlayerProfile.getCurrentDuckType()) 
			? YELLOW_DUCK_FRAMES 
			: PURPLE_DUCK_FRAMES;

		if (dir >= 0 && dir < targetFrames.length) {
			duckFrame = targetFrames[dir];
		}

		if (duckFrame != null && !duckFrame.isError()) {
			boolean isYellow = "YELLOW".equals(com.nhom_01.robot_pathfinding.core.PlayerProfile.getCurrentDuckType());

			// Dynamic Scaling: adjust based on maze width to stay consistent across difficulties
			// Easy (15), Medium (25), Hard (35)
			// Reference width is 35 (Hard)
			double mazeResRatio = Math.max(0.4, Math.min(1.0, mazeW / 35.0));
			
			// Base multipliers that scale linearly with the ratio
			double scaleW = 1.15 + (0.65 * mazeResRatio); // ~1.42 on Easy, ~1.61 on Medium, 1.8 on Hard
			double scaleH = 1.35 + (0.60 * mazeResRatio); // ~1.60 on Easy, ~1.78 on Medium, 1.95 on Hard

			// Boost if facing Lên/Xuống (Up/Down)
			if (useFacing == DuckFacing.UP || useFacing == DuckFacing.DOWN) {
				if (isYellow) {
					// Yellow Duck vertical frames are visually short and need more boost
					scaleH = 1.60 + (0.70 * mazeResRatio); // ~1.90 on Easy, ~2.10 on Medium, 2.3 on Hard
					scaleW = 1.25 + (0.85 * mazeResRatio); // ~1.61 on Easy, ~1.85 on Medium, 2.1 on Hard
				} else {
					// Purple Duck vertical frames only need a subtler boost to match its Side views
					scaleH = 1.45 + (0.50 * mazeResRatio); // ~1.65 on Easy, ~1.85 on Medium, 1.95 on Hard
					scaleW = 1.20 + (0.60 * mazeResRatio); // ~1.44 on Easy, ~1.65 on Medium, 1.80 on Hard
				}
			}

			// Subtle extra overall boost for Yellow Duck to match Purple Duck's visual weight
			if (isYellow) {
				scaleW *= 1.08;
				scaleH *= 1.08;
			}

			double duckW = size * scaleW;
			double duckH = size * scaleH;
			double duckX = x - (duckW - size) / 2;
			
			// Base vertical offset to "sink" the duck more into the current cell (reduces floating)
			double sinkOffset = size * 0.12; 
			// Extra push for DOWN view to make beak touch/overlap the wall below
			if (useFacing == DuckFacing.DOWN) {
				// Yellow duck needs a very aggressive sink due to its sprite proportions
				sinkOffset += isYellow ? (size * 0.28) : (size * 0.10);
			}
			// Slight push up for UP to keep feet grounded in the current cell
			if (useFacing == DuckFacing.UP) {
				sinkOffset -= size * 0.05;
			}

			double duckY = y - (duckH - size) + sinkOffset;

			// 1. Draw the actual Duck first
			gc.drawImage(duckFrame, duckX, duckY, duckW, duckH);

			// 2. Add speed trail if speed is high (re-drawn over duck but with transparency)
			if (speedMultiplier > 1.1) {
				gc.setGlobalAlpha(0.3);
				double trailOff = size * 0.15;
				gc.drawImage(duckFrame, duckX - trailOff, duckY + trailOff * 0.5, duckW, duckH);
				gc.setGlobalAlpha(1.0);
			}

			// 3. Draw Shield/Bubble OVER the duck
			if (shieldVisible) {
				long switchTime = (activeTimerMs > 0 && activeTimerMs < 2000) ? 100 : 300;
				boolean frameIndex = (now / switchTime) % 2 == 0;
				Image bubble = frameIndex ? BUBBLE1_IMAGE : BUBBLE2_IMAGE;
				
				if (bubble != null && !bubble.isError()) {
					double bSize = duckW * 1.7;
					gc.drawImage(bubble, duckX - (bSize - duckW) / 2, duckY - (bSize - duckH) / 2 + size * 0.1, bSize, bSize);
				}
			}

			// 4. Draw Countdown Timer
			if (activeTimerMs > 0) {
				int secs = (int)((activeTimerMs + 999) / 1000);
				gc.setFont(AppFonts.vt323(Math.max(12, size * 0.5)));
				gc.setFill(Color.YELLOW);
				gc.setStroke(Color.BLACK);
				gc.setLineWidth(1);
				String txt = secs + "s";
				gc.strokeText(txt, duckX + duckW / 2 - size * 0.2, duckY - size * 0.1);
				gc.fillText(txt, duckX + duckW / 2 - size * 0.2, duckY - size * 0.1);
			}

			// 5. Draw Floating VFX Messages (+Heart, +Score, +15s)
			long cur = System.currentTimeMillis();
			double vfxHSize = size * 0.7;
			renderFloatingMessage(gc, "heart", vfx.lifeUntil(), -1, cur, duckX, duckY, duckW, vfxHSize, size);
			renderFloatingMessage(gc, "score", vfx.scoreUntil(), vfx.scoreValue(), cur, duckX, duckY, duckW, vfxHSize, size);
			renderFloatingMessage(gc, "time", vfx.timeUntil(), 15, cur, duckX, duckY, duckW, vfxHSize, size);

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

	private static void renderFloatingMessage(
		GraphicsContext gc, String type, long untilMs, int value, long now,
		double duckX, double duckY, double duckW, double hSize, double size
	) {
		long remaining = untilMs - now;
		if (remaining <= 0) return;

		double progress = 1.0 - (remaining / 1000.0); // 0.0 -> 1.0
		double alpha = 1.0 - progress;
		double floatY = progress * size * 1.5; // Slightly more float for text

		gc.save();
		gc.setGlobalAlpha(alpha);

		double centerX = duckX + duckW / 2;
		double vfxY = duckY - hSize * 0.5 - floatY;

		gc.setStroke(Color.BLACK);
		gc.setLineWidth(1);

		if ("heart".equals(type)) {
			gc.setFont(AppFonts.vt323(size * 0.6));
			gc.setFill(Color.WHITE);
			String plus = "+";
			double plusW = size * 0.2;
			gc.strokeText(plus, centerX - plusW - hSize/2, vfxY + hSize * 0.7);
			gc.fillText(plus, centerX - plusW - hSize/2, vfxY + hSize * 0.7);
			gc.drawImage(HEART_IMAGE, centerX - hSize/2 + plusW/2, vfxY, hSize, hSize);
		} else if ("score".equals(type)) {
			gc.setFont(AppFonts.vt323(size * 0.7));
			gc.setFill(Color.GOLD);
			String txt = "+" + value;
			gc.strokeText(txt, centerX - size * 0.2, vfxY + size * 0.3);
			gc.fillText(txt, centerX - size * 0.2, vfxY + size * 0.3);
		} else if ("time".equals(type)) {
			gc.setFont(AppFonts.vt323(size * 0.7));
			gc.setFill(Color.CYAN);
			String txt = "+" + value + "s";
			gc.strokeText(txt, centerX - size * 0.2, vfxY + size * 0.3);
			gc.fillText(txt, centerX - size * 0.2, vfxY + size * 0.3);
		}

		gc.restore();
	}

	public static void clearState() {
		LAST_BOMB_POS.clear();
		BOMB_EXPLODE_TS.clear();
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

	private static void drawSonarWave(GraphicsContext gc, double ox, double oy, double gx, double gy, double cellSize, double w, double h) {
		if (Double.isNaN(gx)) return;
		long time = System.currentTimeMillis();
		double centerX = ox + gx * cellSize + cellSize / 2;
		double centerY = oy + gy * cellSize + cellSize / 2;
		
		double duration = 2000.0;
		double progress = (time % (long)duration) / duration;
		double radius = progress * Math.max(w, h) * 1.2;
		
		gc.save();
		// Start colored, fade out as it expands
		gc.setStroke(Color.CYAN.deriveColor(0, 1, 1, 1.0 - progress));
		gc.setLineWidth(4.0);
		gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);
		gc.restore();
	}
}
