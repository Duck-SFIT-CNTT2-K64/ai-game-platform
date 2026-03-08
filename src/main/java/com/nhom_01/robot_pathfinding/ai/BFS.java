package com.nhom_01.robot_pathfinding.ai;

import com.nhom_01.robot_pathfinding.core.SearchResult;
import com.nhom_01.robot_pathfinding.core.State;

import java.util.*;

public class BFS implements SearchAlgorithm {

    @Override
    public SearchResult findPath(State start, State goal, int[][] map) {

        Queue<State> queue = new LinkedList<>();
        Set<State> visited = new HashSet<>();
        Map<State, State> parent = new HashMap<>();

        queue.add(start);
        visited.add(start);

        int exploredNodes = 0;

        while (!queue.isEmpty()) {
            State current = queue.poll();
            exploredNodes++;


            if (current.equals(goal)) {
                List<State> path = reconstructPath(parent, current);
                return new SearchResult(path, exploredNodes);
            }


            for (State neighbor : getNeighbors(current, map)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor);
                    parent.put(neighbor, current);
                    queue.add(neighbor); // Thêm vào cuối Queue
                }
            }
        }


        return new SearchResult(Collections.emptyList(), exploredNodes);
    }

    private List<State> reconstructPath(Map<State, State> parent, State goal) {
        List<State> path = new ArrayList<>();
        State current = goal;

        while (current != null) {
            path.add(current);
            current = parent.get(current);
        }

        Collections.reverse(path);
        return path;
    }

    private List<State> getNeighbors(State state, int[][] map) {
        List<State> neighbors = new ArrayList<>();

        int x = state.getX();
        int y = state.getY();
        int lives = state.getLives();

        int[][] directions = {
                {0, 1},
                {1, 0},
                {0, -1},
                {-1, 0}
        };

        for (int[] d : directions) {
            int nx = x + d[0];
            int ny = y + d[1];

            if (isValid(nx, ny, map)) {
                neighbors.add(new State(nx, ny, lives));
            }
        }

        return neighbors;
    }

    private boolean isValid(int x, int y, int[][] map) {
        return x >= 0 && y >= 0 && x < map.length && y < map[0].length && map[x][y] != 1;
    }
}