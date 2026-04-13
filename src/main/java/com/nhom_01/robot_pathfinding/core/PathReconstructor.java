package com.nhom_01.robot_pathfinding.core;

import java.util.*;

public class PathReconstructor {
    public static List<State> reconstruct(Map<State, State> cameFrom, State goal) {
        List<State> path = new ArrayList<>();
        State current = goal;

        // Di nguoc tu Goal ve dau
        while (current != null) {
            path.add(current);
            current = cameFrom.get(current);
        }

        // Dao nguoc danh sach de co thu tu tu Start -> Goal
        Collections.reverse(path);
        return path;
    }
}