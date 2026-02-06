package com.nhom_01.robot_pathfinding.core;

import java.util.Objects;

public class State{
    private final int x;
    private final int y;
    private final int lives;

    public State(int x, int y, int lives){
        this.x = x;
        this.y = y;
        this.lives = lives;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

    public int getLives(){
        return lives;
    }

    @Override
    public boolean equals(Object o){
        if(this == o) return true;
        if(!(o instanceof State)) return false;
        State state = (State) o;
        return x == state.x && y == state.y && lives == state.lives;
    }

    @Override
    public int hashCode(){
        return Objects.hash(x, y, lives);
    }

    @Override
    public String toString(){
        return "(" + x + ", " + y + ", lives=" + lives + ")";
    }
}