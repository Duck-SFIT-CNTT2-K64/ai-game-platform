package com.nhom_01.robot_pathfinding.ui;

import com.nhom_01.robot_pathfinding.core.CellType;
import com.nhom_01.robot_pathfinding.core.Maze;
import com.nhom_01.robot_pathfinding.core.State;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.List;

public class MazeRenderer {

	private static final Color BG = Color.web("#08101A");
	private static final Color WALL = Color.web("#25374A");
	private static final Color EMPTY = Color.web("#142335");
	private static final Color ITEM = Color.web("#2BD99F");
	private static final Color BOMB = Color.web("#EE5A7A");
	private static final Color START = Color.web("#5EA5FF");
	private static final Color GOAL = Color.web("#FFD166");
	private static final Color EXPLORED = Color.color(0.56, 0.86, 1.0, 0.18);
	private static final Color PATH = Color.color(0.94, 0.96, 1.0, 0.28);
	private static final Color ROBOT = Color.web("#8BE9FD");

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
				gc.setFill(colorFor(type));
				gc.fillRect(offsetX + x * cellSize, offsetY + y * cellSize, cellSize, cellSize);
			}
		}

		gc.setStroke(Color.color(0.5, 0.8, 1.0, 0.10));
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
				gc.fillRect(offsetX + s.getX() * cellSize, offsetY + s.getY() * cellSize, cellSize, cellSize);
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

		if (robotPosition != null) {
			drawRobot(gc, offsetX + robotPosition.getX() * cellSize, offsetY + robotPosition.getY() * cellSize, cellSize);
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
}
