package com.nhom_01.robot_pathfinding.game;

import java.util.List;

import com.nhom_01.robot_pathfinding.ai.SearchAlgorithm;
import com.nhom_01.robot_pathfinding.core.SearchResult;
import com.nhom_01.robot_pathfinding.core.State;
import com.nhom_01.robot_pathfinding.core.Maze;
import com.nhom_01.robot_pathfinding.core.CellType;
public class GameEngine {
    private Maze maze;
    private State start;
    private State goal;
    private SearchAlgorithm algorithm;
    private RobotController robot;
    private GameState state;
    private int score;
    private int lives;
    private List<State> explored;

    public GameEngine(Maze maze, State start, State goal, SearchAlgorithm algorithm) {
        this.maze = maze;
        this.start = start;
        this.goal = goal;
        this.algorithm = algorithm;
        this.robot = new RobotController();
        this.robot.setPath(java.util.List.of(start));
        this.state = GameState.READY;
        this.score = 1000; // Diem khoi dau, se tru dan theo thoi gian/buoc di
        this.lives = Math.max(1, start.getLives());
    }

    // Them vao trong class GameEngine.java
//    public void updateManually() {
//        // Chi don gian la thong bao Engine dang hoat dong
//        this.state = GameState.MOVING;
//
//        // Kiem tra neu dam vao o Dich
//        State pos = robot.getCurrentPosition();
//        if (pos.getX() == goal.getX() && pos.getY() == goal.getY()) {
//            this.state = GameState.FINISHED;
//        }
//    }
    public void startSearch() {
        this.state = GameState.SEARCHING;
        SearchResult result = algorithm.findPath(maze, start, goal);

        if (result.getPath().isEmpty()) {
            this.state = GameState.NO_PATH; // 13. Lose detection
        } else {
            this.explored = result.getExplored(); // 14. AI Visualization data
            robot.setPath(result.getPath());
            this.state = GameState.MOVING;
        }
    }

    public void update() {
        if (state == GameState.MOVING) {
            boolean moved = robot.moveNext();

            // 16. Score system: Moi buoc di tru 10 diem
            score -= 10;

            // Kiem tra vat pham (Item) de cong diem
            State pos = robot.getCurrentPosition();
            if (maze.getCell(pos.getX(), pos.getY()) == CellType.ITEM) {
                score += 200;
                maze.setCell(pos.getX(), pos.getY(), CellType.EMPTY); // An roi thi mat
            } else if (maze.getCell(pos.getX(), pos.getY()) == CellType.BOMB) {
                lives--;
                score = Math.max(0, score - 120);
                maze.setCell(pos.getX(), pos.getY(), CellType.EMPTY);
                
                if (lives <= 0) {
                    this.state = GameState.NO_PATH; // NO_PATH dong vai tro la Game Over cho Bot
                    return;
                }
            }

            if (!moved || robot.isFinished()) {
                this.state = GameState.FINISHED; // Win detection
            }
        }
    }
    public List<State> getPath() {
        return (robot != null) ? robot.getPath() : null;
    }
    public State getRobotPosition() {
        return (robot != null) ? robot.getCurrentPosition() : null;
    }
    public int getScore() { return Math.max(0, score); }
    public int getLives() { return Math.max(0, lives); }
    public void setLives(int lives) { this.lives = lives; }
    public GameState getState() { return state; }
    public RobotController getRobot() { return robot; }
    public List<State> getExplored() { return explored; }
    public Maze getMaze() { return maze; }
    /* Khi nao tao GUI thi them doan nay vao (loop update de robot di chuyen tu dong)
    Timeline loop = new Timeline(
        new KeyFrame(Duration.millis(300), e -> {
            engine.update();
            drawMap();
        })
    );

    loop.setCycleCount(Animation.INDEFINITE);
    loop.play(); */
}
