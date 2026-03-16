package com.nhom_01.robot_pathfinding.core;

import java.util.*;

public class PathReconstructor {
    public static List<State> reconstruct(Map<State, State> cameFrom, State goal) {
        List<State> path = new ArrayList<>();
        State current = goal;

        // Đi ngược từ Goal về đầu
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }

        // Đảo ngược danh sách để có thứ tự từ Start -> Goal
        Collections.reverse(path);
        return path;
    }
}