package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.GameSettings;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
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

    private static final double VIEW_WIDTH  = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    private static final double VIEW_HEIGHT = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();
    private static final double CARD_WIDTH  = 415;
    private static final double CARD_HEIGHT = 330;

    private OptionsPageJava() {
    }

    public static void showOptionsOnStage(Stage stage, Scene menuScene) {
        boolean keepMaximized = stage.isMaximized();
        Scene scene = buildScene(stage, menuScene);
        MenuAudioManager.wireScene(scene);
        MenuAudioManager.startTheme();
        stage.setScene(scene);
        if (keepMaximized) {
            stage.setMaximized(true);
        }
    }

    private static Scene buildScene(Stage stage, Scene menuScene) {
        GameSettings settings = GameSettings.getInstance();
        
        StackPane root = new StackPane();
        root.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
        root.setMaxWidth(Double.MAX_VALUE);
        root.setMaxHeight(Double.MAX_VALUE);

        VBox page = new VBox(20);
        page.setAlignment(Pos.TOP_CENTER);
        page.setPadding(new Insets(24, 56, 24, 56));

        VBox titleBox = new VBox(6);
        titleBox.setAlignment(Pos.CENTER);

        Text title = new Text("OPTIONS");
        title.setFont(Font.font("Orbitron", FontWeight.BOLD, 62));
        title.setFill(Color.web("#1F2D3A"));

        Text subtitle = new Text("ESSENTIAL SETTINGS");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.web("#4F5B62"));

        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.24));
        titleGlow.setRadius(16);
        title.setEffect(titleGlow);

        titleBox.getChildren().addAll(title, subtitle);

        VBox content = new VBox(20);
        content.setAlignment(Pos.TOP_CENTER);

        HBox topCards = new HBox(22);
        topCards.setAlignment(Pos.TOP_CENTER);

        // ============ AUDIO CARD ============
        VBox audioCard = createCard("AUDIO");
        Slider masterVolume = createNeonSlider(settings.getMasterVolume());
        Slider musicVolume = createNeonSlider(settings.getMusicVolume());
        Slider sfxVolume = createNeonSlider(settings.getSFXVolume());
        CheckBox spatialAudio = createToggle("Enable spatial audio");
        CheckBox menuSound = createToggle("Menu sound effects");
        
        // Load audio settings
        spatialAudio.setSelected(settings.isSpatialAudioEnabled());
        menuSound.setSelected(settings.isMenuSoundEffectsEnabled());
        
        // Add listeners to update settings
        masterVolume.valueProperty().addListener((obs, oldVal, newVal) ->
            settings.setMasterVolume(newVal.doubleValue())
        );
        musicVolume.valueProperty().addListener((obs, oldVal, newVal) ->
            settings.setMusicVolume(newVal.doubleValue())
        );
        sfxVolume.valueProperty().addListener((obs, oldVal, newVal) ->
            settings.setSFXVolume(newVal.doubleValue())
        );
        spatialAudio.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setSpatialAudioEnabled(newVal)
        );
        menuSound.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setMenuSoundEffectsEnabled(newVal)
        );
        
        audioCard.getChildren().addAll(
            createSectionLabel("Volume"),
            createSliderRow("Master", masterVolume),
            createSliderRow("Music", musicVolume),
            createSliderRow("SFX", sfxVolume),
            spatialAudio,
            menuSound
        );

        // ============ GAMEPLAY CARD ============
        VBox gameplayCard = createCard("GAMEPLAY");
        CheckBox pathHint = createToggle("Show path hint");
        CheckBox aiSuggestion = createToggle("Enable AI suggestion");
        CheckBox vibration = createToggle("Vibration feedback");
        CheckBox autoPause = createToggle("Auto pause when unfocused");
        CheckBox quitConfirm = createToggle("Confirm before quitting");
        
        // Load gameplay settings
        pathHint.setSelected(settings.isShowPathHint());
        aiSuggestion.setSelected(settings.isEnableAISuggestion());
        vibration.setSelected(settings.isVibrationFeedbackEnabled());
        autoPause.setSelected(settings.isAutoPauseWhenUnfocused());
        quitConfirm.setSelected(settings.isConfirmBeforeQuitting());
        
        // Add listeners to update settings
        pathHint.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setShowPathHint(newVal)
        );
        aiSuggestion.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setEnableAISuggestion(newVal)
        );
        vibration.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setVibrationFeedbackEnabled(newVal)
        );
        autoPause.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setAutoPauseWhenUnfocused(newVal)
        );
        quitConfirm.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setConfirmBeforeQuitting(newVal)
        );
        
        gameplayCard.getChildren().addAll(
            createSectionLabel("Assist"),
            pathHint,
            aiSuggestion,
            vibration,
            createSectionLabel("Control"),
            autoPause,
            quitConfirm
        );

        // ============ DISPLAY CARD ============
        VBox accessibilityCard = createCard("DISPLAY");
        Slider uiScale = createNeonSlider(settings.getUIScale());
        uiScale.setMin(80);
        uiScale.setMax(130);
        uiScale.setMajorTickUnit(10);
        CheckBox highContrast = createToggle("High contrast labels");
        CheckBox reducedMotion = createToggle("Reduced motion");
        
        // Load display settings
        highContrast.setSelected(settings.isHighContrastLabels());
        reducedMotion.setSelected(settings.isReducedMotion());
        
        // Add listeners to update settings
        uiScale.valueProperty().addListener((obs, oldVal, newVal) ->
            settings.setUIScale(newVal.doubleValue())
        );
        highContrast.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setHighContrastLabels(newVal)
        );
        reducedMotion.selectedProperty().addListener((obs, oldVal, newVal) ->
            settings.setReducedMotion(newVal)
        );
        
        accessibilityCard.getChildren().addAll(
            createSectionLabel("Display"),
            createSliderRow("UI scale", uiScale),
            highContrast,
            reducedMotion
        );

        topCards.getChildren().addAll(audioCard, gameplayCard, accessibilityCard);
        content.getChildren().addAll(topCards);

        HBox actions = new HBox(16);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.setMaxWidth(1288);

        Button reset = createActionButton("RESET", Color.web("#607D8B"));
        Button save = createActionButton("SAVE", Color.web("#00FF9C"));
        Button back = createActionButton("BACK TO MENU", Color.web("#FF6B6B"));

        reset.setOnAction(e -> {
            settings.resetToDefaults();
            masterVolume.setValue(settings.getMasterVolume());
            musicVolume.setValue(settings.getMusicVolume());
            sfxVolume.setValue(settings.getSFXVolume());
            uiScale.setValue(settings.getUIScale());
            spatialAudio.setSelected(settings.isSpatialAudioEnabled());
            menuSound.setSelected(settings.isMenuSoundEffectsEnabled());
            pathHint.setSelected(settings.isShowPathHint());
            aiSuggestion.setSelected(settings.isEnableAISuggestion());
            vibration.setSelected(settings.isVibrationFeedbackEnabled());
            autoPause.setSelected(settings.isAutoPauseWhenUnfocused());
            quitConfirm.setSelected(settings.isConfirmBeforeQuitting());
            highContrast.setSelected(settings.isHighContrastLabels());
            reducedMotion.setSelected(settings.isReducedMotion());
        });
        
        save.setOnAction(e -> {
            settings.saveSettings();
            MenuAudioManager.updateVolumes();
            stage.setScene(menuScene);
        });
        
        back.setOnAction(e -> stage.setScene(menuScene));

        actions.getChildren().addAll(reset, save, back);

        page.getChildren().addAll(titleBox, content, actions);

        Pane overlay = new Pane();
        overlay.setStyle("-fx-background-color: rgba(255,255,255,0.06);");
        overlay.setMouseTransparent(true);

        StackPane contentLayer = new StackPane();
        contentLayer.setPrefSize(VIEW_WIDTH, VIEW_HEIGHT);
        contentLayer.setMaxWidth(Double.MAX_VALUE);
        contentLayer.setMaxHeight(Double.MAX_VALUE);
        contentLayer.getChildren().addAll(createFuturisticBackground(), page, overlay);
        
        root.getChildren().add(contentLayer);
        
        AppFonts.applyTo(root);
        Scene scene = new Scene(root);
        return scene;
    }

    private static StackPane createFuturisticBackground() {
        StackPane bgPane = new StackPane();
        bgPane.setMaxWidth(Double.MAX_VALUE);
        bgPane.setMaxHeight(Double.MAX_VALUE);
        Canvas canvas = new Canvas(VIEW_WIDTH, VIEW_HEIGHT);
        bgPane.getChildren().add(canvas);

        canvas.widthProperty().bind(bgPane.widthProperty());
        canvas.heightProperty().bind(bgPane.heightProperty());
        canvas.widthProperty().addListener((obs, oldV, newV) -> drawBackground(canvas));
        canvas.heightProperty().addListener((obs, oldV, newV) -> drawBackground(canvas));
        drawBackground(canvas);

        return bgPane;
    }

    private static void drawBackground(Canvas canvas) {
        double width = Math.max(1, canvas.getWidth());
        double height = Math.max(1, canvas.getHeight());
        GraphicsContext gc = canvas.getGraphicsContext2D();
        PlayToneBackground.draw(gc, width, height, OptionsPageJava.class);
    }

    private static VBox createCard(String title) {
        VBox card = new VBox(16);
        card.setPadding(new Insets(18));
        card.setPrefSize(CARD_WIDTH, CARD_HEIGHT);
        card.setMinSize(CARD_WIDTH, CARD_HEIGHT);
        card.setAlignment(Pos.TOP_LEFT);
        card.setStyle(
            "-fx-background-color: rgba(255,255,255,0.94);" +
            "-fx-border-color: rgba(0,0,0,0.10);" +
            "-fx-border-width: 1.6;" +
            "-fx-border-radius: 12;" +
            "-fx-background-radius: 12;"
        );

        DropShadow glow = new DropShadow();
        glow.setColor(Color.color(0.12, 0.16, 0.20, 0.14));
        glow.setRadius(10);
        card.setEffect(glow);

        Label heading = new Label(title);
        heading.setTextFill(Color.web("#1F2D3A"));
        heading.setFont(Font.font("Orbitron", FontWeight.BOLD, 24));
        card.getChildren().add(heading);

        return card;
    }

    private static Label createSectionLabel(String text) {
        Label section = new Label(text);
        section.setTextFill(Color.web("#546E7A"));
        section.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        return section;
    }

    private static VBox createSliderRow(String label, Slider slider) {
        VBox row = new VBox(7);
        row.setAlignment(Pos.CENTER_LEFT);

        Label rowLabel = new Label(label + "  " + formatPercent(slider.getValue()));
        rowLabel.setTextFill(Color.web("#455A64"));
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
            "-fx-control-inner-background: #F3E3C7;" +
            "-fx-accent: #2F80ED;"
        );
        return slider;
    }

    private static CheckBox createToggle(String text) {
        CheckBox box = new CheckBox(text);
        box.setTextFill(Color.web("#455A64"));
        box.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        box.setCursor(Cursor.HAND);
        box.setStyle(
            "-fx-mark-color: #2F80ED;" +
            "-fx-focus-color: transparent;" +
            "-fx-faint-focus-color: transparent;"
        );
        return box;
    }

    private static Button createActionButton(String text, Color color) {
        Button btn = new NeonButton(text, color, 14, 8, 14, 8);
        btn.setMinWidth(140);
        return btn;
    }
}
