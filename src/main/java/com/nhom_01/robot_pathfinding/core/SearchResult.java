package com.nhom_01.robot_pathfinding.core;

import java.util.List;

public class SearchResult {
    private final List<State> path;
    private final int exploredNodes;

    public SearchResult(List<State> path, int exploredNodes){
        this.path = path;
        this.exploredNodes = exploredNodes;
    }

    public List<State> getPath(){
        return path;
    }

    public int getExploredNodes(){
            return exploredNodes;
    }
}
