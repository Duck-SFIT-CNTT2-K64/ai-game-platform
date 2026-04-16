package com.nhom_01.robot_pathfinding.ui.pages;

import com.nhom_01.robot_pathfinding.core.CellType;
import com.nhom_01.robot_pathfinding.core.Maze;
import com.nhom_01.robot_pathfinding.core.State;
import com.nhom_01.robot_pathfinding.ui.MazeRenderer;
import com.nhom_01.robot_pathfinding.ui.audio.MenuAudioManager;
import com.nhom_01.robot_pathfinding.ui.components.NeonButton;
import com.nhom_01.robot_pathfinding.ui.theme.AppFonts;
import com.nhom_01.robot_pathfinding.ui.theme.PlayToneBackground;
import javafx.animation.AnimationTimer;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;

public final class TutorialPageJava {

    private static final double VIEW_WIDTH  = javafx.stage.Screen.getPrimary().getVisualBounds().getWidth();
    private static final double VIEW_HEIGHT = javafx.stage.Screen.getPrimary().getVisualBounds().getHeight();

    private static AnimationTimer previewTimer;
    private static Timeline typewriterTimeline;

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
        root.getChildren().add(PlayToneBackground.create(VIEW_WIDTH, VIEW_HEIGHT, TutorialPageJava.class));

        VBox page = new VBox(32);
        page.setPadding(new Insets(80, 60, 80, 60));
        page.setAlignment(Pos.TOP_CENTER);

        // --- Header ---
        HBox header = new HBox(20);
        header.setAlignment(Pos.CENTER_LEFT);

        VBox titleBox = new VBox(2);
        Text title = new Text("HƯỚNG DẪN GIẢI CỨU VỊT");
        title.setFont(AppFonts.vt323(28));
        title.setFill(Color.WHITE);
        
        DropShadow titleGlow = new DropShadow();
        titleGlow.setColor(Color.color(0.18, 0.50, 0.93, 0.3));
        titleGlow.setRadius(20);
        title.setEffect(titleGlow);

        Text subtitle = new Text("HƯỚNG DẪN CƠ BẢN VÀ NÂNG CAO");
        subtitle.setFont(AppFonts.vt323(14));
        subtitle.setFill(Color.WHITE);
        titleBox.getChildren().addAll(title, subtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button backBtn = new NeonButton("QUAY LẠI", Color.web("#455A64"), 14, 8, 16, 8);
        backBtn.setPrefWidth(140);
        backBtn.setOnAction(e -> {
            stopAnimations();
            stage.setScene(menuScene);
        });

        header.getChildren().addAll(titleBox, spacer, backBtn);

        // --- Main Content Area (Cards + Preview) ---
        HBox contentLayout = new HBox(30);
        contentLayout.setAlignment(Pos.CENTER);
        VBox.setVgrow(contentLayout, Priority.ALWAYS);

        // Left: Instruction Cards
        VBox cardsList = new VBox(15);
        cardsList.setPrefWidth(380);
        cardsList.setAlignment(Pos.CENTER);

        // Right: Dynamic Preview & Description
        VBox rightPane = new VBox(20);
        rightPane.setAlignment(Pos.TOP_CENTER);
        HBox.setHgrow(rightPane, Priority.ALWAYS);

        StackPane previewFrame = new StackPane();
        previewFrame.setPrefSize(800, 400);
        previewFrame.setMaxSize(800, 400);
        previewFrame.setStyle("-fx-background-color: #0F172A; -fx-background-radius: 20; -fx-border-color: #334155; -fx-border-width: 2; -fx-border-radius: 20;");
        
        Canvas previewCanvas = new Canvas(760, 360);
        previewFrame.getChildren().add(previewCanvas);
        
        DropShadow frameShadow = new DropShadow();
        frameShadow.setColor(Color.color(0, 0, 0, 0.4));
        frameShadow.setRadius(30);
        previewFrame.setEffect(frameShadow);

        // Description Box
        VBox descBox = new VBox(10);
        descBox.setPadding(new Insets(15));
        descBox.setPrefHeight(180);
        descBox.setMinHeight(180);
        descBox.setMaxHeight(180);
        descBox.setPrefWidth(800);
        descBox.setMaxWidth(800);
        descBox.setStyle("-fx-background-color: rgba(15, 23, 42, 0.9); -fx-background-radius: 15; -fx-border-color: rgba(255,255,255,0.1); -fx-border-radius: 15;");
        
        Text descTitle = new Text("CHI TIẾT HƯỚNG DẪN");
        descTitle.setFont(AppFonts.vt323(18));
        descTitle.setFill(Color.WHITE);
        
        Text descText = new Text("");
        descText.setFont(AppFonts.vt323(26));
        descText.setFill(Color.web("#F1F5F9"));
        descText.setWrappingWidth(770);
        
        descBox.getChildren().addAll(descTitle, descText);

        // --- POWER-UP LAB SELECTOR (Hidden by default) ---
        VBox labsPane = new VBox(15);
        labsPane.setAlignment(Pos.CENTER);
        labsPane.setPadding(new Insets(10, 0, 10, 0));
        labsPane.setManaged(false);
        labsPane.setVisible(false);

        ScrollPane labsScroll = new ScrollPane();
        labsScroll.setFitToHeight(true);
        labsScroll.setPrefHeight(100);
        labsScroll.setStyle("-fx-background: transparent; -fx-background-color: transparent; -fx-border-color: transparent;");
        labsScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        labsScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);

