package com.nhom_01.robot_pathfinding.ui.audio;

import com.nhom_01.robot_pathfinding.core.GameSettings;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.ButtonBase;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;

import java.net.URL;
import java.util.ArrayDeque;
import java.util.Deque;

public final class MenuAudioManager {

    private static final String CLICK_HANDLER_KEY = "menu-audio-click-handler";
    private static MediaPlayer themePlayer;
    private static AudioClip themeClip;
    private static AudioClip clickClip;

    private MenuAudioManager() {
    }

    public static void startTheme() {
        System.out.println("[AUDIO] startTheme() called - using SystemAudioPlayer");
        
        GameSettings settings = GameSettings.getInstance();
        double musicGain = settings.getMasterVolume() * settings.getMusicVolume() / 10_000.0;
        double clamped = clamp(musicGain);
        
        SystemAudioPlayer.setVolume((float) clamped);
        SystemAudioPlayer.playLooping("/audio/Theme.mp3");
    }

    public static void stopTheme() {
        System.out.println("[AUDIO] stopTheme() called");
        SystemAudioPlayer.stopLooping();
        
        if (themePlayer != null) {
            try { themePlayer.stop(); } catch (Exception ignore) {}
        }
        if (themeClip != null) {
            try { themeClip.stop(); } catch (Exception ignore) {}
        }
    }

    public static void updateVolumes() {
        GameSettings settings = GameSettings.getInstance();
        double musicGain = settings.getMasterVolume() * settings.getMusicVolume() / 10_000.0;
        double clamped = clamp(musicGain);
        
        System.out.println("[AUDIO] updateVolumes() - setting to: " + clamped);
        SystemAudioPlayer.setVolume((float) clamped);
    }

    public static void playClick() {
        ensureLoaded();
        if (clickClip == null) {
            return;
        }

        GameSettings settings = GameSettings.getInstance();
        if (!settings.isMenuSoundEffectsEnabled()) {
            return;
        }

        double sfxGain = settings.getMasterVolume() * settings.getSFXVolume() / 10_000.0;
        clickClip.setVolume(clamp(sfxGain));
        clickClip.play();
    }

    public static void wireScene(Scene scene) {
        if (scene == null) {
            return;
        }

        if (!scene.getProperties().containsKey(CLICK_HANDLER_KEY)) {
            scene.addEventFilter(ActionEvent.ACTION, event -> {
                if (event.getTarget() instanceof ButtonBase) {
                    playClick();
                }
            });
            scene.getProperties().put(CLICK_HANDLER_KEY, Boolean.TRUE);
        }

        Parent root = scene.getRoot();
        if (root != null) {
            attachClickHandlers(root);
        }
        scene.rootProperty().addListener((obs, oldRoot, newRoot) -> {
            if (newRoot != null) {
                attachClickHandlers(newRoot);
            }
        });
    }

    private static void attachClickHandlers(Parent root) {
        Deque<Node> queue = new ArrayDeque<>();
        queue.add(root);

        while (!queue.isEmpty()) {
            Node node = queue.removeFirst();
            if (node instanceof ButtonBase button) {
                if (!button.getProperties().containsKey(CLICK_HANDLER_KEY)) {
                    button.addEventHandler(ActionEvent.ACTION, event -> playClick());
                    button.getProperties().put(CLICK_HANDLER_KEY, Boolean.TRUE);
                }
            }
            if (node instanceof Parent parent) {
                queue.addAll(parent.getChildrenUnmodifiable());
            }
        }
    }

    private static void ensureLoaded() {
        if (themePlayer == null) {
            try {
                java.net.URL themeUrl = MenuAudioManager.class.getResource("/audio/Theme.mp3");
                if (themeUrl != null) {
                    Media media = new Media(themeUrl.toExternalForm());
                    themePlayer = new MediaPlayer(media);
                    themePlayer.setCycleCount(MediaPlayer.INDEFINITE);
                }
            } catch (Exception ex) {
                System.out.println("[AUDIO] Warning: Could not load MediaPlayer");
            }
        }
        if (themeClip == null) {
            try {
                java.net.URL themeUrl = MenuAudioManager.class.getResource("/audio/Theme.mp3");
                if (themeUrl != null) {
                    themeClip = new AudioClip(themeUrl.toExternalForm());
                    themeClip.setCycleCount(AudioClip.INDEFINITE);
                }
            } catch (Exception ex) {
                System.out.println("[AUDIO] Warning: Could not load AudioClip");
            }
        }
        if (clickClip == null) {
            try {
                java.net.URL clickUrl = MenuAudioManager.class.getResource("/audio/Click.wav");
                if (clickUrl != null) {
                    clickClip = new AudioClip(clickUrl.toExternalForm());
                }
            } catch (Exception ex) {
                System.out.println("[AUDIO] Warning: Could not load Click.wav");
            }
        }
    }

    private static double clamp(double value) {
        if (value < 0) {
            return 0;
        }
        if (value > 1) {
            return 1;
        }
        return value;
    }
}