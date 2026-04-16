package com.nhom_01.robot_pathfinding.core;

public enum 
PowerUp {
    SPEED_BOOST(
        "Speed Boost",
        "Significantly increases movement speed for a short duration. Perfect for escaping dangerous areas.",
        "Tăng đáng kể tốc độ di chuyển trong một khoảng thời gian ngắn. Rất hữu ích khi cần thoát khỏi khu vực nguy hiểm.",
        Difficulty.EASY
    ),
    SHIELD(
        "Shield",
        "Protects the duck from 1 bomb collision without losing a life. A distinct blue aura surrounds you.",
        "Bảo vệ Vịt khỏi 1 lần va chạm với bom mà không bị mất mạng. Có một hào quang màu xanh bao quanh bạn.",
        Difficulty.MEDIUM
    ),
    REVEAL_PATH(
        "Reveal Path",
        "Shows the shortest direct route through the current maze path towards the goal.",
        "Hiển thị con đường ngắn nhất và an toàn nhất để dẫn bạn tới được cửa thoát.",
        Difficulty.MEDIUM
    ),
    EXTRA_LIFE(
        "Extra Life",
        "Restores a lost heart, allowing you more chances to navigate the dangerous maze.",
        "Hồi phục cho Vịt 1 đơn vị mạng, giúp bạn có thêm cơ hội để hoàn thành mê cung.",
        Difficulty.EASY
    ),
    BOMB_DETECTOR(
        "Bomb Detector",
        "Reveals the locations of all hidden bombs in a wide radius, helping you navigate safely.",
        "Hiển thị vị trí của các quả bom ẩn trong phạm vi gần xung quanh Vịt.",
        Difficulty.MEDIUM
    ),
    TELEPORT(
        "Teleport",
        "Instantly teleports the duck to a random safe location within the maze.",
        "Ngay lập tức dịch chuyển Vịt đến một vị trí an toàn ngẫu nhiên trong mê cung.",
        Difficulty.MEDIUM
    ),
    REMOVE_WALL(
        "Remove Wall",
        "A special charge that allows the duck to demolish a wall cell upon impact, creating new shortcuts.",
        "Một luồng năng lượng đặc biệt cho phép Vịt phá hủy tường khi chạm vào, tạo ra những con đường tắt mới.",
        Difficulty.MEDIUM
    ),
    DOUBLE_SCORE(
        "Double Score",
        "Doubles your current score instantly. A powerful way to top the leaderboards.",
        "Nhân đôi số điểm hiện tại của bạn ngay lập tức. Đây là cách tuyệt vời để leo lên đỉnh bảng xếp hạng.",
        Difficulty.EASY
    ),
    FREEZE_TIME(
        "Freeze Time",
        "Freezes all bombs in the maze. Frozen bombs act as solid walls that you cannot pass through.",
        "Đóng băng tất cả bom trong mê cung. Bom bị đóng băng sẽ trở thành tường kiên cố mà bạn không thể đi xuyên qua.",
        Difficulty.MEDIUM
    ),
    AI_ASSIST(
        "AI Assist",
        "Hands over control to an advanced AI core for 8 steps to navigate toward the goal.",
        "Trao quyền điều khiển cho AI trong 8 bước để di chuyển theo lộ trình tối ưu nhất tới cửa thoát.",
        Difficulty.MEDIUM
    ),
    SHORTEST_PATH_MODE(
        "Shortest Path Mode",
        "Optimizes your movement strategy using advanced pathfinding algorithms.",
        "Hỗ trợ tìm đường đi tối ưu nhất bằng các thuật toán tìm kiếm thông minh.",
        Difficulty.MEDIUM
    ),
    SPEED_SLOW(
        "Speed Slow",
        "Move slower but gain precise control, making it easier to dodge timed obstacles.",
        "Di chuyển chậm hơn nhưng giúp bạn kiểm soát vị trí chính xác hơn để dễ dàng né các quả bom.",
        Difficulty.EASY
    ),
    LUCKY_FIND(
        "Lucky Find",
        "Grants a random beneficial power-up to your inventory immediately.",
        "Nhận lấy một vật phẩm ngẫu nhiên cho túi đồ của bạn ngay lập tức.",
        Difficulty.MEDIUM
    ),
    ANOTHER_OPTIONS(
        "Another Options",
        "Allows you to pick one extra reward when opening mystery boxes.",
        "Cho phép bạn được chọn thêm một vật phẩm phần thưởng nữa khi mở các hộp quà bí ẩn.",
        Difficulty.MEDIUM
    ),
    TIME_BONUS(
        "Time Bonus",
        "Adds 15 seconds to your mission clock. Vital for large mazes.",
        "Cộng thêm 15 giây vào đồng hồ đếm ngược. Rất quan trọng khi đối đầu với các mê cung lớn.",
        Difficulty.EASY
    ),
    VISION_BOOST(
        "Sonar Radar",
        "Uses long-range sound waves to detect and automatically vacuum all nearby mystery boxes.",
        "Sử dụng sóng âm tầm xa để phát hiện và tự động hút tất cả các hộp quà bí ẩn ở gần về phía Vịt.",
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
