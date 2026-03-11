package com.nhom_01.robot_pathfinding.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
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

import java.util.Locale;

public final class OptionsPageJava {

    private static final double VIEW_WIDTH = 1400;
    private static final double VIEW_HEIGHT = 800;
    private static final double CARD_WIDTH = 415;
    private static final double CARD_HEIGHT = 330;

    private OptionsPageJava() {
    }

    public static void showOptionsOnStage(Stage stage, Scene menuScene) {
        stage.setScene(buildScene(stage, menuScene));
    }

    private static Scene buildScene(Stage stage, Scene menuScene) {
        StackPane root = new StackPane();
        root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
        root.getChildren().add(createFuturisticBackground(VIEW_WIDTH, VIEW_HEIGHT));

        VBox page = new VBox(20);
        page.setAlignment(Pos.TOP_CENTER);
        page.setPadding(new Insets(24, 56, 24, 56));

        VBox titleBox = new VBox(6);
        titleBox.setAlignment(Pos.CENTER);

        Text title = new Text("OPTIONS");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 62));
        title.setFill(Color.web("#00FFFF"));

        Text subtitle = new Text("TUNE AUDIO, GAMEPLAY, AND DISPLAY BEFORE STARTING");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web("#B5C7D6"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.web("#00FFFF"));
        titleGlow.setRadius(26);
        title.setEffect(titleGlow);

        titleBox.getChildren().addAll(title, subtitle);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        HBox topCards = new HBox(22);
        topCards.setAlignment(Pos.TOP_CENTER);

        VBox audioCard = createCard("AUDIO");
        Slider masterVolume = createNeonSlider(80);
        Slider musicVolume = createNeonSlider(70);
        Slider sfxVolume = createNeonSlider(85);
        CheckBox spatialAudio = createToggle("Enable spatial audio");
        CheckBox menuSound = createToggle("Menu sound effects");
        audioCard.getChildren().addAll(
            createSectionLabel("Volume Mixer"),
            createSliderRow("Master", masterVolume),
            createSliderRow("Music", musicVolume),
            createSliderRow("SFX", sfxVolume),
            spatialAudio,
            menuSound
        );

        VBox gameplayCard = createCard("GAMEPLAY");
        CheckBox pathHint = createToggle("Show path hint");
        CheckBox aiSuggestion = createToggle("Enable AI suggestion");
        CheckBox vibration = createToggle("Vibration feedback");
        CheckBox autoPause = createToggle("Auto pause when unfocused");
        CheckBox quitConfirm = createToggle("Confirm before quitting");
        gameplayCard.getChildren().addAll(
            createSectionLabel("Assist Features"),
            pathHint,
            aiSuggestion,
            vibration,
            createSectionLabel("Control"),
            autoPause,
            quitConfirm
        );

        VBox accessibilityCard = createCard("DISPLAY");
        Slider uiScale = createNeonSlider(100);
        uiScale.setMin(80);
        uiScale.setMax(130);
        uiScale.setMajorTickUnit(10);
        CheckBox highContrast = createToggle("High contrast labels");
        CheckBox reducedMotion = createToggle("Reduced motion");
        accessibilityCard.getChildren().addAll(
            createSectionLabel("Interface"),
            createSliderRow("UI scale", uiScale),
            highContrast,
            reducedMotion
        );

        topCards.getChildren().addAll(audioCard, gameplayCard, accessibilityCard);
        content.getChildren().addAll(topCards, createSupportRow());

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(1288);

        Button reset = createActionButton("RESET", Color.web("#CCCCCC"));
        Button save = createActionButton("SAVE", Color.web("#00FF9C"));
        Button back = createActionButton("BACK TO MENU", Color.web("#FF6B6B"));

        reset.setOnAction(e -> {
            masterVolume.setValue(80);
            musicVolume.setValue(70);
            sfxVolume.setValue(85);
            uiScale.setValue(100);
            spatialAudio.setSelected(false);
            menuSound.setSelected(false);
            pathHint.setSelected(false);
            aiSuggestion.setSelected(false);
            vibration.setSelected(false);
            autoPause.setSelected(false);
            quitConfirm.setSelected(false);
            highContrast.setSelected(false);
            reducedMotion.setSelected(false);
        });
        back.setOnAction(e -> stage.setScene(menuScene));
        save.setOnAction(e -> stage.setScene(menuScene));

        actions.getChildren().addAll(reset, save, back);

        page.getChildren().addAll(titleBox, content, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(0,0,0,0.18);");
        overlay.setMouseTransparent(true);

        root.getChildren().addAll(page, overlay);
        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static Pane createFuturisticBackground(double width, double height) {
        Pane bgPane = new Pane();
        bgPane.setPrefSize(width, height);

        Canvas canvas = new Canvas(width, height);
        GraphicsContext gc = canvas.getGraphicsContext2D();

        for (int y = 0; y < (int) height; y++) {
            double ratio = y / height;
            int r = (int) (13 + (27 - 13) * ratio);
            int g = (int) (17 + (51 - 17) * ratio);
            int b = (int) (23 + (48 - 23) * ratio);
            gc.setStroke(Color.rgb(r, g, b));
            gc.strokeLine(0, y, width, y);
        }

        gc.setStroke(Color.color(0, 0.55, 0.85, 0.14));
        gc.setLineWidth(1);
        int gridSize = 44;
        for (int x = 0; x < width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }
        for (int y = 0; y < height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        gc.setFill(Color.color(0, 1, 1, 0.22));
        for (int x = 0; x < width; x += gridSize) {
            for (int y = 0; y < height; y += gridSize) {
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        bgPane.getChildren().add(canvas);
        return bgPane;
    }

    private static VBox createCard(String title) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(18));
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
            "-fx-background-color: rgba(8, 17, 30, 0.78);" +
            "-fx-border-color: rgba(0, 255, 255, 0.35);" +
            "-fx-border-width: 1.6;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(0, 1, 1, 0.3));
        glow.setRadius(18);
        card.setEffect(glow);

        Label heading = new Label(title);
        heading.setTextFill(Color.web("#00FFFF"));
        heading.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        card.getChildren().add(heading);

        return card;
    }

    private static HBox createSupportRow() {
        HBox row = new HBox(22);
        row.setAlignment(Pos.TOP_CENTER);

        VBox overviewPanel = createInfoPanel(
            "AUDIO PRESET TIP",
            "Use Master around 75-85% for consistent volume balance. " +
                "Lower SFX when you need focus during harder mazes.",
            Color.web("#8FE8F4")
        );

        VBox comfortPanel = createInfoPanel(
            "VISUAL COMFORT",
            "Enable Reduced motion if animations feel distracting. " +
                "Use High contrast labels for better readability on bright screens.",
            Color.web("#FFB800")
        );

        row.getChildren().addAll(overviewPanel, comfortPanel);
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

    private static Label createSectionLabel(String text) {
        Label section = new Label(text);
        section.setTextFill(Color.web("#9FC8DE"));
        section.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return section;
    }

    private static VBox createSliderRow(String label, Slider slider) {
        VBox row = new VBox(7);
        row.setAlignment(Pos.CENTER_LEFT);

        Label rowLabel = new Label(label + "  " + formatPercent(slider.getValue()));
        rowLabel.setTextFill(Color.web("#D4E4EF"));
        rowLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        slider.valueProperty().addListener((obs, oldVal, newVal) ->
            rowLabel.setText(label + "  " + formatPercent(newVal.doubleValue()))
        );

        row.getChildren().addAll(rowLabel, slider);
        return row;
    }

    private static String formatPercent(double value) {
        return String.format(Locale.US, "%.0f%%", value);
    }

    private static Slider createNeonSlider(double initialValue) {
        Slider slider = new Slider(0, 100, initialValue);
        slider.setShowTickLabels(false);
        slider.setShowTickMarks(false);
        slider.setMaxWidth(Double.MAX_VALUE);
        slider.setStyle(
            "-fx-control-inner-background: #0D1520;" +
            "-fx-accent: #00FFFF;"
        );
        return slider;
    }

    private static CheckBox createToggle(String text) {
        CheckBox box = new CheckBox(text);
        box.setTextFill(Color.web("#D4E4EF"));
        box.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        box.setCursor(Cursor.HAND);
        box.setStyle(
            "-fx-mark-color: #00FFFF;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        return box;
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
            "-fx-padding: 8 16 8 16;" +
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
