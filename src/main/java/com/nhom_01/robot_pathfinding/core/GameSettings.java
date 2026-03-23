package com.nhom_01.robot_pathfinding.core;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;

/**
 * Centralized game settings manager with persistent file storage.
 * Handles all user preferences: audio, gameplay, display settings.
 */
public class GameSettings {
    private static final String CONFIG_DIR = System.getProperty("user.home") + "/.robot-maze";
    private static final String CONFIG_FILE = CONFIG_DIR + "/game-settings.properties";
    
    // Audio Settings
    private double masterVolume = 80.0;
    private double musicVolume = 70.0;
    private double sfxVolume = 85.0;
    private boolean spatialAudioEnabled = false;
    private boolean menuSoundEffectsEnabled = true;
    
    // Gameplay Settings
    private boolean showPathHint = false;
    private boolean enableAISuggestion = false;
    private boolean vibrationFeedbackEnabled = false;
    private boolean autoPauseWhenUnfocused = false;
    private boolean confirmBeforeQuitting = false;
    
    // Display Settings
    private double uiScale = 100.0;
    private boolean highContrastLabels = false;
    private boolean reducedMotion = false;
    
    private static GameSettings instance;
    
    private GameSettings() {
        loadSettings();
    }
    
    /**
     * Get singleton instance of GameSettings
     */
    public static GameSettings getInstance() {
        if (instance == null) {
            instance = new GameSettings();
        }
        return instance;
    }
    
