package com.nhom_01.robot_pathfinding.core;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RankingEntry {
    private String playerName;
    private String difficulty;
    private int steps;
    private long timeMillis;
    private String algorithm;
    private int score;
    private LocalDateTime timestamp;

    public RankingEntry(String playerName, String difficulty, int steps, long timeMillis, String algorithm, int score) {
        this.playerName = playerName;
        this.difficulty = difficulty;
        this.steps = steps;
        this.timeMillis = timeMillis;
        this.algorithm = algorithm;
        this.score = score;
        this.timestamp = LocalDateTime.now();
    }

    // Getters
    public String getPlayerName() { return playerName; }
    public String getDifficulty() { return difficulty; }
    public int getSteps() { return steps; }
    public long getTimeMillis() { return timeMillis; }
    public String getAlgorithm() { return algorithm; }
    public int getScore() { return score; }
    public LocalDateTime getTimestamp() { return timestamp; }

    // Setters
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public void setSteps(int steps) { this.steps = steps; }
    public void setTimeMillis(long timeMillis) { this.timeMillis = timeMillis; }
    public void setAlgorithm(String algorithm) { this.algorithm = algorithm; }
    public void setScore(int score) { this.score = score; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    // Utility
    public String getTimeFormatted() {
        long seconds = timeMillis / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    public String getTimestampFormatted() {
        if (timestamp == null) return "";
        return timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
    }

    @Override
    public String toString() {
        return String.format(
            "RankingEntry{player='%s', difficulty='%s', steps=%d, time=%s, algorithm='%s', score=%d}",
            playerName, difficulty, steps, getTimeFormatted(), algorithm, score
        );
    }
} 
