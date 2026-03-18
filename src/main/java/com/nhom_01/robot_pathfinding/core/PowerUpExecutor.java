package com.nhom_01.robot_pathfinding.core;

public class PowerUpExecutor {
    
    /**
     * Execute a power-up effect on the game state
     */
    public static void executePowerUp(
        CollectedPowerUp powerUp,
        State playerPosition,
        Maze maze,
        GameStateContainer state
    ) {
        if (powerUp == null || !powerUp.isActive()) {
            return;
        }

        PowerUp type = powerUp.getPowerUp();
        
        switch (type) {
            case SPEED_BOOST:
                // Robot moves faster - handled by accelerating ticks
                state.setSpeedMultiplier(0.5); // Move at 50% speed
                break;
                
            case SHIELD:
                // Ignore next bomb - mark as shielded
                state.setShielded(true);
                powerUp.deactivate(); // Single use
                break;
                
            case REVEAL_PATH:
                // Calculate and show path to goal
                state.setShowPath(true);
                break;
                
            case EXTRA_LIFE:
                // Already handled during collection
                state.setLivesBonus(1);
                powerUp.deactivate(); // Single use
                break;
                
            case BOMB_DETECTOR:
                // Mark bombs visible nearby
                state.setBombDetectorActive(true);
                break;
                
            case TELEPORT:
                // Teleport to random safe position
                teleportToRandomSafePosition(maze, playerPosition);
                powerUp.deactivate(); // Single use
                break;
                
            case REMOVE_WALL:
                // Store for next wall removal
                state.setWallRemovalCharge(1);
                powerUp.deactivate(); // Single use
                break;
                
            case SLOW_BOMBS:
                // Delay bomb explosions
                state.setBombDelayActive(true);
                break;
                
            case DOUBLE_SCORE:
                // Apply multiplier to score
                state.setScoreMultiplier(2.0);
                break;
                
            case FREEZE_TIME:
                // Freeze all bombs
                state.setFrozenBombs(true);
                break;
                
            case REVEAL_MAP:
                // Show entire maze
                state.setMapRevealed(true);
                break;
                
            case AI_ASSIST:
                // AI suggests best path segment
                state.setAiAssistActive(true);
                powerUp.deactivate(); // Single use
                break;
                
            case SHORTEST_PATH_MODE:
                // Suggest shortest path
                state.setShortestPathMode(true);
                break;
                
            case BOMB_IMMUNITY:
                // Ignore bombs temporarily
                state.setBombImmune(true);
                powerUp.setDuration(5000); // 5 seconds
                break;
                
            case SPEED_SLOW:
                // Move slower but safer
                state.setSpeedMultiplier(1.5); // 150% slower = 66% speed
                break;
                
            case LUCKY_FIND:
                // Increase item spawn rate for next generation
                state.setLuckyFindActive(true);
                powerUp.deactivate();
                break;
                
            case DOUBLE_CHOICE:
                // Allow selecting 2 power-ups at once
                state.setDoubleChoiceActive(true);
                powerUp.deactivate(); // Single use
                break;
                
            case SAFE_STEP:
                // Guarantee next step has no bomb
                state.setSafeStepActive(true);
                powerUp.deactivate(); // Single use
                break;
                
            case TIME_BONUS:
                // Add time - implementation depends on timer system
                state.setTimeBonus(30); // +30 seconds
                powerUp.deactivate(); // Single use
                break;
                
            case VISION_BOOST:
                // Extend vision range
                state.setVisionRange(2.0); // 2x normal range
                break;
        }
    }
    
    private static void teleportToRandomSafePosition(Maze maze, State playerPosition) {
        // Find random empty cell that's safe
        java.util.List<int[]> safePositions = new java.util.ArrayList<>();
        
        for (int x = 1; x < maze.getWidth() - 1; x++) {
            for (int y = 1; y < maze.getHeight() - 1; y++) {
                CellType cell = maze.getCell(x, y);
                // Safe if not wall and not bomb
                if (cell != CellType.WALL && cell != CellType.BOMB) {
                    safePositions.add(new int[]{x, y});
                }
            }
        }
        
        if (!safePositions.isEmpty()) {
            java.util.Random random = new java.util.Random();
            int[] newPos = safePositions.get(random.nextInt(safePositions.size()));
            // In real implementation, update playerPosition to newPos
        }
    }
    
