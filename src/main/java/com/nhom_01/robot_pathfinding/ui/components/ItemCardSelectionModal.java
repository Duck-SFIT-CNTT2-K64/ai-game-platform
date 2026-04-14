package com.nhom_01.robot_pathfinding.ui.components;

import com.nhom_01.robot_pathfinding.core.PowerUp;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import javafx.animation.FadeTransition;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.FontWeight;
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
        if (gameRoot.lookup("#" + MODAL_OVERLAY_ID) != null) return false;

        List<PowerUp> powerUps = generateRandomPowerUps(3);
        if (powerUps.isEmpty()) { safeRun(onClosed); return false; }

        // ── Dark semi-transparent backdrop — game (duck + maze) stays visible ──
        StackPane overlay = new StackPane();
        overlay.setId(MODAL_OVERLAY_ID);
        overlay.setPrefSize(gameScene.getWidth(), gameScene.getHeight());
        overlay.setStyle("-fx-background-color: rgba(6,4,18,0.78);");
        overlay.setPickOnBounds(true);

        // ── Title (white text floating on dark backdrop) ────────────────────
        Text title = new Text("⚡  CHON POWER-UP");
        title.setFont(AppFonts.vt323(34));
        title.setFill(Color.WHITE);
        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(0.35, 0.62, 1.0, 0.90));
        glow.setRadius(20);
        title.setEffect(glow);

        Text subtitle = new Text("Chon 1 trong 3 power-up de tang cuong co hoi chien thang!");
        subtitle.setFont(AppFonts.vt323(15));
        subtitle.setFill(Color.color(0.72, 0.80, 1.0, 0.78));

        VBox titleBlock = new VBox(7, title, subtitle);
        titleBlock.setAlignment(Pos.CENTER);

        // ── Cards row (cards float directly on dark overlay) ────────────────
        HBox cardsRow = new HBox(22);
        cardsRow.setAlignment(Pos.CENTER);

        AtomicBoolean closing = new AtomicBoolean(false);
        for (PowerUp pu : powerUps) {
            VBox card = buildCard(pu, () -> {
                if (closing.compareAndSet(false, true)) {
                    safeRun(() -> onSelect.accept(pu));
                    fadeOutAndRemove(overlay, gameRoot, onClosed);
                }
            });
            cardsRow.getChildren().add(card);
        }

        Text escHint = new Text("Nhan  ESC  de bo qua");
        escHint.setFont(AppFonts.vt323(13));
        escHint.setFill(Color.color(0.55, 0.60, 0.72, 0.55));

        VBox content = new VBox(16, titleBlock, cardsRow, escHint);
        content.setAlignment(Pos.CENTER);
        content.setPickOnBounds(false);
        overlay.getChildren().add(content);
        AppFonts.applyTo(overlay);

        overlay.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case ESCAPE -> {
                    if (closing.compareAndSet(false, true))
                        fadeOutAndRemove(overlay, gameRoot, onClosed);
                    e.consume();
                }
                default -> e.consume();
            }
        });

        gameRoot.getChildren().add(overlay);
        overlay.requestFocus();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(220), overlay);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.play();
        return true;
    }

    // ── Card: colored header + white body ─────────────────────────────────────
    private static VBox buildCard(PowerUp pu, Runnable onSelect) {
        String diffHex = switch (pu.getDifficulty()) {
            case EASY   -> "#00C853";
            case MEDIUM -> "#F57C00";
            case HARD   -> "#C62828";
        };
        Color diffColor = Color.web(diffHex);
        String diffBgRgba = switch (pu.getDifficulty()) {
            case EASY   -> "rgba(0,200,83,0.12)";
            case MEDIUM -> "rgba(245,124,0,0.12)";
            case HARD   -> "rgba(198,40,40,0.12)";
        };

        VBox card = new VBox(0);
        card.setPrefSize(312, 450);
        card.setMaxSize(312, 450);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.97);" +
            "-fx-border-color: rgba(255,255,255,0.28);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 16;" +
            "-fx-background-radius: 16;" +
            "-fx-cursor: hand;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.55));
        shadow.setRadius(24);
        shadow.setOffsetY(7);
        card.setEffect(shadow);

        // ── Colored header ──────────────────────────────────────────────
        VBox header = new VBox(5);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(22, 16, 18, 16));
        header.setStyle(
            "-fx-background-color: " + diffHex + ";" +
            "-fx-background-radius: 16 16 0 0;"
        );

        Text badge = new Text("▸ " + pu.getDifficulty().toString());
        badge.setFont(AppFonts.vt323(12));
        badge.setFill(Color.color(1, 1, 1, 0.68));

        Text nameText = new Text(pu.getDisplayName());
        nameText.setFont(AppFonts.vt323(22));
        nameText.setFill(Color.WHITE);
        nameText.setTextAlignment(TextAlignment.CENTER);
        nameText.setWrappingWidth(272);
        nameText.setEffect(new DropShadow(6, Color.color(0, 0, 0, 0.30)));

        header.getChildren().addAll(badge, nameText);

        // ── Body ────────────────────────────────────────────────────────
        VBox body = new VBox(12);
        body.setAlignment(Pos.TOP_CENTER);
        body.setPadding(new Insets(18, 18, 20, 18));
        VBox.setVgrow(body, Priority.ALWAYS);

        Text descText = new Text(pu.getEnglishDescription());
        descText.setFont(AppFonts.vt323(15));
        descText.setFill(Color.web("#2D3E50"));
        descText.setTextAlignment(TextAlignment.CENTER);
        descText.setWrappingWidth(264);

        // Vietnamese effect tag with tinted background
        HBox effectTag = new HBox(4);
        effectTag.setAlignment(Pos.CENTER);
        effectTag.setPadding(new Insets(7, 12, 7, 12));
        effectTag.setStyle(
            "-fx-background-color: " + diffBgRgba + ";" +
            "-fx-background-radius: 8;"
        );
        Text effectText = new Text("✦  " + pu.getVietnameseDescription());
        effectText.setFont(AppFonts.vt323(14));
        effectText.setFill(diffColor);
        effectText.setTextAlignment(TextAlignment.CENTER);
        effectText.setWrappingWidth(252);
        effectTag.getChildren().add(effectText);

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Button selectBtn = new NeonButton("  CHON NGAY  ", diffColor, 13, 10, 14, 8);
        selectBtn.setPrefWidth(222);
        selectBtn.setOnAction(e -> onSelect.run());

        body.getChildren().addAll(descText, effectTag, spacer, selectBtn);
        card.getChildren().addAll(header, body);

        // ── Hover: lift + glow border ─────────────────────────────────
        card.setOnMouseEntered(e -> {
            shadow.setRadius(34);
            shadow.setOffsetY(12);
            card.setTranslateY(-5);
            card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.99);" +
                "-fx-border-color: " + diffHex + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;" +
                "-fx-cursor: hand;"
            );
        });
        card.setOnMouseExited(e -> {
            shadow.setRadius(24);
            shadow.setOffsetY(7);
            card.setTranslateY(0);
            card.setStyle(
                "-fx-background-color: rgba(255,255,255,0.97);" +
                "-fx-border-color: rgba(255,255,255,0.28);" +
                "-fx-border-width: 1.5;" +
                "-fx-border-radius: 16;" +
                "-fx-background-radius: 16;" +
                "-fx-cursor: hand;"
            );
        });

        return card;
    }

    private static List<PowerUp> generateRandomPowerUps(int count) {
        List<PowerUp> selected = new ArrayList<>();
        Random rng = new Random();
        
        while (selected.size() < count) {
            // 1. Roll for difficulty based on weights: Easy (50%), Medium (30%), Hard (20%)
            int roll = rng.nextInt(100);
            PowerUp.Difficulty diff;
            if (roll < 50) {
                diff = PowerUp.Difficulty.EASY;
            } else if (roll < 80) {
                diff = PowerUp.Difficulty.MEDIUM;
            } else {
                diff = PowerUp.Difficulty.HARD;
            }
            
            // 2. Get a random power-up of that difficulty
            PowerUp pu = PowerUp.getRandomByDifficulty(diff);
            
            // 3. Avoid duplicates across the 3 cards
            if (!selected.contains(pu)) {
                selected.add(pu);
            }
            
            // Safety break if we somehow run out of items (shouldn't happen with current enum)
            if (selected.size() >= PowerUp.values().length) break;
        }
        return selected;
    }

    private static void fadeOutAndRemove(StackPane overlay, StackPane root, Runnable onClosed) {
        FadeTransition ft = new FadeTransition(Duration.millis(200), overlay);
        ft.setFromValue(1);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            root.getChildren().remove(overlay);
            root.requestFocus();
            safeRun(onClosed);
        });
        ft.play();
    }

    private static void safeRun(Runnable r) {
        if (r == null) return;
        try { r.run(); } catch (Exception ignored) {}
    }
}
