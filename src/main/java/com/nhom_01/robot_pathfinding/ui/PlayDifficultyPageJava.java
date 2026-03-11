package com.nhom_01.robot_pathfinding.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public final class PlayDifficultyPageJava {

    private static final double VIEW_WIDTH = 1400;
    private static final double VIEW_HEIGHT = 800;

    private PlayDifficultyPageJava() {
    }

    public static void showOnStage(Stage stage, Scene menuScene) {
        stage.setScene(buildScene(stage, menuScene));
    }

    private static Scene buildScene(Stage stage, Scene menuScene) {
        StackPane root = new StackPane();
        root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
        root.getChildren().add(createFuturisticBackground());

        VBox page = new VBox(20);
        page.setPadding(new Insets(24, 56, 24, 56));
        page.setAlignment(Pos.TOP_CENTER);

        Text title = new Text("CHOOSE DIFFICULTY");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 58));
        title.setFill(Color.web("#00FFFF"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#00FFFF"));
        titleGlow.setRadius(26);
        title.setEffect(titleGlow);

        Text subtitle = new Text("PICK THE CHALLENGE LEVEL BEFORE ENTERING THE MAZE");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web("#B5C7D6"));

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        HBox cards = new HBox(22);
        cards.setAlignment(Pos.TOP_CENTER);

        cards.getChildren().addAll(
            createDifficultyCard(
                "EASY",
                "🟢",
                "Guided mode with more forgiving routes.",
                "Fewer dead ends and smoother learning curve.",
                "Estimated clear time: 3-5 min",
                Color.web("#00FF9C"),
                () -> AlgorithmSelectionPageJava.showOnStage(stage, stage.getScene(), "EASY")
            ),
            createDifficultyCard(
                "MEDIUM",
                "🟠",
                "Balanced challenge with mixed path patterns.",
                "Good for regular play and skill growth.",
                "Estimated clear time: 5-8 min",
                Color.web("#FFB800"),
                () -> AlgorithmSelectionPageJava.showOnStage(stage, stage.getScene(), "MEDIUM")
            ),
            createDifficultyCard(
                "HARD",
                "🔴",
                "Complex maze with misleading branches.",
                "Best for players who want strategic pressure.",
                "Estimated clear time: 8-12 min",
                Color.web("#FF6B6B"),
                () -> AlgorithmSelectionPageJava.showOnStage(stage, stage.getScene(), "HARD")
            )
        );

        HBox supportRow = createSupportRow();
        content.getChildren().addAll(cards, supportRow);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(1288);

        Button back = createActionButton("BACK TO MENU", Color.web("#CCCCCC"));
        back.setOnAction(e -> stage.setScene(menuScene));
        actions.getChildren().add(back);

        page.getChildren().addAll(heading, content, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.18);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static Pane createFuturisticBackground() {
        Pane bgPane = new Pane();
        bgPane.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);

        Canvas canvas = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int y = 0; y < (int) VIEW_HEIGHT; y++) {
            double ratio = y / VIEW_HEIGHT;
            int r = (int) (13 + (27 - 13) * ratio);
            int g = (int) (17 + (51 - 17) * ratio);
            int b = (int) (23 + (48 - 23) * ratio);
            gc.setStroke(Color.rgb(r, g, b));
            gc.strokeLine(0, y, VIEW_WIDTH, y);
        }

        gc.setStroke(Color.color(0, 0.5, 0.85, 0.14));
        gc.setLineWidth(1);
        int gridSize = 50;
        for (int x = 0; x < VIEW_WIDTH; x += gridSize) {
            gc.strokeLine(x, 0, x, VIEW_HEIGHT);
        }
        for (int y = 0; y < VIEW_HEIGHT; y += gridSize) {
            gc.strokeLine(0, y, VIEW_WIDTH, y);
        }

        gc.setFill(Color.color(0, 1, 1, 0.22));
        for (int x = 0; x < VIEW_WIDTH; x += gridSize) {
            for (int y = 0; y < VIEW_HEIGHT; y += gridSize) {
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        bgPane.getChildren().add(canvas);
        return bgPane;
    }

    private static VBox createDifficultyCard(String level, String icon, String line1,
                                             String line2, String duration, Color accent,
                                             Runnable onChoose) {
        VBox card = new VBox(12);
        card.setAlignment(Pos.TOP_LEFT);
        card.setPadding(new Insets(18));
        card.setPrefSize(415, 330);
        card.setMinSize(415, 330);
        card.setCursor(Cursor.HAND);

        String baseStyle =
            "-fx-background-color: rgba(8, 17, 30, 0.78);" +
            "-fx-border-color: rgba(0, 255, 255, 0.35);" +
            "-fx-border-width: 1.6;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;";

        String hoverStyle =
            "-fx-background-color: rgba(12, 28, 44, 0.90);" +
            "-fx-border-color: " + toRgba(accent, 0.95) + ";" +
            "-fx-border-width: 2.1;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;";

        card.setStyle(baseStyle);

        DropShadow normalGlow = new DropShadow();
        normalGlow.setColor(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.24));
        normalGlow.setRadius(16);
        card.setEffect(normalGlow);

        Text levelText = new Text(icon + "  " + level);
        levelText.setFont(Font.font("Orbitron", FontWeight.BOLD, 30));
        levelText.setFill(accent);

        Text l1 = createBodyText(line1);
        Text l2 = createBodyText(line2);
        Text eta = new Text(duration);
        eta.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        eta.setFill(Color.web("#D9E9F6"));

        Button choose = createActionButton("START " + level, accent);
        choose.setPrefWidth(170);

        choose.setOnAction(e -> {
            card.setStyle(hoverStyle);
            card.setTranslateY(-2);
            onChoose.run();
        });

        card.setOnMouseEntered(e -> {
            card.setStyle(hoverStyle);
            card.setTranslateY(-4);
            DropShadow hoverGlow = new DropShadow();
            hoverGlow.setColor(Color.color(accent.getRed(), accent.getGreen(), accent.getBlue(), 0.45));
            hoverGlow.setRadius(22);
            card.setEffect(hoverGlow);
        });

        card.setOnMouseExited(e -> {
            card.setStyle(baseStyle);
            card.setTranslateY(0);
            card.setEffect(normalGlow);
        });

        card.getChildren().addAll(levelText, l1, l2, eta, choose);
        return card;
    }

    private static HBox createSupportRow() {
        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);

        VBox missionPanel = createInfoPanel(
            "MISSION BRIEF",
            "- Reach the goal with minimum wrong turns.\n" +
                "- Each difficulty changes maze complexity.\n" +
                "- You can return and switch mode anytime.",
            Color.web("#8FE8F4")
        );

        VBox tipPanel = createInfoPanel(
            "QUICK TIP",
            "Start with MEDIUM for a balanced first run.\n" +
                "Move to HARD after learning route patterns.\n" +
                "Use OPTIONS to tune visual comfort settings.",
            Color.web("#FFB800")
        );

        row.getChildren().addAll(missionPanel, tipPanel);
        return row;
    }

    private static VBox createInfoPanel(String headingText, String bodyText, Color headingColor) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16, 18, 16, 18));
        panel.setPrefSize(632, 178);
        panel.setMinSize(632, 178);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setStyle(
            "-fx-background-color: rgba(10, 22, 34, 0.72);" +
            "-fx-border-color: rgba(0, 255, 255, 0.30);" +
            "-fx-border-width: 1.4;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(0, 1, 1, 0.20));
        glow.setRadius(16);
        panel.setEffect(glow);

        Text heading = new Text(headingText);
        heading.setFont(Font.font("Orbitron", FontWeight.BOLD, 22));
        heading.setFill(headingColor);

        Text body = new Text(bodyText);
        body.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        body.setFill(Color.web("#C9DCEA"));
        body.setWrappingWidth(590);

        panel.getChildren().addAll(heading, body);
        return panel;
    }

    private static Text createBodyText(String text) {
        Text node = new Text(text);
        node.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        node.setFill(Color.web("#C9DCEA"));
        node.setWrappingWidth(372);
        return node;
    }

    private static String toRgba(Color c, double alpha) {
        return String.format("rgba(%d,%d,%d,%.2f)",
            (int) (c.getRed() * 255),
            (int) (c.getGreen() * 255),
            (int) (c.getBlue() * 255),
            alpha
        );
    }

    private static Button createActionButton(String text, Color color) {
        Button btn = new Button(text);
        String rgb = String.format("rgb(%d,%d,%d)",
            (int) (color.getRed() * 255),
            (int) (color.getGreen() * 255),
            (int) (color.getBlue() * 255)
        );

        btn.setCursor(Cursor.HAND);
        btn.setStyle(
            "-fx-background-color: transparent;" +
            "-fx-text-fill: " + rgb + ";" +
            "-fx-font-size: 14px;" +
            "-fx-font-weight: bold;" +
            "-fx-font-family: 'Arial';" +
            "-fx-padding: 9 16 9 16;" +
            "-fx-border-color: " + rgb + ";" +
            "-fx-border-width: 1.7;" +
            "-fx-border-radius: 7;"
        );

        DropShadow shadow = new DropShadow();
        shadow.setColor(color);
        shadow.setRadius(12);
        shadow.setSpread(0.18);
        btn.setEffect(shadow);

        return btn;
    }
}
