package com.nhom_01.robot_pathfinding.ai;

import com.nhom_01.robot_pathfinding.core.SearchResult;
import com.nhom_01.robot_pathfinding.core.State;

public interface SearchAlgorithm {
    SearchResult findPath(State start, State goal, int[][] map);
}
