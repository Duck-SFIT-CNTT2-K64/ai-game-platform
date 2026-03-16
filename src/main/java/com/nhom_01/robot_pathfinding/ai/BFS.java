package com.nhom_01.robot_pathfinding.ai;

import com.nhom_01.robot_pathfinding.core.*;
import java.util.*;

public class BFS implements SearchAlgorithm {
    @Override
    public SearchResult findPath(Maze maze, State start, State goal) {
        Queue<State> frontier = new LinkedList<>();
        frontier.add(start);

        Map<State, State> cameFrom = new HashMap<>();
        cameFrom.put(start, null);

        List<State> explored = new ArrayList<>();

        while (!frontier.isEmpty()) {
            State current = frontier.poll();
            explored.add(current);

            if (current.getX() == goal.getX() && current.getY() == goal.getY()) {
                return new SearchResult(PathReconstructor.reconstruct(cameFrom, goal), explored, explored.size());
            }

            for (State next : getNeighbors(current, maze)) {
                if (!cameFrom.containsKey(next)) {
                    frontier.add(next);
                    cameFrom.put(next, current);
                }
            }
        }
        return new SearchResult(new ArrayList<>(), explored, explored.size());
    }

    private List<State> getNeighbors(State s, Maze maze) {
        List<State> neighbors = new ArrayList<>();
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 0}};
        for (int[] d : dirs) {
            int nx = s.getX() + d[0], ny = s.getY() + d[1];
            if (maze.getCell(nx, ny) != CellType.WALL) {
                neighbors.add(new State(nx, ny, s.getLives()));
            }
        }
        return neighbors;
    }
}