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
    private List<State> explored;

    public GameEngine(Maze maze, State start, State goal, SearchAlgorithm algorithm) {
        this.maze = maze;
        this.start = start;
        this.goal = goal;
        this.algorithm = algorithm;
        this.robot = new RobotController();
        this.robot.setPath(java.util.List.of(start));
        this.state = GameState.READY;
        this.score = 1000; // Điểm khởi đầu, sẽ trừ dần theo thời gian/bước đi
    }

    // Thêm vào trong class GameEngine.java
//    public void updateManually() {
//        // Chỉ đơn giản là thông báo Engine đang hoạt động
//        this.state = GameState.MOVING;
//
//        // Kiểm tra nếu dẫm vào ô Đích
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

            // 16. Score system: Mỗi bước đi trừ 10 điểm
            score -= 10;

            // Kiểm tra vật phẩm (Item) để cộng điểm
            State pos = robot.getCurrentPosition();
            if (maze.getCell(pos.getX(), pos.getY()) == CellType.ITEM) {
                score += 200;
                maze.setCell(pos.getX(), pos.getY(), CellType.EMPTY); // Ăn rồi thì mất
            }

            if (!moved || robot.isFinished()) {
                this.state = GameState.FINISHED; // 13. Win detection
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
    public GameState getState() { return state; }
    public RobotController getRobot() { return robot; }
    public List<State> getExplored() { return explored; }
    public Maze getMaze() { return maze; }
    /* Khi nào tạo GUI thì thêm đoạn này vào (loop update để robot di chuyển tự động)
    Timeline loop = new Timeline(
        new KeyFrame(Duration.millis(300), e -> {
            engine.update();
            drawMap();
        })
    );

    loop.setCycleCount(Animation.INDEFINITE);
    loop.play(); */
}