    /**
     * Load settings from file. If file doesn't exist, uses default values.
     */
    private void loadSettings() {
        try {
            Path configPath = Paths.get(CONFIG_FILE);
            if (Files.exists(configPath)) {
                Properties props = new Properties();
                try (InputStream input = new FileInputStream(configPath.toFile())) {
                    props.load(input);
                    
                    // Audio
                    masterVolume = Double.parseDouble(
                        props.getProperty("masterVolume", "80.0")
                    );
                    musicVolume = Double.parseDouble(
                        props.getProperty("musicVolume", "70.0")
                    );
                    sfxVolume = Double.parseDouble(
                        props.getProperty("sfxVolume", "85.0")
                    );
                    spatialAudioEnabled = Boolean.parseBoolean(
                        props.getProperty("spatialAudioEnabled", "false")
                    );
                    menuSoundEffectsEnabled = Boolean.parseBoolean(
                        props.getProperty("menuSoundEffectsEnabled", "true")
                    );
                    
                    // Gameplay
                    showPathHint = Boolean.parseBoolean(
                        props.getProperty("showPathHint", "false")
                    );
                    enableAISuggestion = Boolean.parseBoolean(
                        props.getProperty("enableAISuggestion", "false")
                    );
                    vibrationFeedbackEnabled = Boolean.parseBoolean(
                        props.getProperty("vibrationFeedbackEnabled", "false")
                    );
                    autoPauseWhenUnfocused = Boolean.parseBoolean(
                        props.getProperty("autoPauseWhenUnfocused", "false")
                    );
                    confirmBeforeQuitting = Boolean.parseBoolean(
                        props.getProperty("confirmBeforeQuitting", "false")
                    );
                    
                    // Display
                    uiScale = Double.parseDouble(
                        props.getProperty("uiScale", "100.0")
                    );
                    highContrastLabels = Boolean.parseBoolean(
                        props.getProperty("highContrastLabels", "false")
                    );
                    reducedMotion = Boolean.parseBoolean(
                        props.getProperty("reducedMotion", "false")
                    );
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to load settings: " + e.getMessage());
            // Use default values
        }
    }
    
    /**
     * Save all current settings to file
     */
    public void saveSettings() {
        try {
            Path configDir = Paths.get(CONFIG_DIR);
            Files.createDirectories(configDir);
            
            Properties props = new Properties();
            
            // Audio
            props.setProperty("masterVolume", String.valueOf(masterVolume));
            props.setProperty("musicVolume", String.valueOf(musicVolume));
            props.setProperty("sfxVolume", String.valueOf(sfxVolume));
            props.setProperty("spatialAudioEnabled", String.valueOf(spatialAudioEnabled));
            props.setProperty("menuSoundEffectsEnabled", String.valueOf(menuSoundEffectsEnabled));
            
            // Gameplay
            props.setProperty("showPathHint", String.valueOf(showPathHint));
            props.setProperty("enableAISuggestion", String.valueOf(enableAISuggestion));
            props.setProperty("vibrationFeedbackEnabled", String.valueOf(vibrationFeedbackEnabled));
            props.setProperty("autoPauseWhenUnfocused", String.valueOf(autoPauseWhenUnfocused));
            props.setProperty("confirmBeforeQuitting", String.valueOf(confirmBeforeQuitting));
            
            // Display
            props.setProperty("uiScale", String.valueOf(uiScale));
            props.setProperty("highContrastLabels", String.valueOf(highContrastLabels));
            props.setProperty("reducedMotion", String.valueOf(reducedMotion));
            
            try (OutputStream output = new FileOutputStream(CONFIG_FILE)) {
                props.store(output, "Robot Maze Game Settings");
            }
        } catch (IOException e) {
            System.err.println("Failed to save settings: " + e.getMessage());
        }
    }
    
    /**
     * Reset all settings to default values
     */
    public void resetToDefaults() {
        masterVolume = 80.0;
        musicVolume = 70.0;
        sfxVolume = 85.0;
        spatialAudioEnabled = false;
        menuSoundEffectsEnabled = true;
        
        showPathHint = false;
        enableAISuggestion = false;
        vibrationFeedbackEnabled = false;
        autoPauseWhenUnfocused = false;
        confirmBeforeQuitting = false;
        
        uiScale = 100.0;
        highContrastLabels = false;
        reducedMotion = false;
    }
    
    // ============ AUDIO GETTERS/SETTERS ============
    public double getMasterVolume() { return masterVolume; }
    public void setMasterVolume(double volume) { 
        this.masterVolume = Math.max(0, Math.min(100, volume)); 
    }
    
    public double getMusicVolume() { return musicVolume; }
    public void setMusicVolume(double volume) { 
        this.musicVolume = Math.max(0, Math.min(100, volume)); 
    }
    
    public double getSFXVolume() { return sfxVolume; }
    public void setSFXVolume(double volume) { 
        this.sfxVolume = Math.max(0, Math.min(100, volume)); 
    }
    
    public boolean isSpatialAudioEnabled() { return spatialAudioEnabled; }
    public void setSpatialAudioEnabled(boolean enabled) { this.spatialAudioEnabled = enabled; }
    
    public boolean isMenuSoundEffectsEnabled() { return menuSoundEffectsEnabled; }
    public void setMenuSoundEffectsEnabled(boolean enabled) { this.menuSoundEffectsEnabled = enabled; }
    
    // ============ GAMEPLAY GETTERS/SETTERS ============
    public boolean isShowPathHint() { return showPathHint; }
    public void setShowPathHint(boolean show) { this.showPathHint = show; }
    
    public boolean isEnableAISuggestion() { return enableAISuggestion; }
    public void setEnableAISuggestion(boolean enable) { this.enableAISuggestion = enable; }
    
    public boolean isVibrationFeedbackEnabled() { return vibrationFeedbackEnabled; }
    public void setVibrationFeedbackEnabled(boolean enabled) { this.vibrationFeedbackEnabled = enabled; }
    
    public boolean isAutoPauseWhenUnfocused() { return autoPauseWhenUnfocused; }
    public void setAutoPauseWhenUnfocused(boolean enabled) { this.autoPauseWhenUnfocused = enabled; }
    
    public boolean isConfirmBeforeQuitting() { return confirmBeforeQuitting; }
    public void setConfirmBeforeQuitting(boolean confirm) { this.confirmBeforeQuitting = confirm; }
    
    // ============ DISPLAY GETTERS/SETTERS ============
    public double getUIScale() { return uiScale; }
    public void setUIScale(double scale) { 
        this.uiScale = Math.max(80, Math.min(130, scale)); 
    }
    
    public boolean isHighContrastLabels() { return highContrastLabels; }
    public void setHighContrastLabels(boolean enabled) { this.highContrastLabels = enabled; }
    
    public boolean isReducedMotion() { return reducedMotion; }
    public void setReducedMotion(boolean enabled) { this.reducedMotion = enabled; }
}
