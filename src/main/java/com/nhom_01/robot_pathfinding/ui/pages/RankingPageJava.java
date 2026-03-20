package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.PlayerProfile;
import com.nhom_01.robot_pathfinding.core.RankingEntry;
import com.nhom_01.robot_pathfinding.core.RankingManager;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.UITheme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.List;

public final class RankingPageJava {

    private static final double VIEW_WIDTH = 1400;
    private static final double VIEW_HEIGHT = 800;

    private RankingPageJava() {
    }

    public static void showOnStage(Stage stage, Scene menuScene) {
        stage.setScene(buildScene(stage, menuScene));
    }

    private static Scene buildScene(Stage stage, Scene menuScene) {
        StackPane root = new StackPane();
        root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
        root.getChildren().add(createFuturisticBackground());

        VBox page = new VBox(14);
        page.setPadding(new Insets(22, 36, 22, 36));
        page.setAlignment(Pos.TOP_LEFT);

        HBox header = new HBox(18);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("RANKING BOARD");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 48));
        title.setFill(Color.web("#00FFFF"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.web("#00FFFF"));
        glow.setRadius(28);
        title.setEffect(glow);

        VBox heading = new VBox(4);
        Text subtitle = new Text("TOP SCORES BY DIFFICULTY AND TOTAL GLOBAL LEADERBOARD");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        subtitle.setFill(Color.web("#C9DCEA"));

        Text currentPlayer = new Text("CURRENT PLAYER: " + PlayerProfile.getCurrentPlayerName());
        currentPlayer.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        currentPlayer.setFill(Color.web("#9FD9E9"));
        heading.getChildren().addAll(title, subtitle, currentPlayer);

        Button back = new NeonButton("BACK", UITheme.SECONDARY, 14, 8, 14, 8);
        back.setPrefWidth(110);
        back.setOnAction(e -> stage.setScene(menuScene));
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(heading, spacer, back);

        HBox tabs = new HBox(10);
        tabs.setAlignment(Pos.CENTER_LEFT);
        tabs.setPadding(new Insets(2, 0, 6, 0));

        VBox tablePanel = new VBox(10);
        tablePanel.setPadding(new Insets(16));
        tablePanel.setStyle(
            "-fx-background-color: rgba(8,16,28,0.78);" +
            "-fx-border-color: rgba(0,255,255,0.30);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        DropShadow panelGlow = new DropShadow();
        panelGlow.setColor(Color.color(0, 1, 1, 0.20));
        panelGlow.setRadius(20);
        tablePanel.setEffect(panelGlow);

        String[] categories = {"Easy", "Medium", "Hard", "Total"};
        Button[] tabButtons = new Button[categories.length];

        for (int i = 0; i < categories.length; i++) {
            String category = categories[i];
            Button tab = createTabButton(category);
            tabButtons[i] = tab;
            tabs.getChildren().add(tab);
            final int index = i;
            tab.setOnAction(e -> {
                setActiveTab(tabButtons, index);
                updateRankings(tablePanel, category);
            });
        }

        setActiveTab(tabButtons, 0);
        updateRankings(tablePanel, "Easy");

        page.getChildren().addAll(header, tabs, tablePanel);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.12);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static Button createTabButton(String text) {
        Button tab = new Button(text.toUpperCase());
        tab.setFont(Font.font("Orbitron", FontWeight.BOLD, 14));
        tab.setMinWidth(138);
        tab.setStyle(
            "-fx-background-color: rgba(10,18,30,0.86);" +
            "-fx-text-fill: #95DFFF;" +
            "-fx-border-color: rgba(0,255,255,0.24);" +
            "-fx-border-width: 1.2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-padding: 9 14 9 14;" +
            "-fx-cursor: hand;"
        );
        return tab;
    }

    private static void setActiveTab(Button[] tabs, int activeIndex) {
        for (int i = 0; i < tabs.length; i++) {
            Button tab = tabs[i];
            boolean active = i == activeIndex;
            tab.setStyle(
                "-fx-background-color: " + (active ? "rgba(0,255,255,0.22);" : "rgba(10,18,30,0.86);") +
                "-fx-text-fill: " + (active ? "#E8FBFF;" : "#95DFFF;") +
                "-fx-font-family: 'Orbitron';" +
                "-fx-font-weight: bold;" +
                "-fx-font-size: 14px;" +
                "-fx-border-color: " + (active ? "rgba(0,255,255,0.62);" : "rgba(0,255,255,0.24);") +
                "-fx-border-width: 1.2;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-padding: 9 14 9 14;" +
                "-fx-cursor: hand;"
            );
        }
    }

    private static void updateRankings(VBox panel, String difficulty) {
        panel.getChildren().clear();

        Text sectionTitle = new Text(("Total".equalsIgnoreCase(difficulty) ? "GLOBAL" : difficulty.toUpperCase()) + " RANKING");
        sectionTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 26));
        sectionTitle.setFill(Color.web("#7DE9FF"));

        List<RankingEntry> entries = fetchRankings(difficulty);
        if (entries.isEmpty()) {
            VBox empty = new VBox(8);
            empty.setAlignment(Pos.CENTER);
            empty.setPrefHeight(550);
            Label icon = new Label("NO RANKING DATA");
            icon.setTextFill(Color.web("#7BD7EF"));
            icon.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));
            Text hint = new Text("Play a game to generate the first score entry.");
            hint.setFill(Color.web("#A6C5D7"));
            hint.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
            empty.getChildren().addAll(icon, hint);
            panel.getChildren().addAll(sectionTitle, empty);
            return;
        }

        TableView<RankingEntry> table = createTable();
        table.getItems().setAll(entries);
        VBox.setVgrow(table, javafx.scene.layout.Priority.ALWAYS);

        panel.getChildren().addAll(sectionTitle, table);
    }

    private static List<RankingEntry> fetchRankings(String difficulty) {
        RankingManager manager = RankingManager.getInstance();
        if ("Total".equalsIgnoreCase(difficulty)) {
            return manager.getAllRankings();
        }
        return manager.getRankingsByDifficulty(difficulty);
    }

    private static TableView<RankingEntry> createTable() {
        TableView<RankingEntry> table = new TableView<>();
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        table.setStyle(
            "-fx-background-color: rgba(5,12,22,0.86);" +
            "-fx-control-inner-background: rgba(5,12,22,0.86);" +
            "-fx-table-cell-border-color: rgba(0,255,255,0.10);" +
            "-fx-font-size: 13px;"
        );

        TableColumn<RankingEntry, Integer> rankCol = new TableColumn<>("#");
        rankCol.setCellValueFactory(param -> new SimpleObjectProperty<>(table.getItems().indexOf(param.getValue()) + 1));

        TableColumn<RankingEntry, String> nameCol = new TableColumn<>("Player");
        nameCol.setCellValueFactory(new PropertyValueFactory<>("playerName"));

        TableColumn<RankingEntry, String> diffCol = new TableColumn<>("Difficulty");
        diffCol.setCellValueFactory(new PropertyValueFactory<>("difficulty"));

        TableColumn<RankingEntry, String> algoCol = new TableColumn<>("Algorithm");
        algoCol.setCellValueFactory(new PropertyValueFactory<>("algorithm"));

        TableColumn<RankingEntry, Integer> stepsCol = new TableColumn<>("Steps");
        stepsCol.setCellValueFactory(new PropertyValueFactory<>("steps"));

        TableColumn<RankingEntry, String> timeCol = new TableColumn<>("Time");
        timeCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getTimeFormatted()));

        TableColumn<RankingEntry, Integer> scoreCol = new TableColumn<>("Score");
        scoreCol.setCellValueFactory(new PropertyValueFactory<>("score"));

        TableColumn<RankingEntry, String> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getTimestampFormatted()));

        List<TableColumn<RankingEntry, ?>> cols = new ArrayList<>();
        cols.add(rankCol);
        cols.add(nameCol);
        cols.add(diffCol);
        cols.add(algoCol);
        cols.add(stepsCol);
        cols.add(timeCol);
        cols.add(scoreCol);
        cols.add(dateCol);

        for (TableColumn<RankingEntry, ?> col : cols) {
            col.setStyle("-fx-alignment: CENTER; -fx-font-family: 'Arial'; -fx-text-fill: #E6FAFF;");
        }

        table.getColumns().addAll(rankCol, nameCol, diffCol, algoCol, stepsCol, timeCol, scoreCol, dateCol);
        return table;
    }

    private static Pane createFuturisticBackground() {
        Pane bgPane = new Pane();
        bgPane.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);

        Canvas bgCanvas = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);
        GraphicsContext gc = bgCanvas.getGraphicsContext2D();

        for (int y = 0; y < VIEW_HEIGHT; y++) {
            double ratio = y / VIEW_HEIGHT;
            int r = (int) (13 + (27 - 13) * ratio);
            int g = (int) (17 + (51 - 17) * ratio);
            int b = (int) (23 + (48 - 23) * ratio);
            gc.setStroke(Color.rgb(r, g, b));
            gc.strokeLine(0, y, VIEW_WIDTH, y);
        }

        gc.setStroke(Color.color(0, 0.4, 0.8, 0.15));
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

        bgPane.getChildren().add(bgCanvas);
        return bgPane;
    }
}
