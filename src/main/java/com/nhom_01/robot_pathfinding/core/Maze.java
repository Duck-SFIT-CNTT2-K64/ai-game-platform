package com.nhom_01.robot_pathfinding.core;

public class Maze {
    private final int width;
    private final int height;
    private final CellType[][] grid;
    private State start;
    private State goal;

    public Maze(int width, int height) {
        this.width = width;
        this.height = height;
        this.grid = new CellType[width][height];

        // Mac dinh khoi tao toan bo la tuong
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                grid[x][y] = CellType.WALL;
            }
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public CellType getCell(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) {
            return CellType.WALL; // Ngoai vien luon coi la tuong
        }
        return grid[x][y];
    }

    public void setCell(int x, int y, CellType type) {
        if (x >= 0 && x < width && y >= 0 && y < height) {
            grid[x][y] = type;
        }
    }

    public State getStart() { return start; }
    public void setStart(State start) { this.start = start; }

    public State getGoal() { return goal; }
    public void setGoal(State goal) { this.goal = goal; }


}