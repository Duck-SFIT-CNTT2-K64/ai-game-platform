package com.nhom_01.robot_pathfinding.core;

public enum 
PowerUp {
    SPEED_BOOST(
        "Speed Boost",
        "Robot moves faster for a short time.",
        "Robot di chuyển nhanh hơn",
        Difficulty.EASY
    ),
    SHIELD(
        "Shield",
        "Ignore one bomb explosion.",
        "Gặp bomb không chết 1 lần",
        Difficulty.EASY
    ),
    REVEAL_PATH(
        "Reveal Path",
        "Show the correct path to the goal.",
        "Hiển thị đường đi",
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
        "Hiện bomb gần robot",
        Difficulty.MEDIUM
    ),
    TELEPORT(
        "Teleport",
        "Teleport to a random safe tile.",
        "Teleport ngẫu nhiên",
        Difficulty.MEDIUM
    ),
    REMOVE_WALL(
        "Remove Wall",
        "Destroy one wall.",
        "Xóa 1 tường",
        Difficulty.MEDIUM
    ),
    SLOW_BOMBS(
        "Slow Bombs",
        "Bombs activate slower.",
        "Bomb delay",
        Difficulty.MEDIUM
    ),
    DOUBLE_SCORE(
        "Double Score",
        "Double score for this run.",
        "x2 điểm",
        Difficulty.EASY
    ),
    FREEZE_TIME(
        "Freeze Time",
        "Stop all bombs for a moment.",
        "Bomb không nổ trong vài giây",
        Difficulty.MEDIUM
    ),
    REVEAL_MAP(
        "Reveal Map",
        "Show the whole maze.",
        "Hiện toàn map",
        Difficulty.EASY
    ),
    AI_ASSIST(
        "AI Assist",
        "AI helps find the best path.",
        "Auto solve 1 đoạn",
        Difficulty.MEDIUM
    ),
    SHORTEST_PATH_MODE(
        "Shortest Path Mode",
        "Always choose shortest path.",
        "Ưu tiên BFS/A*",
        Difficulty.MEDIUM
    ),
    BOMB_IMMUNITY(
        "Bomb Immunity",
        "Ignore bombs for 5 seconds.",
        "Miễn nhiễm bomb",
        Difficulty.EASY
    ),
    SPEED_SLOW(
        "Speed Slow",
        "Move slower but safer.",
        "Robot chậm, bomb dễ tránh",
        Difficulty.EASY
    ),
    LUCKY_FIND(
        "Lucky Find",
        "Higher chance to find items.",
        "Tăng spawn item",
        Difficulty.MEDIUM
    ),
    DOUBLE_CHOICE(
        "Double Choice",
        "Choose 2 skills instead of 1.",
        "Chọn 2 skill",
        Difficulty.HARD
    ),
    SAFE_STEP(
        "Safe Step",
        "Next step cannot be a bomb.",
        "Bước tiếp theo luôn an toàn",
        Difficulty.MEDIUM
    ),
    TIME_BONUS(
        "Time Bonus",
        "Extra time added.",
        "+time",
        Difficulty.EASY
    ),
    VISION_BOOST(
        "Vision Boost",
        "See further around robot.",
        "Hiện vùng lớn hơn",
        Difficulty.MEDIUM
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
