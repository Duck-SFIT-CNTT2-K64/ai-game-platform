package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.PlayerProfile;
import com.nhom_01.robot_pathfinding.ui.PlayGamePage;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.GameCard;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
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

public final class AlgorithmSelectionPageJava {

    private static final double VIEW_WIDTH  = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    private static final double VIEW_HEIGHT = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
    private static final String BTN_BFS_PATH = "/image/assets/BFS.jpg";
    private static final String BTN_DFS_PATH = "/image/assets/DFS.jpg";
    private static final String BTN_ASTAR_PATH = "/image/assets/AStar.jpg";
    private static final String BTN_BACK_DIFFICULTY_PATH = "/image/assets/BackDifficulty.jpg";
    private static final String FRAME_BIG_PATH = "/image/assets/frame_big.png";
    private static final String FRAME_LONG_PATH = "/image/assets/frame_long.jpg";
    private static final String LEAF_PATH = "/image/assets/la.jpg";
    private static final String DUCK_PATH = "/image/assets/vit.jpg";

    private AlgorithmSelectionPageJava() {
    }

    public static void showOnStage(Stage stage, Scene menuScene) {
        Scene scene = buildScene(stage, menuScene, "MEDIUM");
        MenuAudioManager.wireScene(scene);
        MenuAudioManager.startTheme();
        stage.setScene(scene);
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

        Text title = new Text("SELECT ALGORITHM");
        title.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 42));
        title.setFill(Color.web("#FFD15A"));
        title.setStroke(Color.web("#6B4518"));
        title.setStrokeWidth(1.8);

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.color(0.16, 0.08, 0.02, 0.55));
        titleGlow.setRadius(20);
        title.setEffect(titleGlow);

        Text subtitle = new Text("DIFFICULTY: " + difficulty + "  |  CHOOSE HOW THE ROBOT WILL SEARCH");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        subtitle.setFill(Color.web("#F5E7C4"));

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        HBox cards = new HBox(22);
        cards.setAlignment(Pos.TOP_CENTER);

        StackPane bfsCard = createAlgorithmCard(
            "BFS",
            "🟢",
            "BFS",
            "Expands nodes level by level from the start point.",
            "Guarantees shortest path on unweighted grids.",
            Color.web("#00FF9C"),
            BTN_BFS_PATH,
            () -> {
                PlayerProfile.setCurrentPlayerName("BFS");
                PlayGamePage.showOnStage(stage, stage.getScene(), difficulty, "BFS");
            }
        );

        StackPane dfsCard = createAlgorithmCard(
            "DFS",
            "🟠",
            "DFS",
            "Follows one branch deeply before backtracking.",
            "Fast to explore, but may return longer routes.",
            Color.web("#FFB800"),
            BTN_DFS_PATH,
            () -> {
                PlayerProfile.setCurrentPlayerName("DFS");
                PlayGamePage.showOnStage(stage, stage.getScene(), difficulty, "DFS");
            }
        );

        StackPane aStarCard = createAlgorithmCard(
            "A*",
            "🔴",
            "A*",
            "Combines path cost and heuristic distance.",
            "Usually reaches the goal faster and smarter.",
            Color.web("#FF5B5B"),
            BTN_ASTAR_PATH,
            () -> {
                PlayerProfile.setCurrentPlayerName("A*");
                PlayGamePage.showOnStage(stage, stage.getScene(), difficulty, "A*");
            }
        );

        cards.getChildren().addAll(bfsCard, dfsCard, aStarCard);

        HBox supportRow = createSupportRow();
        content.getChildren().addAll(cards, supportRow);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(1288);

        Button back = createImageButton(BTN_BACK_DIFFICULTY_PATH, 300, 74);
        back.setOnAction(e -> stage.setScene(previousScene));

        actions.getChildren().add(back);

        page.getChildren().addAll(heading, content, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        AppFonts.applyTo(root);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static Pane createFuturisticBackground() {
        return PlayToneBackground.create(VIEW_WIDTH, VIEW_HEIGHT, AlgorithmSelectionPageJava.class);
    }

    private static StackPane createAlgorithmCard(String level, String icon, String algorithm,
                                            String descriptionA, String descriptionB, Color accent,
                                            String buttonImagePath,
                                            Runnable onChoose) {
        final double frameWidth = 415;
        final double frameHeight = 330;
        final double horizontalPadding = 28;
        final double topPadding = 20;
        final double bottomPadding = 20;
        final double contentWidth = frameWidth - (horizontalPadding * 2);

        StackPane card = new StackPane();
        card.setPrefSize(frameWidth, frameHeight);
        card.setMinSize(frameWidth, frameHeight);
        card.setMaxSize(frameWidth, frameHeight);
        card.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        Image frameImage = removeWhiteBackground(loadImageResource(FRAME_BIG_PATH));
        ImageView bg = new ImageView(frameImage);
        bg.setViewport(detectFrameViewport(frameImage));
        bg.setFitWidth(frameWidth);
        bg.setFitHeight(frameHeight);
        bg.setPreserveRatio(false);
        bg.setSmooth(false);

        Text heading = new Text(icon + "  " + level);
        heading.setFont(Font.font("Orbitron", FontWeight.BOLD, 30));
        heading.setFill(accent);
        heading.setWrappingWidth(contentWidth);

        Text algorithmTitle = new Text("ALGORITHM: " + algorithm);
        algorithmTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 20));
        algorithmTitle.setFill(Color.web("#EAF3FA"));
        algorithmTitle.setWrappingWidth(contentWidth);

        Text lineA = new Text(descriptionA);
        lineA.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        lineA.setFill(Color.web("#4F5B62"));
        lineA.setWrappingWidth(contentWidth);

        Text lineB = new Text(descriptionB);
        lineB.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        lineB.setFill(Color.web("#4F5B62"));
        lineB.setWrappingWidth(contentWidth);

        Button choose = createImageButton(buttonImagePath, 220, 86);
        choose.setOnAction(e -> onChoose.run());
        VBox content = new VBox(9, heading, algorithmTitle, lineA, lineB, choose);
        content.setPadding(new Insets(topPadding, horizontalPadding, bottomPadding, horizontalPadding));
        content.setAlignment(Pos.TOP_LEFT);

        ImageView leaf = createDecorSprite(LEAF_PATH, 54, 54);
        StackPane.setAlignment(leaf, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(leaf, new Insets(0, 20, 34, 0));
        ImageView duck = createDecorSprite(DUCK_PATH, 48, 48);
        StackPane.setAlignment(duck, Pos.BOTTOM_LEFT);
        StackPane.setMargin(duck, new Insets(0, 0, 24, 24));

        card.getChildren().addAll(bg, leaf, duck, content);
        return card;
    }

    private static HBox createSupportRow() {
        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);

        StackPane guidance = createInfoPanel(
            "HOW TO CHOOSE",
            "BFS: stable and shortest routes.\n" +
                "DFS: exploratory and sometimes surprising.\n" +
                "A*: smart balance between speed and precision.",
            Color.web("#8FE8F4")
        );

        StackPane recommendation = createInfoPanel(
            "RECOMMENDATION",
            "For EASY, start with BFS.\n" +
                "For MEDIUM, DFS gives a dynamic challenge.\n" +
                "For HARD, A* keeps performance consistent.",
            Color.web("#FFB800")
        );

        row.getChildren().addAll(guidance, recommendation);
        return row;
    }

    private static StackPane createInfoPanel(String headingText, String bodyText, Color headingColor) {
        final double frameWidth = 632;
        final double frameHeight = 178;
        final double horizontalPadding = 38;
        final double verticalPadding = 24;
        final double contentWidth = frameWidth - (horizontalPadding * 2);

        StackPane panel = new StackPane();
        panel.setPrefSize(frameWidth, frameHeight);
        panel.setMinSize(frameWidth, frameHeight);
        panel.setMaxSize(frameWidth, frameHeight);
        panel.setStyle("-fx-background-color: transparent; -fx-padding: 0;");

        Image frameImage = removeWhiteBackground(loadImageResource(FRAME_LONG_PATH));
        ImageView bg = new ImageView(frameImage);
        bg.setViewport(detectFrameViewport(frameImage));
        bg.setFitWidth(frameWidth);
        bg.setFitHeight(frameHeight);
        bg.setPreserveRatio(false);
        bg.setSmooth(false);

        Text heading = new Text(headingText);
        heading.setFont(Font.font("Press Start 2P", FontWeight.BOLD, 14));
        heading.setFill(headingColor);
        heading.setWrappingWidth(contentWidth);

        Text body = new Text(bodyText);
        body.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        body.setFill(Color.WHITE);
        body.setWrappingWidth(contentWidth);
        body.setLineSpacing(2);

        VBox content = new VBox(8, heading, body);
        content.setPadding(new Insets(verticalPadding, horizontalPadding, verticalPadding, horizontalPadding));
        content.setAlignment(Pos.TOP_LEFT);

        ImageView leaf = createDecorSprite(LEAF_PATH, 40, 40);
        StackPane.setAlignment(leaf, Pos.BOTTOM_RIGHT);
        StackPane.setMargin(leaf, new Insets(0, 22, 14, 0));
        ImageView duck = createDecorSprite(DUCK_PATH, 34, 34);
        StackPane.setAlignment(duck, Pos.BOTTOM_LEFT);
        StackPane.setMargin(duck, new Insets(0, 0, 10, 22));

        panel.getChildren().addAll(bg, leaf, duck, content);
        return panel;
    }

    private static Button createImageButton(String imagePath, double width, double height) {
        Button button = new Button();
        button.setPrefWidth(width);
        button.setMinWidth(width);
        button.setPrefHeight(height);
        button.setMinHeight(height);
        button.setCursor(Cursor.HAND);
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
                if (red > 245 && green > 245 && blue > 245) continue;
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
        var resource = AlgorithmSelectionPageJava.class.getResource(resourcePath);
        if (resource == null) {
            return new WritableImage(4, 4);
        }
        return new Image(resource.toExternalForm());
    }
}
