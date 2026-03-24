package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.ui.components.GameCard;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
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

public final class PlayDifficultyPageJava {

    private static final double VIEW_WIDTH  = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    private static final double VIEW_HEIGHT = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

    private PlayDifficultyPageJava() {
    }

    public static void showOnStage(Stage stage, Scene menuScene) {
        Scene scene = buildScene(stage, menuScene);
        MenuAudioManager.wireScene(scene);
        MenuAudioManager.startTheme();
        stage.setScene(scene);
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
        title.setFill(Color.web("#1F2D3A"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.25));
        titleGlow.setRadius(16);
        title.setEffect(titleGlow);

        Text subtitle = new Text("PICK THE CHALLENGE LEVEL BEFORE ENTERING THE MAZE");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web("#4F5B62"));

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
                () -> PlayModeSelectionPageJava.showOnStage(stage, stage.getScene(), "EASY")
            ),
            createDifficultyCard(
                "MEDIUM",
                "🟠",
                "Balanced challenge with mixed path patterns.",
                "Good for regular play and skill growth.",
                "Estimated clear time: 5-8 min",
                Color.web("#FFB800"),
                () -> PlayModeSelectionPageJava.showOnStage(stage, stage.getScene(), "MEDIUM")
            ),
            createDifficultyCard(
                "HARD",
                "🔴",
                "Complex maze with misleading branches.",
                "Best for players who want strategic pressure.",
                "Estimated clear time: 8-12 min",
                Color.web("#FF6B6B"),
                () -> PlayModeSelectionPageJava.showOnStage(stage, stage.getScene(), "HARD")
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
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static Pane createFuturisticBackground() {
        return PlayToneBackground.create(VIEW_WIDTH, VIEW_HEIGHT, PlayDifficultyPageJava.class);
    }

    private static VBox createDifficultyCard(String level, String icon, String line1,
                                             String line2, String duration, Color accent,
                                             Runnable onChoose) {
        GameCard card = new GameCard(level, icon, accent, 415, 330);

        Text l1 = card.createBodyText(line1, 372);
        Text l2 = card.createBodyText(line2, 372);
        Text eta = new Text(duration);
        eta.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        eta.setFill(Color.web("#4F5B62"));

        Button choose = new NeonButton("START " + level, accent, 14, 8, 14, 8);
        choose.setPrefWidth(220);
        choose.setMinWidth(220);
        choose.setOnAction(e -> onChoose.run());

        card.addBodyNodes(l1, l2, eta, choose);
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
        return new NeonButton(text, color, 14, 8, 14, 8);
    }
}
