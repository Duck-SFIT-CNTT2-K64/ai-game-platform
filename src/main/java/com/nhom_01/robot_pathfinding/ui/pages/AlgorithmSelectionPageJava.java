package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.PlayerProfile;
import com.nhom_01.robot_pathfinding.ui.PlayGamePage;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.GameCard;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
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

public final class AlgorithmSelectionPageJava {

    private static final double VIEW_WIDTH = 1400;
    private static final double VIEW_HEIGHT = 800;

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
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 58));
        title.setFill(Color.web("#1F2D3A"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.24));
        titleGlow.setRadius(16);
        title.setEffect(titleGlow);

        Text subtitle = new Text("DIFFICULTY: " + difficulty + "  |  CHOOSE HOW THE ROBOT WILL SEARCH");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web("#4F5B62"));

        VBox heading = new VBox(8, title, subtitle);
        heading.setAlignment(Pos.CENTER);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        HBox cards = new HBox(22);
        cards.setAlignment(Pos.TOP_CENTER);

        VBox bfsCard = createAlgorithmCard(
            "BFS",
            "🟢",
            "BFS",
            "Expands nodes level by level from the start point.",
            "Guarantees shortest path on unweighted grids.",
            Color.web("#00FF9C"),
            () -> {
                PlayerProfile.setCurrentPlayerName("BFS");
                PlayGamePage.showOnStage(stage, stage.getScene(), difficulty, "BFS");
            }
        );

        VBox dfsCard = createAlgorithmCard(
            "DFS",
            "🟠",
            "DFS",
            "Follows one branch deeply before backtracking.",
            "Fast to explore, but may return longer routes.",
            Color.web("#FFB800"),
            () -> {
                PlayerProfile.setCurrentPlayerName("DFS");
                PlayGamePage.showOnStage(stage, stage.getScene(), difficulty, "DFS");
            }
        );

        VBox aStarCard = createAlgorithmCard(
            "A*",
            "🔴",
            "A*",
            "Combines path cost and heuristic distance.",
            "Usually reaches the goal faster and smarter.",
            Color.web("#FF5B5B"),
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

        Button back = createActionButton("BACK TO DIFFICULTY", Color.web("#607D8B"));
        back.setOnAction(e -> stage.setScene(previousScene));

        actions.getChildren().add(back);

        page.getChildren().addAll(heading, content, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static Pane createFuturisticBackground() {
        return PlayToneBackground.create(VIEW_WIDTH, VIEW_HEIGHT, AlgorithmSelectionPageJava.class);
    }

    private static VBox createAlgorithmCard(String level, String icon, String algorithm,
                                            String descriptionA, String descriptionB, Color accent,
                                            Runnable onChoose) {
        GameCard card = new GameCard(level, icon, accent, 415, 330);

        Text algorithmTitle = new Text("ALGORITHM: " + algorithm);
        algorithmTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 22));
        algorithmTitle.setFill(Color.web("#2D3E50"));

        Text lineA = card.createBodyText(descriptionA, 372);
        Text lineB = card.createBodyText(descriptionB, 372);

        Button choose = new NeonButton("USE " + algorithm, accent, 14, 8, 14, 8);
        choose.setPrefWidth(180);
        choose.setMinWidth(180);
        choose.setOnAction(e -> onChoose.run());

        card.addBodyNodes(algorithmTitle, lineA, lineB, choose);

        return card;
    }

    private static HBox createSupportRow() {
        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);

        VBox guidance = createInfoPanel(
            "HOW TO CHOOSE",
            "BFS: stable and shortest routes.\n" +
                "DFS: exploratory and sometimes surprising.\n" +
                "A*: smart balance between speed and precision.",
            Color.web("#8FE8F4")
        );

        VBox recommendation = createInfoPanel(
            "RECOMMENDATION",
            "For EASY, start with BFS.\n" +
                "For MEDIUM, DFS gives a dynamic challenge.\n" +
                "For HARD, A* keeps performance consistent.",
            Color.web("#FFB800")
        );

        row.getChildren().addAll(guidance, recommendation);
        return row;
    }

    private static VBox createInfoPanel(String headingText, String bodyText, Color headingColor) {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(16, 18, 16, 18));
        panel.setPrefSize(632, 178);
        panel.setMinSize(632, 178);
        panel.setAlignment(Pos.TOP_LEFT);
        panel.setStyle(
            "-fx-background-color: rgba(255,255,255,0.94);" +
            "-fx-border-color: rgba(0,0,0,0.10);" +
            "-fx-border-width: 1.4;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(0.12, 0.16, 0.20, 0.14));
        glow.setRadius(10);
        panel.setEffect(glow);

        Text heading = new Text(headingText);
        heading.setFont(Font.font("Orbitron", FontWeight.BOLD, 22));
        heading.setFill(headingColor);

        Text body = new Text(bodyText);
        body.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        body.setFill(Color.web("#4F5B62"));
        body.setWrappingWidth(590);

        panel.getChildren().addAll(heading, body);
        return panel;
    }

    private static Button createActionButton(String text, Color color) {
        return new NeonButton(text, color);
    }
}