        HBox labsBox = new HBox(15);
        labsBox.setAlignment(Pos.CENTER);
        labsBox.setPadding(new Insets(5, 20, 5, 20));
        labsScroll.setContent(labsBox);
        labsPane.getChildren().add(labsScroll);

        rightPane.getChildren().addAll(previewFrame, labsPane, descBox);


        // Build Sections
        List<TutorialSection> sections = createSections();
        for (TutorialSection section : sections) {
            cardsList.getChildren().add(createInstructionCard(section, isSelected -> {
                if (isSelected) {
                    if (section.isSkillAcademy()) {
                        labsPane.setManaged(true);
                        labsPane.setVisible(true);
                        setupSkillAcademy(labsBox, previewCanvas, descText, section);
                    } else {
                        labsPane.setManaged(false);
                        labsPane.setVisible(false);
                        startPreview(section, previewCanvas);
                        animateText(descText, section.description());
                    }
                }
            }, cardsList));
        }

        contentLayout.getChildren().addAll(cardsList, rightPane);
        page.getChildren().addAll(header, contentLayout);

        root.getChildren().add(page);
        AppFonts.applyTo(root);
        
        // Select first section by default
        if (!cardsList.getChildren().isEmpty()) {
            ((StackPane)cardsList.getChildren().get(0)).fireEvent(new javafx.scene.input.MouseEvent(javafx.scene.input.MouseEvent.MOUSE_CLICKED, 0, 0, 0, 0, javafx.scene.input.MouseButton.PRIMARY, 1, true, true, true, true, true, true, true, true, true, true, null));
        }

