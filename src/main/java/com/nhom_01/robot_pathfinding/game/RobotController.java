package com.nhom_01.robot_pathfinding.game;

import java.util.List;

import com.nhom_01.robot_pathfinding.core.State;

public class RobotController {
    private List<State> path;
    public int currentIndex = 0;
    public void setPath(List<State> path) {
        this.path = path;
        currentIndex = 0;
    }
    public State getCurrentPosition() {
        if (path == null || currentIndex >= path.size()) return null;
        return path.get(currentIndex);
    }
    public boolean moveNext() {
        if (path == null || currentIndex >= path.size() - 1) return false;
        currentIndex++;
        return true;
    }
    public List<State> getPath() {
        return path;
    }
    public boolean isFinished() {
        return path != null && currentIndex >= path.size() - 1;
    }
}
