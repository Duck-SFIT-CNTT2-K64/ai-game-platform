package com.nhom_01.robot_pathfinding.ui;

import com.nhom_01.robot_pathfinding.core.CellType;
import com.nhom_01.robot_pathfinding.core.Maze;
import com.nhom_01.robot_pathfinding.core.State;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;

import java.util.List;

public class MazeRenderer {

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

	private static final Image DUCK_IMAGE = loadImage("/image/vit/Duck.png");
	private static final Image GRASS_IMAGE = loadImage("/image/vit/Grass.png");
	private static final Image WATER_IMAGE = loadImage("/image/vit/Water.png");
	private static final Image GOAL_IMAGE = loadImage("/image/vit/FinishLine.png");
	private static final Image STOP_IMAGE = loadImage("/image/vit/Stop.png");
	private static final Image VIT_IMAGE = loadImage("/image/vit/Vit.png");
	private static final Image FLAG_IMAGE = loadImage("/image/vit/Flag.png");

	private MazeRenderer() {
	}

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
		render(gc, maze, explored, path, robotGridX, robotGridY, width, height);
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
		gc.setFill(BG);
		gc.fillRect(0, 0, width, height);

		int mazeW = maze.getWidth();
		int mazeH = maze.getHeight();
		double cellSize = Math.min(width / mazeW, height / mazeH);
		double drawW = mazeW * cellSize;
		double drawH = mazeH * cellSize;
		double offsetX = (width - drawW) / 2.0;
		double offsetY = (height - drawH) / 2.0;

		for (int x = 0; x < mazeW; x++) {
			for (int y = 0; y < mazeH; y++) {
				CellType type = maze.getCell(x, y);
				double px = offsetX + x * cellSize;
				double py = offsetY + y * cellSize;
				if (type == CellType.WALL) {
					if (GRASS_IMAGE != null && !GRASS_IMAGE.isError()) {
						gc.drawImage(GRASS_IMAGE, px + cellSize * 0.06, py + cellSize * 0.06, cellSize * 0.88, cellSize * 0.88);
					} else {
						gc.setFill(WALL);
						gc.fillRoundRect(px + cellSize * 0.06, py + cellSize * 0.06, cellSize * 0.88, cellSize * 0.88, cellSize * 0.24, cellSize * 0.24);
					}
					gc.setStroke(Color.web("#9BC157"));
					gc.setLineWidth(Math.max(0.8, cellSize * 0.06));
					gc.strokeRoundRect(px + cellSize * 0.06, py + cellSize * 0.06, cellSize * 0.88, cellSize * 0.88, cellSize * 0.24, cellSize * 0.24);
				} else {
					if (WATER_IMAGE != null && !WATER_IMAGE.isError()) {
						gc.drawImage(WATER_IMAGE, px, py, cellSize, cellSize);
					} else {
						gc.setFill(colorFor(type));
						gc.fillRect(px, py, cellSize, cellSize);
					}
				}
			}
		}

		gc.setStroke(Color.color(0.63, 0.91, 0.96, 0.18));
		gc.setLineWidth(0.5);
		for (int x = 0; x <= mazeW; x++) {
			double gx = offsetX + x * cellSize;
			gc.strokeLine(gx, offsetY, gx, offsetY + drawH);
		}
		for (int y = 0; y <= mazeH; y++) {
			double gy = offsetY + y * cellSize;
			gc.strokeLine(offsetX, gy, offsetX + drawW, gy);
		}

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
				drawCellIcon(gc, maze.getCell(x, y), offsetX + x * cellSize, offsetY + y * cellSize, cellSize);
			}
		}

		if (!Double.isNaN(robotGridX) && !Double.isNaN(robotGridY)) {
			drawRobot(gc, offsetX + robotGridX * cellSize, offsetY + robotGridY * cellSize, cellSize);
		}
	}

	private static void drawCellIcon(GraphicsContext gc, CellType type, double x, double y, double size) {
		switch (type) {
			case BOMB -> drawBomb(gc, x, y, size);
			case ITEM -> drawItem(gc, x, y, size);
			case GOAL -> drawGoal(gc, x, y, size);
			case START -> drawStart(gc, x, y, size);
			default -> {
			}
		}
	}

	private static void drawBomb(GraphicsContext gc, double x, double y, double size) {
		if (STOP_IMAGE != null && !STOP_IMAGE.isError()) {
			gc.drawImage(STOP_IMAGE, x + size * 0.15, y + size * 0.15, size * 0.70, size * 0.70);
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

	private static void drawItem(GraphicsContext gc, double x, double y, double size) {
		if (VIT_IMAGE != null && !VIT_IMAGE.isError()) {
			gc.drawImage(VIT_IMAGE, x + size * 0.06, y + size * 0.06, size * 0.88, size * 0.88);
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

	private static void drawGoal(GraphicsContext gc, double x, double y, double size) {
		if (GOAL_IMAGE != null && !GOAL_IMAGE.isError()) {
			gc.drawImage(GOAL_IMAGE, x + size * 0.02, y + size * 0.02, size * 0.96, size * 0.96);
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

	private static void drawRobot(GraphicsContext gc, double x, double y, double size) {
		if (DUCK_IMAGE != null && !DUCK_IMAGE.isError()) {
			gc.drawImage(DUCK_IMAGE, x + size * 0.03, y + size * 0.03, size * 0.94, size * 0.94);
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

		gc.setStroke(Color.web("#7BE3FF"));
		gc.strokeLine(x + size * 0.5, y + size * 0.16, x + size * 0.5, y + size * 0.24);
		gc.setFill(Color.web("#7BE3FF"));
		gc.fillOval(x + size * 0.47, y + size * 0.12, size * 0.06, size * 0.06);
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
