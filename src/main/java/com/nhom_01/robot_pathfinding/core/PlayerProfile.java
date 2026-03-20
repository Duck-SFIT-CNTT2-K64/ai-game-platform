package com.nhom_01.robot_pathfinding.core;

public final class PlayerProfile {

    private static String currentPlayerName;

    private PlayerProfile() {
    }

    public static synchronized void setCurrentPlayerName(String name) {
        currentPlayerName = sanitize(name);
    }

    public static synchronized String getCurrentPlayerName() {
        if (currentPlayerName == null || currentPlayerName.isBlank()) {
            return "PLAYER";
        }
        return currentPlayerName;
    }

    public static synchronized boolean hasPlayerName() {
        return currentPlayerName != null && !currentPlayerName.isBlank();
    }

    private static String sanitize(String name) {
        if (name == null) {
            return null;
        }
        String trimmed = name.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        if (trimmed.length() > 24) {
            return trimmed.substring(0, 24);
        }
        return trimmed;
    }
}
