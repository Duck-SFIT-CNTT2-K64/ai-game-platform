package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.ui.PlayGamePage;
import com.nhom_01.robot_pathfinding.ui.components.GameCard;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
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
            () -> PlayGamePage.showPlayerOnStage(stage, stage.getScene(), difficulty)
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
}
