package com.nhom_01.robot_pathfinding.core;

import com.nhom_01.robot_pathfinding.ai.BFS;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MazeGenerator {
    private static final Random random = new Random();

    public enum GameMode {
        PLAYER,
        BOT
    }

    private static java.util.List<int[]> currentSafePath = null;
    
    private static boolean isProtectedFromBomb(int x, int y) {
        if (currentSafePath == null) return false;
        for (int[] p : currentSafePath) {
            if (p[0] == x && p[1] == y) return true;
        }
        return false;
    }

    public static Maze generate(String difficulty) {
        return generate(difficulty, GameMode.PLAYER);
    }

    public static Maze generate(String difficulty, GameMode mode) {
        int width, height, bombCount, itemCount;
        int startingLives = 5;

        switch (difficulty.toUpperCase()) {
            case "EASY":
                width = 15; height = 11; bombCount = 3; itemCount = 3; break;
            case "MEDIUM":
                width = 25; height = 15; bombCount = 8; itemCount = 6; break;
            case "HARD":
                width = 35; height = 21; bombCount = 15; itemCount = 10; break;
            default:
                width = 21; height = 15; bombCount = 5; itemCount = 5; break;
        }

        Maze maze;
        int attempts = 0;
        BFS validator = new BFS();

        do {
            attempts++;
            maze = new Maze(width, height);

            // 2. Sinh duong di (Recursive Backtracking)
            carvePassagesFrom(1, 1, maze);

            // 3. Dat Start va Goal
            placeStartAndGoal(maze, startingLives);

            // 4. Get the guaranteed safe path BEFORE we punch holes and place random stuff
            currentSafePath = findPath(maze, maze.getStart().getX(), maze.getStart().getY(), maze.getGoal().getX(), maze.getGoal().getY());

            if (mode == GameMode.PLAYER || mode == GameMode.BOT) {
                placeEntities(maze, CellType.BOMB, bombCount);
                placeEntities(maze, CellType.ITEM, itemCount);
                openRandomWalls(maze, (width * height) / 15);
                placeBombsOnMainPath(maze, startingLives);
                placeBombInDeadEnds(maze, bombCount / 3);
                placeEntities(maze, CellType.BOMB, bombCount / 3);
                placeEntities(maze, CellType.ITEM, itemCount);
            }
            
            currentSafePath = null;

            // Final Validation: Use BFS to ensure it's actually survivable with current bombs
            SearchResult result = validator.findPath(maze, maze.getStart(), maze.getGoal());
            if (!result.getPath().isEmpty()) {
                break; // Found a valid path!
            }
        } while (attempts < 20);

        return maze;
    }
    private static List<int[]> findPath(Maze maze, int sx, int sy, int gx, int gy) {

        int w = maze.getWidth();
        int h = maze.getHeight();

        boolean[][] visited = new boolean[w][h];
        int[][] parentX = new int[w][h];
        int[][] parentY = new int[w][h];

        List<int[]> queue = new ArrayList<>();
        queue.add(new int[]{sx, sy});
        visited[sx][sy] = true;

        int head = 0;

        int[][] dirs = {{1,0},{-1,0},{0,1},{0,-1}};

        while (head < queue.size()) {

            int[] cur = queue.get(head++);
            int x = cur[0];
            int y = cur[1];

            if (x == gx && y == gy) break;

            for (int[] d : dirs) {

                int nx = x + d[0];
                int ny = y + d[1];

                if (!visited[nx][ny] && maze.getCell(nx, ny) != CellType.WALL) {

                    visited[nx][ny] = true;

                    parentX[nx][ny] = x;
                    parentY[nx][ny] = y;

                    queue.add(new int[]{nx, ny});
                }
            }
        }

        List<int[]> path = new ArrayList<>();

        int x = gx;
        int y = gy;

        while (!(x == sx && y == sy)) {

            path.add(new int[]{x, y});

            int px = parentX[x][y];
            int py = parentY[x][y];

            x = px;
            y = py;
        }

        Collections.reverse(path);
        return path;
    }
    private static void placeBombsOnMainPath(Maze maze, int lives) {

        State start = maze.getStart();
        State goal = maze.getGoal();

        List<int[]> path = findPath(
                maze,
                start.getX(),
                start.getY(),
                goal.getX(),
                goal.getY()
        );

        if (path.size() < 4) return;

        boolean addBombs = random.nextBoolean();

        if (!addBombs) return;

        int maxSurvivable = Math.max(1, lives - 1);
        int bombCount = random.nextInt(maxSurvivable) + 1;
        if (bombCount >= lives) bombCount = lives - 1;

        Collections.shuffle(path, random);

        int placed = 0;

        for (int[] cell : path) {

            int x = cell[0];
            int y = cell[1];

            if (maze.getCell(x, y) == CellType.EMPTY) {

                maze.setCell(x, y, CellType.BOMB);

                placed++;

                if (placed >= bombCount)
                    return;
            }
        }
    }

    private static void placeStartAndGoal(Maze maze, int lives) {

        int width = maze.getWidth();
        int height = maze.getHeight();

        int side = random.nextInt(2);

        int sx = 1, sy = 1;
        int gx = 1, gy = 1;

        switch (side) {
            case 0: // LEFT
                sx = 1;
                sy = random.nextInt(height - 2) + 1;

                gx = width - 2;
                gy = random.nextInt(height - 2) + 1;
                break;

            case 1: // RIGHT
                sx = width - 2;
                sy = random.nextInt(height - 2) + 1;

                gx = 1;
                gy = random.nextInt(height - 2) + 1;
                break;

//            case 2: // TOP
//                sx = random.nextInt(width - 2) + 1;
//                sy = 1;
//
//                gx = random.nextInt(width - 2) + 1;
//                gy = height - 2;
//                break;
//
//            case 3: // BOTTOM
//                sx = random.nextInt(width - 2) + 1;
//                sy = height - 2;
//
//                gx = random.nextInt(width - 2) + 1;
//                gy = 1;
//                break;
        }

        maze.setCell(sx, sy, CellType.START);
        maze.setStart(new State(sx, sy, lives));

        maze.setCell(gx, gy, CellType.GOAL);
        maze.setGoal(new State(gx, gy, 0));
    }
    private static void carvePassagesFrom(int x, int y, Maze maze) {
        maze.setCell(x, y, CellType.EMPTY);

        int[][] directions = { {0, -2}, {2, 0}, {0, 2}, {-2, 0} }; // Up, Right, Down, Left
        List<int[]> dirs = new ArrayList<>();
        Collections.addAll(dirs, directions);
        Collections.shuffle(dirs, random);

        for (int[] dir : dirs) {
            int nx = x + dir[0];
            int ny = y + dir[1];

            if (nx > 0 && nx < maze.getWidth() - 1 && ny > 0 && ny < maze.getHeight() - 1) {
                if (maze.getCell(nx, ny) == CellType.WALL) {
                    maze.setCell(x + dir[0] / 2, y + dir[1] / 2, CellType.EMPTY); // Pha tuong o giua
                    carvePassagesFrom(nx, ny, maze);
                }
            }
        }
    }

    private static void placeEntities(Maze maze, CellType type, int count) {
        int placed = 0;
        int maxAttempts = count * 20; // To prevent infinite loops if maze is too small
        int attempts = 0;
        
        while (placed < count && attempts < maxAttempts) {
            attempts++;
            int x = random.nextInt(maze.getWidth() - 2) + 1;
            int y = random.nextInt(maze.getHeight() - 2) + 1;

            if (type == CellType.BOMB && isProtectedFromBomb(x, y)) continue;

            // Chi dat vao o trong, khong de len Start/Goal
            if (maze.getCell(x, y) == CellType.EMPTY && !isNearEntity(maze, x, y)) {
                maze.setCell(x, y, type);
                placed++;
            }
        }
    }
    private static boolean isNearEntity(Maze maze, int x, int y) {

        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {

                CellType t = maze.getCell(x + dx, y + dy);

                if (t == CellType.BOMB || t == CellType.ITEM) {
                    return true;
                }
            }
        }

        return false;
    }
    private static void placeBombInDeadEnds(Maze maze, int count) {

        int placed = 0;

        for (int x = 1; x < maze.getWidth() - 1; x++) {
            for (int y = 1; y < maze.getHeight() - 1; y++) {

                if (maze.getCell(x, y) != CellType.EMPTY)
                    continue;

                int paths = 0;

                if (maze.getCell(x + 1, y) != CellType.WALL) paths++;
                if (maze.getCell(x - 1, y) != CellType.WALL) paths++;
                if (maze.getCell(x, y + 1) != CellType.WALL) paths++;
                if (maze.getCell(x, y - 1) != CellType.WALL) paths++;

                if (paths == 1) { // dead end
                    if (!isProtectedFromBomb(x, y)) {
                        maze.setCell(x, y, CellType.BOMB);
                        placed++;
                    }

                    if (placed >= count)
                        return;
                }
            }
        }
    }
    private static void openRandomWalls(Maze maze, int count) {
        int opened = 0;
        while (opened < count) {
            int x = random.nextInt(maze.getWidth() - 2) + 1;
            int y = random.nextInt(maze.getHeight() - 2) + 1;
            if (maze.getCell(x, y) == CellType.WALL) {
                maze.setCell(x, y, CellType.EMPTY);
                opened++;
            }
        }
    }
}