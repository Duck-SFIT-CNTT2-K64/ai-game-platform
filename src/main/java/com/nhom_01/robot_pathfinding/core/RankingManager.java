package com.nhom_01.robot_pathfinding.core;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class RankingManager {
    private static final String RANKINGS_FILE = "rankings.dat";
    private static RankingManager INSTANCE;
    private List<RankingEntry> allRankings;

    private RankingManager() {
        this.allRankings = new ArrayList<>();
        loadRankings();
    }

    public static RankingManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new RankingManager();
        }
        return INSTANCE;
    }

    // Load rankings from file
    private void loadRankings() {
        File file = new File(RANKINGS_FILE);
        if (!file.exists()) {
            allRankings.clear();
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            allRankings.clear();
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                RankingEntry entry = parseRankingEntry(line);
                if (entry != null) {
                    allRankings.add(entry);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading rankings: " + e.getMessage());
            allRankings.clear();
        }
    }

    // Save rankings to file
    public void saveRankings() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(RANKINGS_FILE))) {
            for (RankingEntry entry : allRankings) {
                writer.println(serializeRankingEntry(entry));
            }
            writer.flush();
        } catch (IOException e) {
            System.err.println("Error saving rankings: " + e.getMessage());
        }
    }

    // Add a new ranking entry
    public void addRanking(RankingEntry entry) {
        allRankings.add(entry);
        saveRankings();
    }

    // Get rankings by difficulty
    public List<RankingEntry> getRankingsByDifficulty(String difficulty) {
        List<RankingEntry> result = new ArrayList<>();
        for (RankingEntry entry : allRankings) {
            if (entry.getDifficulty().equalsIgnoreCase(difficulty)) {
                result.add(entry);
            }
        }
        // Sort by score descending
        result.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return result;
    }

    // Get all rankings sorted by score
    public List<RankingEntry> getAllRankings() {
        List<RankingEntry> result = new ArrayList<>(allRankings);
        result.sort((a, b) -> Integer.compare(b.getScore(), a.getScore()));
        return result;
    }

    // Serialize entry to file format (CSV-like)
    private String serializeRankingEntry(RankingEntry entry) {
        return String.format(
            "%s|%s|%d|%d|%s|%d|%s",
            entry.getPlayerName(),
            entry.getDifficulty(),
            entry.getSteps(),
            entry.getTimeMillis(),
            entry.getAlgorithm(),
            entry.getScore(),
            entry.getTimestamp().format(DateTimeFormatter.ISO_DATE_TIME)
        );
    }

    // Deserialize entry from file format
    private RankingEntry parseRankingEntry(String line) {
        try {
            String[] parts = line.split("\\|");
            if (parts.length < 6) return null;

            RankingEntry entry;
            if (parts.length >= 7) {
                entry = new RankingEntry(
                    parts[0],
                    parts[1],
                    Integer.parseInt(parts[2]),
                    Long.parseLong(parts[3]),
                    parts[4],
                    Integer.parseInt(parts[5])
                );
                entry.setTimestamp(LocalDateTime.parse(parts[6]));
            } else {
                // Backward compatibility with old format without player name.
                entry = new RankingEntry(
                    "PLAYER",
                    parts[0],
                    Integer.parseInt(parts[1]),
                    Long.parseLong(parts[2]),
                    parts[3],
                    Integer.parseInt(parts[4])
                );
                entry.setTimestamp(LocalDateTime.parse(parts[5]));
            }
            return entry;
        } catch (Exception e) {
            System.err.println("Error parsing ranking entry: " + line + " - " + e.getMessage());
            return null;
        }
    }

    // Clear all rankings
    public void clearAll() {
        allRankings.clear();
        saveRankings();
    }

    // Get top N rankings for a difficulty
    public List<RankingEntry> getTopRankings(String difficulty, int count) {
        List<RankingEntry> rankings = getRankingsByDifficulty(difficulty);
        return rankings.size() > count ? rankings.subList(0, count) : rankings;
    }

    // Check if entry is in top N for difficulty
    public boolean isTopRanking(String difficulty, int score, int topN) {
        List<RankingEntry> top = getTopRankings(difficulty, topN);
        if (top.size() < topN) return true;
        return score > top.get(top.size() - 1).getScore();
    }
}
