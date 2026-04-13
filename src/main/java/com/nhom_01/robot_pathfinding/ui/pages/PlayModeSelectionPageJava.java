package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.PlayerProfile;
import com.nhom_01.robot_pathfinding.ui.PlayGamePage;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.effect.BlendMode;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayDeque;

public final class PlayModeSelectionPageJava {

    private static final double VIEW_WIDTH  = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    private static final double VIEW_HEIGHT = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
    private static final String BTN_PLAYER_PATH = "/image/assets/player.jpg";
    private static final String BTN_BOT_PATH = "/image/assets/bot.jpg";
    private static final String BTN_BACK_DIFFICULTY_PATH = "/image/assets/BackDifficulty.jpg";
    private static final String FRAME_BIG_PATH = "/image/assets/frame_big.png";
    private static final String LEAF_PATH = "/image/assets/la.jpg";
    private static final String DUCK_PATH = "/image/assets/vit.jpg";

    private PlayModeSelectionPageJava() {
    }

    public static void showOnStage(Stage stage, Scene previousScene, String difficulty) {
        Scene scene = buildScene(stage, previousScene, difficulty);
        MenuAudioManager.wireScene(scene);
        MenuAudioManager.startTheme();
        stage.setScene(scene);
    }

    private static Scene buildScene(Stage stage, Scene previousScene, String difficulty) {
        StackPane root = new StackPane();
        root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
        root.getChildren().add(createFuturisticBackground());

        VBox page = new VBox(20);
        page.setPadding(new Insets(24, 56, 24, 56));
        page.setAlignment(Pos.TOP_CENTER);

        Text title = new Text("CHOOSE PLAY MODE");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 56));
        title.setFill(Color.web("#1F2D3A"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.24));
        titleGlow.setRadius(14);
        title.setEffect(titleGlow);

        Text subtitle = new Text("DIFFICULTY: " + difficulty);
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        subtitle.setFill(Color.web("#4F5B62"));

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);

        HBox cards = new HBox(24);
        cards.setAlignment(Pos.CENTER);

        StackPane playerCard = createModeCard(
                "PLAYER",
                "🕹",
                "Control robot manually with arrow keys.",
                "Great for learning maze patterns and reacting to bombs.",
                "Use keyboard: UP / DOWN / LEFT / RIGHT",
                BTN_PLAYER_PATH,
                () -> ensurePlayerName(stage, stage.getScene(), () ->
                        PlayGamePage.showPlayerOnStage(stage, stage.getScene(), difficulty)
                )
        );

        StackPane botCard = createModeCard(
                "BOT",
                "🦆",
                "AI solves maze automatically based on algorithm.",
                "Useful to observe path quality and compare strategies.",
                "Next step: choose BFS / DFS / A*",
                BTN_BOT_PATH,
                () -> AlgorithmSelectionPageJava.showOnStage(stage, stage.getScene(), difficulty)
        );

        cards.getChildren().addAll(playerCard, botCard);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(1288);

        Button back = createImageButton(BTN_BACK_DIFFICULTY_PATH, 300, 74);
        back.setOnAction(e -> stage.setScene(previousScene));
        actions.getChildren().add(back);

        page.getChildren().addAll(heading, cards, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        AppFonts.applyTo(root);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static StackPane createModeCard(String title, String icon, String line1, String line2,
                                            String line3, String buttonImagePath, Runnable onChoose) {
        final double frameWidth = 540;
        final double frameHeight = 360;
        final double horizontalPadding = 34;
        final double topPadding = 22;
        final double bottomPadding = 22;
        final double contentWidth = frameWidth - (horizontalPadding * 2);

        StackPane card = new StackPane();
        card.setPrefSize(frameWidth, frameHeight);
        card.setMinSize(frameWidth, frameHeight);
        card.setMaxSize(frameWidth, frameHeight);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        Image rawFrameImage = loadImageResource(FRAME_BIG_PATH);
        Image frameImage = removeWhiteBackground(rawFrameImage);
        ImageView bg = new ImageView(frameImage);
        bg.setViewport(detectFrameViewport(frameImage));
        bg.setFitWidth(frameWidth);
        bg.setFitHeight(frameHeight);
        bg.setPreserveRatio(false);
        bg.setSmooth(false);

        Text heading = new Text(icon + "  " + title);
        heading.setFont(Font.font("Orbitron", FontWeight.BOLD, 34));
        heading.setFill(Color.web("#F3F8FF"));
        heading.setStroke(Color.web("#243142"));
        heading.setStrokeWidth(0.9);
        heading.setWrappingWidth(contentWidth);

        Text l1 = new Text(line1);
        l1.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        l1.setFill(Color.web("#EAF3FA"));
        l1.setWrappingWidth(contentWidth);

        Text l2 = new Text(line2);
        l2.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        l2.setFill(Color.web("#EAF3FA"));
        l2.setWrappingWidth(contentWidth);

        Text l3 = new Text(line3);
        l3.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        l3.setFill(Color.web("#D2ECFF"));
        l3.setWrappingWidth(contentWidth);

        Button start = createImageButton(buttonImagePath, 220, 100);
        start.setOnAction(e -> onChoose.run());

        VBox content = new VBox(11, heading, l1, l2, l3, start);
        content.setPadding(new Insets(topPadding, horizontalPadding, bottomPadding, horizontalPadding));
        content.setAlignment(Pos.TOP_LEFT);

        ImageView leaf = createDecorSprite(LEAF_PATH, 58, 58);
        StackPane.setAlignment(leaf, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(leaf, new Insets(0, 24, 30, 0));

        ImageView leafTop = createDecorSprite(LEAF_PATH, 46, 46);
        StackPane.setAlignment(leafTop, Pos.TOP_LEFT);
        StackPane.setMargin(leafTop, new Insets(14, 0, 0, 20));

        ImageView leafMid = createDecorSprite(LEAF_PATH, 42, 42);
        StackPane.setAlignment(leafMid, Pos.CENTER_RIGHT);
        StackPane.setMargin(leafMid, new Insets(30, 18, 0, 0));

        ImageView duck = createDecorSprite(DUCK_PATH, 50, 50);
        StackPane.setAlignment(duck, Pos.BOTTOM_LEFT);
        StackPane.setMargin(duck, new Insets(0, 0, 24, 24));

        card.getChildren().addAll(bg, leaf, leafTop, leafMid, duck, content);
        return card;
    }

    private static Pane createFuturisticBackground() {
        return PlayToneBackground.create(VIEW_WIDTH, VIEW_HEIGHT, PlayModeSelectionPageJava.class);
    }

    private static void ensurePlayerName(Stage stage, Scene currentScene, Runnable onReady) {
        if (PlayerProfile.hasPlayerName()) { onReady.run(); return; }
        if (!(currentScene.getRoot() instanceof StackPane root)) { onReady.run(); return; }
        if (root.lookup("#player-name-overlay") != null) return;

        // ── Dark semi-transparent backdrop ──────────────────────────────────
        StackPane overlay = new StackPane();
        overlay.setId("player-name-overlay");
        overlay.setStyle("-fx-background-color: rgba(6,4,18,0.72);");
        overlay.setPickOnBounds(true);

        // ── Compact dialog card ──────────────────────────────────────────────
        VBox card = new VBox(0);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(420);
        card.setMaxWidth(420);
        card.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
        card.setStyle(
                "-fx-background-color: #F8FAFF;" +
                        "-fx-border-color: rgba(0,0,0,0.06);" +
                        "-fx-border-width: 1;" +
                        "-fx-border-radius: 16;" +
                        "-fx-background-radius: 16;"
        );
        javafx.scene.effect.DropShadow shadow = new javafx.scene.effect.DropShadow();
        shadow.setColor(Color.color(0, 0, 0, 0.30));
        shadow.setRadius(32); shadow.setOffsetY(8);
        card.setEffect(shadow);

        // ── Gradient header ──────────────────────────────────────────────────
        VBox cardHeader = new VBox(5);
        cardHeader.setAlignment(Pos.CENTER_LEFT);
        cardHeader.setPadding(new Insets(22, 24, 18, 24));
        cardHeader.setStyle(
                "-fx-background-color: linear-gradient(to right, #1565C0, #2F80ED);" +
                        "-fx-background-radius: 16 16 0 0;"
        );
        Text headerTag = new Text("🦆  ROBOT MAZE");
        headerTag.setFont(Font.font("Orbitron", FontWeight.BOLD, 11));
        headerTag.setFill(Color.color(1, 1, 1, 0.60));
        Text headerTitle = new Text("Nhap ten nguoi choi");
        headerTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        headerTitle.setFill(Color.WHITE);
        Text headerSub = new Text("Ten hien thi tren BXH.");
        headerSub.setFont(Font.font("Arial", FontWeight.NORMAL, 12));
        headerSub.setFill(Color.color(0.80, 0.90, 1.0, 0.70));
        cardHeader.getChildren().addAll(headerTag, headerTitle, headerSub);

        // ── Body ─────────────────────────────────────────────────────────────
        VBox cardBody = new VBox(12);
        cardBody.setPadding(new Insets(20, 24, 22, 24));

        // Input row with char counter
        HBox inputRow = new HBox(8);
        inputRow.setAlignment(Pos.CENTER_LEFT);
        TextField nameField = new TextField();
        nameField.setPromptText("Nhan vao day de nhap ten...");
        HBox.setHgrow(nameField, javafx.scene.layout.Priority.ALWAYS);
        nameField.setStyle(
                "-fx-background-color: #EEF2FF;" +
                        "-fx-text-fill: #1F2D3A;" +
                        "-fx-prompt-text-fill: #8A9AA1;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-family: 'Arial';" +
                        "-fx-border-color: transparent transparent #2F80ED transparent;" +
                        "-fx-border-width: 0 0 2 0;" +
                        "-fx-background-radius: 6 6 0 0;" +
                        "-fx-padding: 9 10 9 10;"
        );
        Text counter = new Text("0 / 24");
        counter.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        counter.setFill(Color.web("#7A8DA0"));
        nameField.textProperty().addListener((obs, ov, nv) -> {
            if (nv != null && nv.length() > 24) {
                nameField.setText(ov != null ? ov : "");
                return;
            }
            int len = nv == null ? 0 : nv.length();
            counter.setText(len + " / 24");
            counter.setFill(len > 20 ? Color.web("#FF5252") : Color.web("#7A8DA0"));
        });
        inputRow.getChildren().addAll(nameField, counter);

        Text error = new Text("");
        error.setFont(Font.font("Arial", FontWeight.BOLD, 12));
        error.setFill(Color.web("#FF5252"));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new NeonButton("HUY", Color.web("#90A4AE"), 12, 6, 14, 7);
        Button save   = new NeonButton("XAC NHAN  ▶", Color.web("#2F80ED"), 12, 6, 14, 7);

        cancel.setOnAction(e -> root.getChildren().remove(overlay));
        save.setOnAction(e -> {
            String rawName = nameField.getText() == null ? "" : nameField.getText().trim();
            if (rawName.isEmpty()) {
                error.setText("⚠  Ten khong duoc de trong.");
                return;
            }
            PlayerProfile.setCurrentPlayerName(rawName);
            root.getChildren().remove(overlay);
            onReady.run();
        });
        nameField.setOnAction(e -> save.fire());

        actions.getChildren().addAll(cancel, save);
        cardBody.getChildren().addAll(inputRow, error, actions);
        card.getChildren().addAll(cardHeader, cardBody);

        overlay.getChildren().add(card);
        AppFonts.applyTo(overlay);
        root.getChildren().add(overlay);
        nameField.requestFocus();
    }

    private static Button createImageButton(String imagePath, double width, double height) {
        Button button = new Button();
        button.setPrefWidth(width);
        button.setMinWidth(width);
        button.setPrefHeight(height);
        button.setMinHeight(height);
        button.setStyle("-fx-background-color: transparent; -fx-padding: 0; -fx-border-color: transparent;");

        Image rawImage = loadImageResource(imagePath);
        Image cleanedImage = removeCornerBackground(rawImage);
        ImageView imageView = new ImageView(cleanedImage);
        imageView.setViewport(detectFrameViewport(cleanedImage));
        imageView.setFitWidth(width);
        imageView.setFitHeight(height);
        imageView.setPreserveRatio(false);
        imageView.setSmooth(false);
        button.setGraphic(imageView);

        button.setOnMouseEntered(e -> {
            button.setScaleX(1.02);
            button.setScaleY(1.02);
        });
        button.setOnMouseExited(e -> {
            button.setScaleX(1.0);
            button.setScaleY(1.0);
        });
        return button;
    }

    private static ImageView createDecorSprite(String imagePath, double width, double height) {
        Image rawImage = loadImageResource(imagePath);
        Image cleanedImage = removeCornerBackground(rawImage);
        ImageView sprite = new ImageView(cleanedImage);
        sprite.setViewport(detectFrameViewport(cleanedImage));
        sprite.setFitWidth(width);
        sprite.setFitHeight(height);
        sprite.setPreserveRatio(true);
        sprite.setSmooth(false);
        sprite.setOpacity(0.65);
        sprite.setBlendMode(BlendMode.MULTIPLY);
        sprite.setMouseTransparent(true);
        return sprite;
    }

    private static Rectangle2D detectFrameViewport(Image image) {
        PixelReader reader = image.getPixelReader();
        if (reader == null) {
            return new Rectangle2D(0, 0, image.getWidth(), image.getHeight());
        }

        int width = (int) image.getWidth();
        int height = (int) image.getHeight();
        int minX = width;
        int minY = height;
        int maxX = -1;
        int maxY = -1;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                if (alpha < 16) continue;

                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;
                boolean nearWhite = red > 245 && green > 245 && blue > 245;
                if (nearWhite) continue;

                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }

        if (maxX < minX || maxY < minY) {
            return new Rectangle2D(0, 0, image.getWidth(), image.getHeight());
        }
        return new Rectangle2D(minX, minY, (maxX - minX) + 1, (maxY - minY) + 1);
    }

    private static Image removeWhiteBackground(Image source) {
        PixelReader reader = source.getPixelReader();
        if (reader == null) return source;

        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int argb = reader.getArgb(x, y);
                int alpha = (argb >>> 24) & 0xFF;
                int red = (argb >>> 16) & 0xFF;
                int green = (argb >>> 8) & 0xFF;
                int blue = argb & 0xFF;
                boolean whiteBackground = alpha > 0 && red > 245 && green > 245 && blue > 245;
                writer.setArgb(x, y, whiteBackground ? 0x00000000 : argb);
            }
        }
        return output;
    }

    private static Image removeCornerBackground(Image source) {
        PixelReader reader = source.getPixelReader();
        if (reader == null) return source;

        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage output = new WritableImage(width, height);
        PixelWriter writer = output.getPixelWriter();

        int corner1 = reader.getArgb(0, 0);
        int corner2 = reader.getArgb(Math.max(0, width - 1), 0);
        int corner3 = reader.getArgb(0, Math.max(0, height - 1));
        int corner4 = reader.getArgb(Math.max(0, width - 1), Math.max(0, height - 1));

        int total = width * height;
        boolean[] removeMask = new boolean[total];
        ArrayDeque<Integer> queue = new ArrayDeque<>();

        for (int x = 0; x < width; x++) {
            queue.add(x);
            queue.add((height - 1) * width + x);
        }
        for (int y = 1; y < height - 1; y++) {
            queue.add(y * width);
            queue.add(y * width + (width - 1));
        }

        while (!queue.isEmpty()) {
            int idx = queue.poll();
            if (idx < 0 || idx >= total || removeMask[idx]) continue;

            int x = idx % width;
            int y = idx / width;
            int argb = reader.getArgb(x, y);
            boolean nearCornerBg =
                    isNearColor(argb, corner1, 20) ||
                            isNearColor(argb, corner2, 20) ||
                            isNearColor(argb, corner3, 20) ||
                            isNearColor(argb, corner4, 20);
            if (!nearCornerBg) continue;

            removeMask[idx] = true;
            if (x > 0) queue.add(idx - 1);
            if (x < width - 1) queue.add(idx + 1);
            if (y > 0) queue.add(idx - width);
            if (y < height - 1) queue.add(idx + width);
        }

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idx = y * width + x;
                int argb = reader.getArgb(x, y);
                writer.setArgb(x, y, removeMask[idx] ? 0x00000000 : argb);
            }
        }

        return output;
    }

    private static boolean isNearColor(int argbA, int argbB, int threshold) {
        int aA = (argbA >>> 24) & 0xFF;
        int rA = (argbA >>> 16) & 0xFF;
        int gA = (argbA >>> 8) & 0xFF;
        int bA = argbA & 0xFF;
        int aB = (argbB >>> 24) & 0xFF;
        int rB = (argbB >>> 16) & 0xFF;
        int gB = (argbB >>> 8) & 0xFF;
        int bB = argbB & 0xFF;

        return Math.abs(aA - aB) <= threshold
                && Math.abs(rA - rB) <= threshold
                && Math.abs(gA - gB) <= threshold
                && Math.abs(bA - bB) <= threshold;
    }

    private static Image loadImageResource(String resourcePath) {
        var resource = PlayModeSelectionPageJava.class.getResource(resourcePath);
        if (resource == null) {
            return new WritableImage(4, 4);
        }
        return new Image(resource.toExternalForm());
    }
}
