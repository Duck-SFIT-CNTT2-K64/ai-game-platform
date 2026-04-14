package com.nhom_01.robot_pathfinding.core;

public enum 
PowerUp {
    SPEED_BOOST(
        "Speed Boost",
        "Robot moves faster for a short time.",
        "Robot di chuyen nhanh hon",
        Difficulty.EASY
    ),
    SHIELD(
        "Shield",
        "Invincibility against bombs for 10 seconds.",
        "Khien bao ve trong 10 giay",
        Difficulty.MEDIUM
    ),
    REVEAL_PATH(
        "Reveal Path",
        "Show the correct path to the goal.",
        "Hien thi duong di",
        Difficulty.MEDIUM
    ),
    EXTRA_LIFE(
        "Extra Life",
        "Gain one extra life.",
        "+1 life",
        Difficulty.EASY
    ),
    BOMB_DETECTOR(
        "Bomb Detector",
        "Reveal bombs near the robot.",
        "Hien bomb gan robot",
        Difficulty.MEDIUM
    ),
    TELEPORT(
        "Teleport",
        "Teleport to a random safe tile.",
        "Teleport ngau nhien",
        Difficulty.MEDIUM
    ),
    REMOVE_WALL(
        "Remove Wall",
        "Destroy one wall.",
        "Xoa 1 tuong",
        Difficulty.MEDIUM
    ),
    DOUBLE_SCORE(
        "Double Score",
        "Double score for this run.",
        "x2 diem",
        Difficulty.EASY
    ),
    FREEZE_TIME(
        "Freeze Time",
        "Stop all bombs for a moment.",
        "Bomb khong no trong vai giay",
        Difficulty.MEDIUM
    ),
    AI_ASSIST(
        "AI Assist",
        "AI helps find the best path.",
        "Auto solve 1 doan",
        Difficulty.MEDIUM
    ),
    SHORTEST_PATH_MODE(
        "Shortest Path Mode",
        "Always choose shortest path.",
        "Uu tien BFS/A*",
        Difficulty.MEDIUM
    ),
    SPEED_SLOW(
        "Speed Slow",
        "Move slower but safer.",
        "Robot cham, bomb de tranh",
        Difficulty.EASY
    ),
    LUCKY_FIND(
        "Lucky Find",
        "Higher chance to find items.",
        "Tang spawn item",
        Difficulty.MEDIUM
    ),
    ANOTHER_OPTIONS(
        "Another Options",
        "Pick an additional power-up from mystery boxes.",
        "Nhan them 1 vat pham khi mo hop bi an",
        Difficulty.MEDIUM
    ),
    TIME_BONUS(
        "Time Bonus",
        "Extra time added.",
        "+time",
        Difficulty.EASY
    ),
    VISION_BOOST(
        "Sonar Radar",
        "Reveal map and pulse item locations for 15 seconds.",
        "Radar quet vat pham trong 15 giay",
        Difficulty.HARD
    );

    public enum Difficulty {
        EASY,
        MEDIUM,
        HARD
    }

    private final String displayName;
    private final String englishDescription;
    private final String vietnameseDescription;
    private final Difficulty difficulty;

    PowerUp(String displayName, String englishDescription, String vietnameseDescription, Difficulty difficulty) {
        this.displayName = displayName;
        this.englishDescription = englishDescription;
        this.vietnameseDescription = vietnameseDescription;
        this.difficulty = difficulty;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEnglishDescription() {
        return englishDescription;
    }

    public String getVietnameseDescription() {
        return vietnameseDescription;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }

    public static PowerUp getRandomByDifficulty(Difficulty difficulty) {
        PowerUp[] powerUps = values();
        PowerUp[] filtered = java.util.Arrays.stream(powerUps)
            .filter(p -> p.difficulty == difficulty)
            .toArray(PowerUp[]::new);
        
        if (filtered.length == 0) {
            return EXTRA_LIFE; // Fallback
        }
        
        java.util.Random random = new java.util.Random();
        return filtered[random.nextInt(filtered.length)];
    }
}