        return new Scene(root, VIEW_WIDTH, VIEW_HEIGHT);
    }

    private static StackPane createInstructionCard(TutorialSection section, java.util.function.Consumer<Boolean> onSelect, VBox container) {
        StackPane card = new StackPane();
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setPrefWidth(350);
        card.setCursor(javafx.scene.Cursor.HAND);
        
        VBox layout = new VBox(5);
        Text title = new Text(section.title());
        title.setFont(AppFonts.vt323(18));
        title.setFill(Color.web("#334155"));
        
        Text sub = new Text(section.subtitle());
        sub.setFont(AppFonts.vt323(12));
        sub.setFill(Color.web("#E2E8F0"));
        
        layout.getChildren().addAll(title, sub);
        card.getChildren().add(layout);

        String activeStyle = "-fx-background-color: linear-gradient(to right, #2563EB, #3B82F6); -fx-background-radius: 12; -fx-border-color: #60A5FA; -fx-border-width: 2; -fx-border-radius: 12;";
        String idleStyle = "-fx-background-color: rgba(255,255,255,0.6); -fx-background-radius: 12; -fx-border-color: rgba(0,0,0,0.05); -fx-border-width: 2; -fx-border-radius: 12;";

        card.setStyle(idleStyle);

        card.setOnMouseEntered(e -> {
            if (!card.getStyle().contains("#2563EB")) {
                card.setScaleX(1.03);
                card.setStyle("-fx-background-color: rgba(255,255,255,0.9); -fx-background-radius: 12; -fx-border-color: #94A3B8; -fx-border-width: 2; -fx-border-radius: 12;");
            }
        });
        
        card.setOnMouseExited(e -> {
            if (!card.getStyle().contains("#2563EB")) {
                card.setScaleX(1.0);
                card.setStyle(idleStyle);
            }
        });

        card.setOnMouseClicked(e -> {
            for (javafx.scene.Node node : container.getChildren()) {
                node.setStyle(idleStyle);
                node.setScaleX(1.0);
                ((VBox)((StackPane)node).getChildren().get(0)).getChildren().forEach(t -> {
                    if (t instanceof Text txt) txt.setFill(Color.web("#334155"));
                });
            }
            card.setStyle(activeStyle);
            title.setFill(Color.WHITE);
            sub.setFill(Color.web("#BFDBFE"));
            onSelect.accept(true);
        });

        return card;
    }

    private static void startPreview(TutorialSection section, Canvas canvas) {
        stopAnimations();
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Maze maze = section.scenarioMaze();
        final long startTime = System.currentTimeMillis();

        previewTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                long ms = System.currentTimeMillis() - startTime;
                PreviewState state = section.animate(ms);

                gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
                gc.setFill(Color.web("#1E293B"));
                gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

                MazeRenderer.render(
                    gc,
                    maze,
                    new ArrayList<com.nhom_01.robot_pathfinding.core.State>(),
                    state.path, 
                    state.x,
                    state.y,
                    canvas.getWidth(),
                    canvas.getHeight(), 
                    state.mysteryOpenStartMs,
                    state.mysteryOpenGx,
                    state.mysteryOpenGy,
                    state.bombTouchStartMs,
                    state.bombTouchGx,
                    state.bombTouchGy, 
                    state.facing,
                    true,
                    state.freezeTimeActive,
                    state.bombDetectorActive,
                    state.speedMultiplier,
                    state.shieldVisible,
                    state.activeTimerMs,
                    state.visionActive,
                    state.teleportStartMs,
                    (int)state.teleportGx,
                    (int)state.teleportGy,
                    state.teleportDestX,
                    state.teleportDestY,
                    state.flyingItems,
                    state.vfx
                );

                if (state.showPowerUps) {
                    renderPowerUpCards(gc, canvas.getWidth(), canvas.getHeight());
                }
            }
        };
        previewTimer.start();
    }

    private static void renderPowerUpCards(GraphicsContext gc, double w, double h) {
        double cardW = 120;
        double cardH = 180;
        double gap = 20;
        double totalW = cardW * 3 + gap * 2;
        double startX = (w - totalW) / 2;
        double startY = (h - cardH) / 2;

        String[] titles = {"TỐC ĐỘ", "KHIÊN", "X2 ĐIỂM"};
        Color[] colors = {Color.web("#00ACC1"), Color.web("#1E88E5"), Color.web("#F9A825")};

        for (int i = 0; i < 3; i++) {
            double cx = startX + i * (cardW + gap);
            double cy = startY;

            // Card background
            gc.setFill(Color.web("#FFFFFF", 0.95));
            gc.fillRoundRect(cx, cy, cardW, cardH, 12, 12);
            gc.setStroke(colors[i]);
            gc.setLineWidth(2);
            gc.strokeRoundRect(cx, cy, cardW, cardH, 12, 12);

            // Header
            gc.setFill(colors[i]);
            gc.fillRoundRect(cx, cy, cardW, 40, 12, 12);
            gc.fillRect(cx, cy + 30, cardW, 10);

            // Text
            gc.setFill(Color.WHITE);
            gc.setFont(AppFonts.vt323(16));
            gc.fillText(titles[i], cx + 15, cy + 25);

            // Icon placeholder
            gc.setFill(colors[i]);
            gc.fillOval(cx + cardW/2 - 20, cy + 60, 40, 40);
            gc.setFill(Color.WHITE);
            gc.fillText("?", cx + cardW/2 - 5, cy + 85);
        }
    }

    private static void animateText(Text textNode, String content) {
        if (typewriterTimeline != null) typewriterTimeline.stop();
        textNode.setText("");
        typewriterTimeline = new Timeline();
        for (int i = 0; i <= content.length(); i++) {
            final int idx = i;
            KeyFrame kf = new KeyFrame(Duration.millis(i * 15), e -> textNode.setText(content.substring(0, idx)));
            typewriterTimeline.getKeyFrames().add(kf);
        }
        typewriterTimeline.play();
    }

    private static void stopAnimations() {
        if (previewTimer != null) previewTimer.stop();
        if (typewriterTimeline != null) typewriterTimeline.stop();
    }

    private static Maze createMaze(int[][] data) {
        int rows = data.length;
        int cols = data[0].length;
        Maze m = new Maze(cols, rows);
        for (int y = 0; y < rows; y++) {
            for (int x = 0; x < cols; x++) {
                CellType type = switch (data[y][x]) {
                    case 1 -> CellType.WALL;
                    case 2 -> CellType.BOMB;
                    case 3 -> CellType.ITEM;
                    case 4 -> CellType.START;
                    case 5 -> CellType.GOAL;
                    default -> CellType.EMPTY;
                };
                m.setCell(x, y, type);
            }
        }
        return m;
    }

    private static List<TutorialSection> createSections() {
        List<TutorialSection> list = new ArrayList<>();

        // 1. MOVEMENT - Redesigned to show Water path vs Grass walls
        list.add(new TutorialSection(
            "DI CHUYỂN", "Cách đi lại cơ bản trong mê cung.",
            "Sử dụng các phím mũi tên để điều khiển Vịt. " +
            "Mỗi lần nhấn phím tương ứng với một bước đi. Vịt chỉ có thể di chuyển trên " +
            "NƯỚC (ô màu xanh) và không thể đi vào CỎ (ô màu xanh lá). " +
            "Hãy giữ nhịp độ ổn định để tránh va vào tường.",
            "Hãy tập trung di chuyển trên những vùng nước rộng để rẽ hướng dễ dàng nhất!",
            createMaze(new int[][]{
                {1,1,1,1,1},
                {1,0,0,0,1},
                {1,0,1,0,1},
                {1,0,0,0,1},
                {1,1,1,1,1}
            }),
            ms -> {
                double t = (ms % 6000) / 6000.0;
                double x, y;
                MazeRenderer.DuckFacing f;
                // Square path on water: (1,1) -> (3,1) -> (3,3) -> (1,3) -> (1,1)
                if (t < 0.25) {
                    double p = t * 4;
                    x = 1.0 + p * 2.0; y = 1.0; f = MazeRenderer.DuckFacing.RIGHT;
                } else if (t < 0.5) {
                    double p = (t - 0.25) * 4;
                    x = 3.0; y = 1.0 + p * 2.0; f = MazeRenderer.DuckFacing.DOWN;
                } else if (t < 0.75) {
                    double p = (t - 0.5) * 4;
                    x = 3.0 - p * 2.0; y = 3.0; f = MazeRenderer.DuckFacing.LEFT;
                } else {
                    double p = (t - 0.75) * 4;
                    x = 1.0; y = 3.0 - p * 2.0; f = MazeRenderer.DuckFacing.UP;
                }
                return new PreviewState(x, y, f);
            }, false
        ));

        // 2. BOMB
        list.add(new TutorialSection(
            "BOM & BẪY", "Tránh né các chướng ngại vật nguy hiểm.",
            "Bom xuất hiện ngẫu nhiên trong mê cung. Nếu dẫm phải bom, bạn sẽ bị trừ 1 mạng " +
            "và một lượng điểm lớn. Tuy nhiên, bạn có thể sử dụng các vật phẩm như KHIÊN " +
            "để bảo vệ bản thân hoặc MÁY DÒ BOM để nhìn thấy chúng từ xa.",
            "Đừng quá vội vàng! Một giây quan sát có thể cứu sống bạn.",
            createMaze(new int[][]{{0,0,0},{0,0,0},{0,2,0}}),
            ms -> {
                double cycle = (ms % 3000) / 3000.0;
                double x, y;
                long bStart = 0;
                if (cycle < 0.4) {
                    x = cycle * 2.5; y = 2.0; 
                } else {
                    x = 1.0; y = 2.0; 
                    bStart = System.currentTimeMillis() - (long)(ms % 3000 - 1200);
                }
                return new PreviewState(x, y, MazeRenderer.DuckFacing.RIGHT).withBomb(bStart, 1, 2);
            }, false
        ));

        // 3. ITEM - Redesigned to show Mystery Opening -> Card Selection
        list.add(new TutorialSection(
            "VẬT PHẨM", "Sức mạnh hỗ trợ Vịt vượt khó.",
            "Hộp bí ẩn chứa đựng những vật phẩm mạnh mẽ. Khi nhặt được, bạn sẽ được chọn " +
            "một trong các kỹ năng ngẫu nhiên. Hãy tận dụng chúng để vượt qua " +
            "các thách thức khó khăn nhất hoặc tối ưu hóa điểm số.",
            "Các kỹ năng hiếm có màu Đỏ, hãy ưu tiên chọn chúng nhé!",
            createMaze(new int[][]{
                {0,0,0,0},
                {0,3,0,0},
                {0,0,0,0}
            }),
            ms -> {
                double cycle = (ms % 6000) / 6000.0;
                double x;
                long iStart = 0;
                boolean showCards = false;
                
                if (cycle < 0.2) {
                    // Moving to item
                    x = cycle * 5.0; // 0 to 1
                } else if (cycle < 0.6) {
                    // Opening mystery box
                    x = 1.0;
                    iStart = System.currentTimeMillis() - (long)((cycle - 0.2) * 6000);
                } else {
                    // Showing selection cards
                    x = 1.0;
                    showCards = true;
                }
                return new PreviewState(x, 1.0, MazeRenderer.DuckFacing.RIGHT)
                    .withItem(iStart, 1, 1)
                    .withPowerUps(showCards);
            }, false
        ));

        // 4. BOT
        list.add(new TutorialSection(
            "CHẾ ĐỘ BOT", "Tìm hiểu về sức mạnh của AI.",
            "Trong chế độ BOT, Vịt sẽ được giải cứu bởi các thuật toán thông minh: " +
            "BFS (Tìm theo chiều rộng) đảm bảo đường đi ngắn nhất, " +
            "DFS (Tìm theo chiều sâu) khám phá mê cung táo bạo hơn, " +
            "A* là thuật toán tối ưu nhất kết hợp cả hai yếu tố trên. " +
            "Vịt là nhân vật chính cần được bạn hoặc thuật toán giải cứu khỏi mê cung.",
            "Hãy quan sát cách Bot di chuyển để học hỏi lộ trình tối ưu nhất nhé!",
            createMaze(new int[][]{{0,0,0,0,0,0},{0,1,1,1,1,0},{0,1,0,0,1,0},{0,1,0,0,1,0},{0,1,1,1,1,0},{0,0,0,0,0,0}}),
            ms -> {
                double t = (ms % 5000) / 5000.0;
                double x, y;
                MazeRenderer.DuckFacing f;
                if (t < 0.2) { x = t*5*5.0; y = 0.0; f = MazeRenderer.DuckFacing.RIGHT; }
                else if (t < 0.4) { x = 5.0; y = (t-0.2)*5*5.0; f = MazeRenderer.DuckFacing.DOWN; }
                else if (t < 0.6) { x = 5.0 - (t-0.4)*5*5.0; y = 5.0; f = MazeRenderer.DuckFacing.LEFT; }
                else if (t < 0.8) { x = 0.0; y = 5.0 - (t-0.6)*5*5.0; f = MazeRenderer.DuckFacing.UP; }
                else { x = 0.0; y = 0.0; f = MazeRenderer.DuckFacing.DOWN; }
                return new PreviewState(x, y, f);
            }, false
        ));

        // 5. SKILL ACADEMY
        list.add(new TutorialSection(
            "HỌC VIỆN KỸ NĂNG", "Trải nghiệm sức mạnh đặc biệt.",
            "Chào mừng đến với Học viện Kỹ năng! Tại đây bạn có thể thử nghiệm mọi loại Power-up " +
            "trước khi bước vào mê cung thực sự. Hãy chọn một kỹ năng bên dưới để xem cách nó hoạt động.",
            "Nắm vững kỹ năng là chìa khóa để giành điểm cao nhất!",
            createMaze(new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}}),
            ms -> new PreviewState(2.0, 1.0, MazeRenderer.DuckFacing.DOWN),
            true
        ));

        return list;
    }

    private static void setupSkillAcademy(HBox container, Canvas canvas, Text desc, TutorialSection rootSection) {
        container.getChildren().clear();
        String[] powers = {"Shield", "Speed Boost", "Teleport", "Sonar Radar", "Wall Removal", "Freeze Time"};
        Color[] colors = {Color.web("#1E88E5"), Color.web("#00ACC1"), Color.web("#CE93D8"), Color.web("#00B8D4"), Color.web("#FFAB91"), Color.web("#81D4FA")};
        
        for (int i = 0; i < powers.length; i++) {
            final int idx = i;
            Button btn = new NeonButton(powers[i], colors[i], 12, 6, 12, 6);
            btn.setMinWidth(140);
            btn.setOnAction(e -> {
                runSubDemo(idx, canvas, desc);
            });
            container.getChildren().add(btn);
        }
        
        // Pick first by default
        if (!container.getChildren().isEmpty()) {
            ((Button)container.getChildren().get(0)).fire();
        }
    }

    private static void runSubDemo(int type, Canvas canvas, Text desc) {
        TutorialSection demo;
        Maze maze = createMaze(new int[][]{{0,0,0,0,0},{0,0,0,0,0},{0,0,0,0,0}});
        
        switch (type) {
            case 0 -> demo = new TutorialSection("Shield", "", "Bảo vệ Vịt khỏi 1 lần va chạm với bom mà không bị mất mạng. Có một hào quang màu xanh bao quanh bạn.", "", maze, 
                ms -> new PreviewState(2.0, 1.0, MazeRenderer.DuckFacing.DOWN).withShield(true), true);
            case 1 -> demo = new TutorialSection("Speed Boost", "", "Tăng đáng kể tốc độ di chuyển trong một khoảng thời gian ngắn. Rất hữu ích khi cần thoát khỏi khu vực nguy hiểm.", "", maze, 
                ms -> {
                    double t = (ms % 1000) / 1000.0;
                    return new PreviewState(0.5 + t * 3.0, 1.0, MazeRenderer.DuckFacing.RIGHT).withSpeedMultiplier(0.5);
                }, true);
            case 2 -> demo = new TutorialSection("Teleport", "", "Ngay lập tức dịch chuyển Vịt đến một vị trí an toàn ngẫu nhiên trong mê cung.", "", maze, 
                ms -> {
                    long cycle = ms % 2500;
                    if (cycle < 800) return new PreviewState(1.0, 1.0, MazeRenderer.DuckFacing.DOWN);
                    else if (cycle < 1600) return new PreviewState(1.0, 1.0, MazeRenderer.DuckFacing.DOWN).withTeleport(System.currentTimeMillis() - (cycle - 800), 1, 1, 3, 1);
                    else return new PreviewState(3.0, 1.0, MazeRenderer.DuckFacing.DOWN);
                }, true);
            case 3 -> {
                Maze radarMaze = createMaze(new int[][]{{0,0,3,0,0}});
                java.util.List<com.nhom_01.robot_pathfinding.ui.FlyingItem> tutFlying = new java.util.ArrayList<>();
                demo = new TutorialSection("Sonar Radar", "", "Sử dụng sóng âm tầm xa để phát hiện và tự động hút tất cả các hộp quà bí ẩn ở gần về phía Vịt.", "", radarMaze, 
                ms -> {
                    double t = (ms % 3000) / 3000.0;
                    PreviewState ps = new PreviewState(0.5, 0.0, MazeRenderer.DuckFacing.RIGHT).withVision(true, 5000L);
                    if (t < 0.1) {
                        radarMaze.setCell(2, 0, CellType.ITEM);
                        tutFlying.clear();
                    } else if (t > 0.4 && radarMaze.getCell(2,0) == CellType.ITEM) {
                        radarMaze.setCell(2, 0, CellType.EMPTY);
                        tutFlying.add(new com.nhom_01.robot_pathfinding.ui.FlyingItem(2, 0, 180));
                        ps.withItem(System.currentTimeMillis(), 2, 0);
                    }
                    for (com.nhom_01.robot_pathfinding.ui.FlyingItem fi : tutFlying) {
                        fi.update(ps.x, ps.y);
                    }
                    ps.flyingItems.addAll(tutFlying);
                    return ps;
                }, true);
            }
            case 4 -> {
                Maze wallMaze = createMaze(new int[][]{{0,0,1,0,0}});
                demo = new TutorialSection("Wall Removal", "", "Một luồng năng lượng đặc biệt cho phép Vịt phá hủy tường khi chạm vào, tạo ra những con đường tắt mới.", "", wallMaze, 
                ms -> {
                    double t = (ms % 2500) / 2500.0;
                    double x = 0.5 + t * 4.0;
                    if (x >= 1.5) wallMaze.setCell(2, 0, CellType.EMPTY); // Vanish as soon as touched
                    if (t < 0.1) wallMaze.setCell(2, 0, CellType.WALL);
                    return new PreviewState(x, 0.0, MazeRenderer.DuckFacing.RIGHT);
                }, true);
            }
            case 5 -> {
                Maze bombMaze = createMaze(new int[][]{{0,0,2,0,0},{0,0,0,0,0}});
                demo = new TutorialSection("Freeze Time", "", "Đóng băng tất cả bom và bẫy trong mê cung. Bom bị đóng băng sẽ đóng vai trò như những bức tường kiên cố mà bạn không thể đi xuyên qua.", "", bombMaze, 
                ms -> {
                    double t = (ms % 3000) / 3000.0;
                    double x, y;
                    MazeRenderer.DuckFacing f;
                    if (t < 0.3) { x = 0.5 + t*3.0; y = 0.0; f = MazeRenderer.DuckFacing.RIGHT; }
                    else if (t < 0.6) { x = 1.4; y = (t-0.3)*3.0; f = MazeRenderer.DuckFacing.DOWN; } // Turn away
                    else { x = 1.4 + (t-0.6)*4.0; y = 1.0; f = MazeRenderer.DuckFacing.RIGHT; }
                    return new PreviewState(x, y, f).withFreeze(true);
                }, true);
            }
            default -> demo = new TutorialSection("Welcome", "", "Chào mừng đến với Học viện Kỹ năng! Hãy chọn một kỹ năng để bắt đầu.", "", maze, 
                ms -> new PreviewState(2.0, 1.0, MazeRenderer.DuckFacing.DOWN), false);
        }
        
        startPreview(demo, canvas);
        animateText(desc, demo.description());
    }

    private record TutorialSection(
        String title, String subtitle, String description, String tip,
        Maze scenarioMaze, java.util.function.Function<Long, PreviewState> animator,
        boolean isSkillAcademy
    ) {
        public PreviewState animate(long ms) { return animator.apply(ms); }
    }

    private static class PreviewState {
        double x, y;
        MazeRenderer.DuckFacing facing;
        long mysteryOpenStartMs = 0;
        int mysteryOpenGx = -1;
        int mysteryOpenGy = -1;
        long bombTouchStartMs = 0;
        int bombTouchGx = -1;
        int bombTouchGy = -1;
        boolean showPowerUps = false;

        // New Power-Up Effects
        boolean shieldVisible = false;
        boolean visionActive = false;
        boolean freezeTimeActive = false;
        boolean bombDetectorActive = false;
        double speedMultiplier = 1.0;
        long activeTimerMs = -1L;
        long teleportStartMs = 0;
        double teleportGx = -1, teleportGy = -1;
        int teleportDestX = -1, teleportDestY = -1;
        List<State> path = new ArrayList<>();
        java.util.List<com.nhom_01.robot_pathfinding.ui.FlyingItem> flyingItems = new java.util.ArrayList<>();
        MazeRenderer.RenderVfx vfx = new MazeRenderer.RenderVfx(0L, 0L, 0, 0L, 0L);

        PreviewState(double x, double y, MazeRenderer.DuckFacing f) {
            this.x = x; this.y = y; this.facing = f;
        }
        PreviewState withItem(long start, int gx, int gy) {
            this.mysteryOpenStartMs = start; this.mysteryOpenGx = gx; this.mysteryOpenGy = gy;
            return this;
        }
        PreviewState withBomb(long start, int gx, int gy) {
            this.bombTouchStartMs = start; this.bombTouchGx = gx; this.bombTouchGy = gy;
            return this;
        }
        PreviewState withPowerUps(boolean show) {
            this.showPowerUps = show;
            return this;
        }
        PreviewState withShield(boolean v) { this.shieldVisible = v; return this; }
        PreviewState withVision(boolean v, long timer) { this.visionActive = v; this.activeTimerMs = timer; return this; }
        PreviewState withFreeze(boolean v) { this.freezeTimeActive = v; return this; }
        PreviewState withDetector(boolean v) { this.bombDetectorActive = v; return this; }
        PreviewState withSpeedMultiplier(double v) { this.speedMultiplier = v; return this; }
        PreviewState withPath(List<State> p) { this.path = p; return this; }
        PreviewState withVfx(long l, long s, int sv, long t, long ifr) { this.vfx = new MazeRenderer.RenderVfx(l, s, sv, t, ifr); return this; }
        PreviewState withTeleport(long start, double sx, double sy, int dx, int dy) {
            this.teleportStartMs = start; this.teleportGx = sx; this.teleportGy = sy;
            this.teleportDestX = dx; this.teleportDestY = dy;
            return this;
        }
    }
}
