package com.nhom_01.robot_pathfinding.ui.components;

import com.nhom_01.robot_pathfinding.core.PowerUp;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ItemCardSelectionModal {
    private static final String MODAL_OVERLAY_ID = "item-card-selection-overlay";

    private ItemCardSelectionModal() {
    }

    public static boolean showOnScene(Scene gameScene, Consumer<PowerUp> onSelect, Runnable onClosed) {
        if (gameScene == null || !(gameScene.getRoot() instanceof StackPane)) {
            safeRun(onClosed);
            return false;
        }

        StackPane gameRoot = (StackPane) gameScene.getRoot();
        if (gameRoot.lookup("#" + MODAL_OVERLAY_ID) != null) {
            return false;
        }

        StackPane overlayRoot = new StackPane();
        overlayRoot.setId(MODAL_OVERLAY_ID);
        overlayRoot.setPrefSize(gameScene.getWidth(), gameScene.getHeight());
        overlayRoot.setStyle("-fx-background-color: rgba(0,0,0,0.72);");
        overlayRoot.setPickOnBounds(true);

        VBox modalContainer = new VBox(20);
        modalContainer.setAlignment(Pos.CENTER);
        modalContainer.setPrefWidth(1200);
        modalContainer.setStyle(
            "-fx-background-color: rgba(22,34,52,0.95);" +
            "-fx-border-color: rgba(0,255,255,0.8);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 15;" +
            "-fx-background-radius: 15;"
        );
        modalContainer.setPadding(new Insets(30));

        Text titleText = new Text("SELECT A POWER-UP");
        titleText.setFont(Font.font("Orbitron", FontWeight.BOLD, 40));
        titleText.setFill(Color.web("#00FFFF"));

        Text subtitleText = new Text("Choose 1 of 3 power-ups to boost your game");
        subtitleText.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitleText.setFill(Color.web("#C9DCEA"));

        // Generate 3 random power-ups
        List<PowerUp> selectedPowerUps = generateRandomPowerUps(3);

        if (selectedPowerUps.isEmpty()) {
            Label error = new Label("No power-up available");
            error.setFont(Font.font("Arial", FontWeight.BOLD, 16));
            error.setTextFill(Color.web("#FFD59A"));
            modalContainer.getChildren().add(error);
            overlayRoot.getChildren().add(modalContainer);
            gameRoot.getChildren().add(overlayRoot);
            safeRun(onClosed);
            return false;
        }

        HBox cardsBox = new HBox(30);
        cardsBox.setAlignment(Pos.CENTER);
        cardsBox.setPrefHeight(350);

        AtomicBoolean closing = new AtomicBoolean(false);

        for (PowerUp powerUp : selectedPowerUps) {
            HBox cardBox = createPowerUpCard(powerUp, () -> {
                if (closing.compareAndSet(false, true)) {
                    safeRun(() -> onSelect.accept(powerUp));
                    fadeOutAndRemove(overlayRoot, gameRoot, onClosed);
                }
            });
            cardsBox.getChildren().add(cardBox);
        }

        modalContainer.getChildren().addAll(titleText, subtitleText, cardsBox);
        overlayRoot.getChildren().add(modalContainer);

        // Block arrow keys while selecting card, and close with ESC.
        overlayRoot.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    if (closing.compareAndSet(false, true)) {
                        fadeOutAndRemove(overlayRoot, gameRoot, onClosed);
                    }
                    e.consume();
                }
                default -> e.consume();
            }
        });

        gameRoot.getChildren().add(overlayRoot);
        overlayRoot.requestFocus();

        // Fade in animation
        FadeTransition fade = new FadeTransition(Duration.millis(240), overlayRoot);
        fade.setFromValue(0);
        fade.setToValue(1);
        fade.play();
        return true;
    }

    private static HBox createPowerUpCard(PowerUp powerUp, Runnable onSelect) {
        HBox card = new HBox(15);
        card.setPrefSize(300, 320);
        card.setAlignment(Pos.TOP_CENTER);
        card.setPadding(new Insets(20));
        card.setStyle(
            "-fx-background-color: rgba(30,50,80,0.8);" +
            "-fx-border-color: rgba(100,200,255,0.6);" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;" +
            "-fx-cursor: hand;"
        );

        VBox content = new VBox(12);
        content.setAlignment(Pos.TOP_CENTER);
        content.setPrefWidth(260);

        // Difficulty indicator
        Text difficultyText = new Text(powerUp.getDifficulty().toString());
        difficultyText.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        Color diffColor = switch (powerUp.getDifficulty()) {
            case EASY -> Color.web("#00FF9C");
            case MEDIUM -> Color.web("#FFB800");
            case HARD -> Color.web("#FF6B6B");
        };
        difficultyText.setFill(diffColor);

        // Power-up name
        Text nameText = new Text(powerUp.getDisplayName());
        nameText.setFont(Font.font("Orbitron", FontWeight.BOLD, 18));
        nameText.setFill(Color.web("#00FFFF"));
        nameText.setTextAlignment(TextAlignment.CENTER);
        nameText.setWrappingWidth(260);

        // English description
        Text descText = new Text(powerUp.getEnglishDescription());
        descText.setFont(Font.font("Arial", FontWeight.NORMAL, 13));
        descText.setFill(Color.web("#C9DCEA"));
        descText.setTextAlignment(TextAlignment.CENTER);
        descText.setWrappingWidth(260);

        // Vietnamese description (effect)
        Text effectText = new Text("Hiệu ứng: " + powerUp.getVietnameseDescription());
        effectText.setFont(Font.font("Arial", 12));
        effectText.setFill(Color.web("#9FFFD8"));
        effectText.setTextAlignment(TextAlignment.CENTER);
        effectText.setWrappingWidth(260);

        // Select button
        Button selectButton = new NeonButton("SELECT", Color.web("#00FF9C"), 12, 6, 12, 6);
        selectButton.setPrefWidth(200);
        selectButton.setOnAction(e -> onSelect.run());

        content.getChildren().addAll(difficultyText, nameText, descText, effectText, selectButton);
        card.getChildren().add(content);

        // Hover effect
        card.setOnMouseEntered(e -> {
            card.setStyle(
                "-fx-background-color: rgba(50,80,120,0.9);" +
                "-fx-border-color: rgba(0,255,255,0.9);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                "-fx-background-color: rgba(30,50,80,0.8);" +
                "-fx-border-color: rgba(100,200,255,0.6);" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 12;" +
                "-fx-background-radius: 12;" +
                "-fx-cursor: hand;"
            );
        });

        return card;
    }

    private static List<PowerUp> generateRandomPowerUps(int count) {
        PowerUp[] allPowerUps = PowerUp.values();
        List<PowerUp> selected = new ArrayList<>();
        Random random = new Random();

        while (selected.size() < count && selected.size() < allPowerUps.length) {
            PowerUp powerUp = allPowerUps[random.nextInt(allPowerUps.length)];
            if (!selected.contains(powerUp)) {
                selected.add(powerUp);
            }
        }

        return selected;
    }

    private static void fadeOutAndRemove(StackPane overlayRoot, StackPane gameRoot, Runnable onClosed) {
        FadeTransition fade = new FadeTransition(Duration.millis(180), overlayRoot);
        fade.setFromValue(1);
        fade.setToValue(0);
        fade.setOnFinished(e -> {
            gameRoot.getChildren().remove(overlayRoot);
            gameRoot.requestFocus();
            safeRun(onClosed);
        });
        fade.play();
    }

    private static void safeRun(Runnable runnable) {
        if (runnable == null) {
            return;
        }
        try {
            runnable.run();
        } catch (Exception ignored) {
            // Never break UI flow because of callback exceptions.
        }
    }
}
