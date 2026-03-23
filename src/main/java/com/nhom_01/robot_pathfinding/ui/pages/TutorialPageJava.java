package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
import com.nhom_01.robot_pathfinding.ui.theme.UITheme;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.net.URL;

public final class TutorialPageJava {

    private static final double VIEW_WIDTH = 1400;
    private static final double VIEW_HEIGHT = 800;

    private TutorialPageJava() {
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

        VBox page = new VBox(14);
        page.setPadding(new Insets(22, 36, 22, 36));

        HBox header = new HBox(18);
        header.setAlignment(Pos.CENTER_LEFT);

        Text title = new Text("TUTORIAL CENTER");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 46));
        title.setFill(Color.web("#1F2D3A"));
        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(0.18, 0.50, 0.93, 0.24));
        glow.setRadius(16);
        title.setEffect(glow);

        VBox titleBox = new VBox(4);
        Text subtitle = new Text("LEARN MOVEMENT, BOMBS, ITEMS, SKILLS, AND ALGORITHM STRATEGY");
        subtitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        subtitle.setFill(Color.web("#4F5B62"));
        titleBox.getChildren().addAll(title, subtitle);

        Button back = new NeonButton("BACK", UITheme.SECONDARY, 14, 8, 14, 8);
        back.setPrefWidth(110);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        header.getChildren().addAll(titleBox, spacer, back);

        HBox content = new HBox(14);
        content.setAlignment(Pos.TOP_LEFT);

        List<TutorialSection> sections = tutorialSections();
        ListView<TutorialSection> sectionList = new ListView<>();
        sectionList.getItems().addAll(sections);
        sectionList.setPrefWidth(320);
        sectionList.setStyle(
            "-fx-background-color: rgba(255,255,255,0.95);" +
            "-fx-control-inner-background: rgba(255,255,255,0.95);" +
            "-fx-border-color: rgba(0,0,0,0.10);" +
            "-fx-border-width: 1.4;" +
            "-fx-border-radius: 10;"
        );
        sectionList.setCellFactory(list -> new ListCell<>() {
            @Override
            protected void updateItem(TutorialSection item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                    setStyle("");
                    return;
                }
                setText(item.title());
                setFont(Font.font("Orbitron", FontWeight.BOLD, 16));
                setTextFill(Color.web("#2D3E50"));
                String bg = isSelected() ? "rgba(47,128,237,0.18)" : "transparent";
                setStyle("-fx-background-color: " + bg + "; -fx-padding: 10;");
            }
        });

        VBox rightPane = new VBox(10);
        rightPane.setPadding(new Insets(12));
        rightPane.setStyle(
            "-fx-background-color: rgba(255,255,255,0.94);" +
            "-fx-border-color: rgba(0,0,0,0.10);" +
            "-fx-border-width: 1.5;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );
        DropShadow panelGlow = new DropShadow();
        panelGlow.setColor(Color.color(0.12, 0.16, 0.20, 0.14));
        panelGlow.setRadius(10);
        rightPane.setEffect(panelGlow);

        Text sectionTitle = new Text();
        sectionTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 28));
        sectionTitle.setFill(Color.web("#1F2D3A"));

        StackPane videoPane = new StackPane();
        videoPane.setPrefHeight(390);
        videoPane.setStyle(
            "-fx-background-color: rgba(247,241,227,0.95);" +
            "-fx-border-color: rgba(0,0,0,0.12);" +
            "-fx-border-width: 1.4;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );

        MediaView mediaView = new MediaView();
        mediaView.setFitWidth(970);
        mediaView.setFitHeight(370);
        mediaView.setPreserveRatio(true);

        Text fallbackText = new Text("VIDEO NOT FOUND\nSet a valid file path in tutorial section config.");
        fallbackText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        fallbackText.setFill(Color.web("#607D8B"));

        videoPane.getChildren().add(fallbackText);

        HBox controls = new HBox(10);
        controls.setAlignment(Pos.CENTER_LEFT);
        Button play = new NeonButton("PLAY", Color.web("#2E7D32"), 12, 6, 10, 5);
        Button pause = new NeonButton("PAUSE", Color.web("#FFB800"), 12, 6, 10, 5);
        Button stop = new NeonButton("STOP", Color.web("#FF6B6B"), 12, 6, 10, 5);

        Text sourcePath = new Text();
        sourcePath.setFill(Color.web("#607D8B"));
        sourcePath.setFont(Font.font("Arial", FontWeight.NORMAL, 12));

        controls.getChildren().addAll(play, pause, stop, sourcePath);

        Text descriptionTitle = new Text("DETAILED GUIDE");
        descriptionTitle.setFont(Font.font("Orbitron", FontWeight.BOLD, 18));
        descriptionTitle.setFill(Color.web("#2D3E50"));

        TextArea descriptionArea = new TextArea();
        descriptionArea.setWrapText(true);
        descriptionArea.setEditable(false);
        descriptionArea.setPrefRowCount(8);
        descriptionArea.setStyle(
            "-fx-control-inner-background: rgba(255,255,255,0.95);" +
            "-fx-text-fill: #455A64;" +
            "-fx-font-family: 'Arial';" +
            "-fx-font-size: 14px;" +
            "-fx-border-color: rgba(0,0,0,0.12);" +
            "-fx-border-width: 1.2;"
        );

        rightPane.getChildren().addAll(sectionTitle, videoPane, controls, descriptionTitle, descriptionArea);
        HBox.setHgrow(rightPane, Priority.ALWAYS);
        VBox.setVgrow(descriptionArea, Priority.ALWAYS);

        content.getChildren().addAll(sectionList, rightPane);
        VBox.setVgrow(content, Priority.ALWAYS);

        final MediaPlayer[] currentPlayer = {null};

        Runnable stopAndDispose = () -> {
            if (currentPlayer[0] != null) {
                currentPlayer[0].stop();
                currentPlayer[0].dispose();
                currentPlayer[0] = null;
            }
        };

        back.setOnAction(e -> {
            stopAndDispose.run();
            stage.setScene(menuScene);
        });

        Runnable loadCurrent = () -> {
            TutorialSection selected = sectionList.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            sectionTitle.setText(selected.title());
            descriptionArea.setText(selected.description());
            sourcePath.setText("Source: " + selected.videoPath());

            stopAndDispose.run();
            MediaPlayer player = createPlayerFromPath(selected.videoPath());
            if (player == null) {
                videoPane.getChildren().setAll(fallbackText);
                return;
            }

            currentPlayer[0] = player;
            mediaView.setMediaPlayer(player);
            videoPane.getChildren().setAll(mediaView);
        };

        sectionList.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> loadCurrent.run());
        sectionList.getSelectionModel().select(0);

        play.setOnAction(e -> {
            if (currentPlayer[0] != null) {
                currentPlayer[0].play();
            }
        });
        pause.setOnAction(e -> {
            if (currentPlayer[0] != null) {
                currentPlayer[0].pause();
            }
        });
        stop.setOnAction(e -> {
            if (currentPlayer[0] != null) {
                currentPlayer[0].stop();
            }
        });

        page.getChildren().addAll(header, content);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static MediaPlayer createPlayerFromPath(String videoPath) {
        try {
            String mediaUri = resolveVideoUri(videoPath);
            if (mediaUri == null) {
                return null;
            }
            Media media = new Media(mediaUri);
            MediaPlayer player = new MediaPlayer(media);
            player.setAutoPlay(false);
            return player;
        } catch (Exception ex) {
            return null;
        }
    }

    private static String resolveVideoUri(String videoPath) {
        if (videoPath == null || videoPath.isBlank()) {
            return null;
        }

        // 1) Classpath resource (recommended): /image/video/xxx.mp4
        if (videoPath.startsWith("/")) {
            URL resourceUrl = TutorialPageJava.class.getResource(videoPath);
            if (resourceUrl != null) {
                return resourceUrl.toExternalForm();
            }
        }

        // 2) External/relative file path fallback
        Path path = Path.of(videoPath);
        if (Files.exists(path)) {
            return path.toUri().toString();
        }

        return null;
    }

    private static List<TutorialSection> tutorialSections() {
        return List.of(
            new TutorialSection(
                "MOVEMENT",
                "/image/video/movement.mp4",
                "Movement controls are grid-based and every arrow-key input represents one deliberate tactical step.\n\n"
                    + "Best practice:\n"
                    + "1) Observe the nearest safe corridor before moving.\n"
                    + "2) Avoid bouncing against walls because every attempted move can waste score momentum.\n"
                    + "3) Stay centered in open lanes to keep reaction options when bombs or items appear.\n\n"
                    + "Advanced tip: plan 3-5 cells ahead and commit to low-risk routes instead of short but dangerous shortcuts."
            ),
            new TutorialSection(
                "BOMB",
                "/image/video/bomb.mp4",
                "Bomb cells are high-risk hazards that reduce lives and heavily penalize score.\n\n"
                    + "How to survive:\n"
                    + "1) Scan neighboring cells before each step in narrow passages.\n"
                    + "2) If bomb density appears high, detour even if the route is longer.\n"
                    + "3) Preserve lives early; late-game recovery options are limited.\n\n"
                    + "Strategic principle: one avoided bomb is usually worth more than several quick steps toward the goal."
            ),
            new TutorialSection(
                "ITEM",
                "/image/video/item.mp4",
                "Items are opportunity nodes that improve survivability or scoring potential through power-ups.\n\n"
                    + "Collection workflow:\n"
                    + "1) Reach item cell.\n"
                    + "2) Pick one power-up from the modal.\n"
                    + "3) Re-evaluate route according to your new capability.\n\n"
                    + "Optimization tip: take items when they are on a low-risk route; never force item collection through bomb-heavy branches."
            ),
            new TutorialSection(
                "SKILL",
                "/image/video/t.mp4",
                "Skills amplify your decision quality and should be used when they change the outcome of a route, not just for convenience.\n\n"
                    + "Usage model:\n"
                    + "1) Trigger skill before entering uncertain regions.\n"
                    + "2) Chain skill benefit with the next 3-4 moves immediately.\n"
                    + "3) Avoid wasting a skill while standing in already-safe corridors.\n\n"
                    + "High-level tactic: combine skill timing with score preservation for stronger ranking results."
            ),
            new TutorialSection(
                "ALGORITHM",
                "/image/video/algorithm.mp4",
                "In BOT mode, algorithm choice directly influences path style, exploration breadth, and total efficiency.\n\n"
                    + "Comparison:\n"
                    + "- BFS: reliable shortest path on unweighted grids.\n"
                    + "- DFS: deep exploration, can be faster to discover but may return longer paths.\n"
                    + "- A*: heuristic-guided search, typically best balance of speed and quality.\n\n"
                    + "Leaderboard tip: on complex mazes, A* often yields stronger score-to-time performance for total ranking."
            )
        );
    }

    private static Pane createFuturisticBackground() {
        return PlayToneBackground.create(VIEW_WIDTH, VIEW_HEIGHT, TutorialPageJava.class);
    }

    private record TutorialSection(String title, String videoPath, String description) {
    }
}
