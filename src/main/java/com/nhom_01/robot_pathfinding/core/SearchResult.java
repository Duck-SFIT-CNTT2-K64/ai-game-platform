package com.nhom_01.robot_pathfinding.core;

import java.util.List;

public class SearchResult {
    private final List<State> path;
    private final List<State> explored;
    private final int exploredNodes;

    public SearchResult(List<State> path, List<State> explored, int exploredNodes){
        this.path = path;
        this.explored = explored;
        this.exploredNodes = exploredNodes;
    }

    public List<State> getPath(){
        return path;
    }

    public List<State> getExplored(){
        return explored;
    }

    public int getExploredNodes(){
            return exploredNodes;
    }
}