    /**
     * Check and deactivate expired power-ups
     */
    public static void updatePowerUpStates(java.util.List<CollectedPowerUp> inventory) {
        for (CollectedPowerUp powerUp : inventory) {
            if (powerUp.isActive() && powerUp.isExpired()) {
                powerUp.deactivate();
            }
        }
    }
}

/**
 * Container for game state modifications by power-ups
 */
class GameStateContainer {
    private double speedMultiplier = 1.0;
    private boolean shielded = false;
    private boolean showPath = false;
    private int livesBonus = 0;
    private boolean bombDetectorActive = false;
    private boolean bombDelayActive = false;
    private double scoreMultiplier = 1.0;
    private boolean frozenBombs = false;
    private boolean mapRevealed = false;
    private boolean aiAssistActive = false;
    private boolean shortestPathMode = false;
    private boolean bombImmune = false;
    private boolean luckyFindActive = false;
    private boolean doubleChoiceActive = false;
    private boolean safeStepActive = false;
    private int timeBonus = 0;
    private double visionRange = 1.0;
    private int wallRemovalCharge = 0;

    // Getters and Setters
    public double getSpeedMultiplier() { return speedMultiplier; }
    public void setSpeedMultiplier(double value) { this.speedMultiplier = value; }

    public boolean isShielded() { return shielded; }
    public void setShielded(boolean value) { this.shielded = value; }

    public boolean isShowPath() { return showPath; }
    public void setShowPath(boolean value) { this.showPath = value; }

    public int getLivesBonus() { return livesBonus; }
    public void setLivesBonus(int value) { this.livesBonus = value; }

    public boolean isBombDetectorActive() { return bombDetectorActive; }
    public void setBombDetectorActive(boolean value) { this.bombDetectorActive = value; }

    public boolean isBombDelayActive() { return bombDelayActive; }
    public void setBombDelayActive(boolean value) { this.bombDelayActive = value; }

    public double getScoreMultiplier() { return scoreMultiplier; }
    public void setScoreMultiplier(double value) { this.scoreMultiplier = value; }

    public boolean isFrozenBombs() { return frozenBombs; }
    public void setFrozenBombs(boolean value) { this.frozenBombs = value; }

    public boolean isMapRevealed() { return mapRevealed; }
    public void setMapRevealed(boolean value) { this.mapRevealed = value; }

    public boolean isAiAssistActive() { return aiAssistActive; }
    public void setAiAssistActive(boolean value) { this.aiAssistActive = value; }

    public boolean isShortestPathMode() { return shortestPathMode; }
    public void setShortestPathMode(boolean value) { this.shortestPathMode = value; }

    public boolean isBombImmune() { return bombImmune; }
    public void setBombImmune(boolean value) { this.bombImmune = value; }

    public boolean isLuckyFindActive() { return luckyFindActive; }
    public void setLuckyFindActive(boolean value) { this.luckyFindActive = value; }

    public boolean isDoubleChoiceActive() { return doubleChoiceActive; }
    public void setDoubleChoiceActive(boolean value) { this.doubleChoiceActive = value; }

    public boolean isSafeStepActive() { return safeStepActive; }
    public void setSafeStepActive(boolean value) { this.safeStepActive = value; }

    public int getTimeBonus() { return timeBonus; }
    public void setTimeBonus(int value) { this.timeBonus = value; }

    public double getVisionRange() { return visionRange; }
    public void setVisionRange(double value) { this.visionRange = value; }

    public int getWallRemovalCharge() { return wallRemovalCharge; }
    public void setWallRemovalCharge(int value) { this.wallRemovalCharge = value; }
}
