package com.nhom_01.robot_pathfinding.game;

import java.util.List;

import com.nhom_01.robot_pathfinding.ai.SearchAlgorithm;
import com.nhom_01.robot_pathfinding.core.SearchResult;
import com.nhom_01.robot_pathfinding.core.State;

public class GameEngine {
    private int[][] map;
    private State start;
    private State goal;
    private SearchAlgorithm algorithm;
    private RobotController robot;
    private GameState state;
    private int exploredNodes;
    private List<State> explored;

    public GameEngine(int[][] map, State start, State goal, SearchAlgorithm algorithm) {
        this.map = map;
        this.start = start;
        this.goal = goal;
        this.algorithm = algorithm;
        this.robot = new RobotController();
        this.state = GameState.READY;
        this.exploredNodes = 0;
    }
    public void startSearch() {
        state = GameState.SEARCHING;
        SearchResult result = algorithm.findPath(start, goal, map);
        exploredNodes = result.getExploredNodes();
        explored = result.getExplored();

        List<State> path = result.getPath();
        if (path.isEmpty()) {
            state = GameState.NO_PATH;
            return;
        }
        robot.setPath(path);
        state = GameState.MOVING;
    }
    public void update() {
        if (state != GameState.MOVING) return;
        boolean moved = robot.moveNext();
        if (!moved || robot.isFinished()) {
            state = GameState.FINISHED;
        }
    }
    public State getRobotPosition() {
        return robot.getCurrentPosition();
    }
    public GameState getState() {
        return state;
    }
    public int getExploredNodes() {
        return exploredNodes;
    }
    public void reset() {
        robot = new RobotController();
        state = GameState.READY;
    }
    public List<State> getPath() {
        return robot.getPath();
    }
    public List<State> getExplored() {
        return explored;
    }
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
