package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.PlayerProfile;
import com.nhom_01.robot_pathfinding.ui.PlayGamePage;
import com.nhom_01.robot_pathfinding.ui.components.GameCard;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
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

public final class PlayModeSelectionPageJava {

    private static final double VIEW_WIDTH = 1400;
    private static final double VIEW_HEIGHT = 800;

    private PlayModeSelectionPageJava() {
    }

    public static void showOnStage(Stage stage, Scene previousScene, String difficulty) {
        stage.setScene(buildScene(stage, previousScene, difficulty));
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
        title.setFill(Color.web("#7FE4FF"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#7FE4FF"));
        titleGlow.setRadius(26);
        title.setEffect(titleGlow);

        Text subtitle = new Text("DIFFICULTY: " + difficulty + "  |  PLAY YOURSELF OR LET AI HANDLE IT");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 17));
        subtitle.setFill(Color.web("#D7E8F4"));

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);

        HBox cards = new HBox(24);
        cards.setAlignment(Pos.CENTER);

        VBox playerCard = createModeCard(
            "PLAYER",
            "🕹",
            "Control robot manually with arrow keys.",
            "Great for learning maze patterns and reacting to bombs.",
            "Use keyboard: UP / DOWN / LEFT / RIGHT",
            Color.web("#5EA5FF"),
            () -> ensurePlayerName(stage, stage.getScene(), () ->
                PlayGamePage.showPlayerOnStage(stage, stage.getScene(), difficulty)
            )
        );

        VBox botCard = createModeCard(
            "BOT",
            "🤖",
            "AI solves maze automatically based on algorithm.",
            "Useful to observe path quality and compare strategies.",
            "Next step: choose BFS / DFS / A*",
            Color.web("#2BD99F"),
            () -> AlgorithmSelectionPageJava.showOnStage(stage, stage.getScene(), difficulty)
        );

        cards.getChildren().addAll(playerCard, botCard);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(1288);

        Button back = new NeonButton("BACK TO DIFFICULTY", Color.web("#CCCCCC"), 14, 8, 14, 8);
        back.setOnAction(e -> stage.setScene(previousScene));
        actions.getChildren().add(back);

        page.getChildren().addAll(heading, cards, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.16);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static VBox createModeCard(String title, String icon, String line1, String line2,
                                       String line3, Color accent, Runnable onChoose) {
        GameCard card = new GameCard(title, icon, accent, 540, 360);

        Text l1 = card.createBodyText(line1, 500);
        Text l2 = card.createBodyText(line2, 500);
        Text l3 = card.createBodyText(line3, 500);
        l3.setFill(Color.web("#F2F8FD"));

        Button start = new NeonButton("CHOOSE " + title, accent, 15, 9, 16, 8);
        start.setPrefWidth(210);
        start.setMinWidth(210);
        start.setOnAction(e -> onChoose.run());

        card.addBodyNodes(l1, l2, l3, start);
        return card;
    }

    private static Pane createFuturisticBackground() {
        Pane bgPane = new Pane();
        bgPane.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);

        Canvas canvas = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int y = 0; y < (int) VIEW_HEIGHT; y++) {
            double ratio = y / VIEW_HEIGHT;
            int r = (int) (16 + (42 - 16) * ratio);
            int g = (int) (30 + (68 - 30) * ratio);
            int b = (int) (46 + (96 - 46) * ratio);
            gc.setStroke(Color.rgb(r, g, b));
            gc.strokeLine(0, y, VIEW_WIDTH, y);
        }

        gc.setStroke(Color.color(0.45, 0.82, 1.0, 0.20));
        gc.setLineWidth(1);
        int gridSize = 46;
        for (int x = 0; x < VIEW_WIDTH; x += gridSize) {
            gc.strokeLine(x, 0, x, VIEW_HEIGHT);
        }
        for (int y = 0; y < VIEW_HEIGHT; y += gridSize) {
            gc.strokeLine(0, y, VIEW_WIDTH, y);
        }

        bgPane.getChildren().add(canvas);
        return bgPane;
    }

    private static void ensurePlayerName(Stage stage, Scene currentScene, Runnable onReady) {
        if (PlayerProfile.hasPlayerName()) {
            onReady.run();
            return;
        }

        if (!(currentScene.getRoot() instanceof StackPane root)) {
            onReady.run();
            return;
        }

        if (root.lookup("#player-name-overlay") != null) {
            return;
        }

        StackPane overlay = new StackPane();
        overlay.setId("player-name-overlay");
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.72);");

        VBox dialog = new VBox(12);
        dialog.setAlignment(Pos.CENTER_LEFT);
        dialog.setPadding(new Insets(22));
        dialog.setPrefWidth(520);
        dialog.setStyle(
            "-fx-background-color: rgba(12,20,32,0.96);" +
            "-fx-border-color: rgba(0,255,255,0.42);" +
            "-fx-border-width: 1.8;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        Text title = new Text("ENTER PLAYER NAME");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 26));
        title.setFill(Color.web("#00FFFF"));

        Text helper = new Text("Ranking requires a player name before game starts.");
        helper.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        helper.setFill(Color.web("#C9DCEA"));

        TextField nameField = new TextField();
        nameField.setPromptText("Your name (max 24 chars)");
        nameField.setStyle(
            "-fx-background-color: rgba(6,12,20,0.96);" +
            "-fx-text-fill: #DFF7FF;" +
            "-fx-prompt-text-fill: #7EA2B8;" +
            "-fx-font-size: 15px;" +
            "-fx-font-family: 'Arial';" +
            "-fx-border-color: rgba(0,255,255,0.32);" +
            "-fx-border-width: 1.2;"
        );

        Text error = new Text("");
        error.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        error.setFill(Color.web("#FF8DA6"));

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        Button cancel = new NeonButton("CANCEL", Color.web("#CCCCCC"), 13, 7, 12, 5);
        Button save = new NeonButton("CONFIRM", Color.web("#00FF9C"), 13, 7, 12, 5);

        cancel.setOnAction(e -> root.getChildren().remove(overlay));
        save.setOnAction(e -> {
            String rawName = nameField.getText() == null ? "" : nameField.getText().trim();
            if (rawName.isEmpty()) {
                error.setText("Name is required.");
                return;
            }
            PlayerProfile.setCurrentPlayerName(rawName);
            root.getChildren().remove(overlay);
            onReady.run();
        });

        nameField.setOnAction(e -> save.fire());

        actions.getChildren().addAll(cancel, save);
        dialog.getChildren().addAll(title, helper, nameField, error, actions);
        overlay.getChildren().add(dialog);

        root.getChildren().add(overlay);
        nameField.requestFocus();
    }
}
