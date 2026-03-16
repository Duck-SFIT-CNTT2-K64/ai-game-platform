package com.nhom_01.robot_pathfinding.ai;

import com.nhom_01.robot_pathfinding.core.*;
import java.util.*;

public class AStar implements SearchAlgorithm {
    @Override
    public SearchResult findPath(Maze maze, State start, State goal) {
        PriorityQueue<Node> frontier = new PriorityQueue<>(Comparator.comparingInt(n -> n.fScore));
        frontier.add(new Node(start, 0, heuristic(start, goal)));

        Map<State, State> cameFrom = new HashMap<>();
        Map<State, Integer> gScore = new HashMap<>();

        gScore.put(start, 0);
        List<State> explored = new ArrayList<>();

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();
            explored.add(current.state);

            if (current.state.getX() == goal.getX() && current.state.getY() == goal.getY()) {
                return new SearchResult(PathReconstructor.reconstruct(cameFrom, goal), explored, explored.size());
            }

            for (State next : getNeighbors(current.state, maze)) {
                int tentativeGScore = gScore.get(current.state) + 1;
                if (tentativeGScore < gScore.getOrDefault(next, Integer.MAX_VALUE)) {
                    cameFrom.put(next, current.state);
                    gScore.put(next, tentativeGScore);
                    int fScore = tentativeGScore + heuristic(next, goal);
                    frontier.add(new Node(next, tentativeGScore, fScore));
                }
            }
        }
        return new SearchResult(new ArrayList<>(), explored, explored.size());
    }

    private int heuristic(State a, State b) {
        return Math.abs(a.getX() - b.getX()) + Math.abs(a.getY() - b.getY());
    }

    private static class Node {
        State state;
        int gScore, fScore;
        Node(State s, int g, int f) { this.state = s; this.gScore = g; this.fScore = f; }
    }
    private List<State> getNeighbors(State s, Maze maze) {
        List<State> neighbors = new ArrayList<>();
        int[][] dirs = {{0, 1}, {1, 0}, {0, -1}, {-1, 1}};
        for (int[] d : dirs) {
            int nx = s.getX() + d[0], ny = s.getY() + d[1];
            if (maze.getCell(nx, ny) != CellType.WALL) {
                neighbors.add(new State(nx, ny, s.getLives()));
            }
        }
        return neighbors;
    }
}