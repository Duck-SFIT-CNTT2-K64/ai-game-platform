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
import javafx.scene.Scene;
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

    private static final double VIEW_WIDTH  = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    private static final double VIEW_HEIGHT = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

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
            "🦆",
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

        Button back = new NeonButton("BACK TO DIFFICULTY", Color.web("#607D8B"), 14, 8, 14, 8);
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

    private static VBox createModeCard(String title, String icon, String line1, String line2,
                                       String line3, Color accent, Runnable onChoose) {
        GameCard card = new GameCard(title, icon, accent, 540, 360);

        Text l1 = card.createBodyText(line1, 500);
        Text l2 = card.createBodyText(line2, 500);
        Text l3 = card.createBodyText(line3, 500);
        l3.setFill(Color.web("#455A64"));

        Button start = new NeonButton("CHOOSE " + title, accent, 15, 9, 16, 8);
        start.setPrefWidth(210);
        start.setMinWidth(210);
        start.setOnAction(e -> onChoose.run());

        card.addBodyNodes(l1, l2, l3, start);
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
}
