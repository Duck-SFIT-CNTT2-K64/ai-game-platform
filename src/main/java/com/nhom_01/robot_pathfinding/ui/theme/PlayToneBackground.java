package com.nhom_01.robot_pathfinding.ui.theme;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;

import java.io.InputStream;

public final class PlayToneBackground {

    private static final double TILE_SIZE = 50;

    private PlayToneBackground() {
    }

    public static Pane create(double width, double height, Class<?> owner) {
        Pane pane = new Pane();
        pane.setPrefSize(width, height);

        Canvas canvas = new Canvas(width, height);
        draw(canvas.getGraphicsContext2D(), width, height, loadTexture(owner));
        pane.getChildren().add(canvas);
        return pane;
    }

    public static void draw(GraphicsContext gc, double width, double height, Class<?> owner) {
        draw(gc, width, height, loadTexture(owner));
    }

    private static void draw(GraphicsContext gc, double width, double height, Image texture) {
        if (texture != null && !texture.isError()) {
            gc.drawImage(texture, 0, 0, width, height);
            return;
        }

        for (int y = 0; y < (int) height; y++) {
            double ratio = y / Math.max(1.0, height);
            int r = (int) (248 + (236 - 248) * ratio);
            int g = (int) (209 + (198 - 209) * ratio);
            int b = (int) (142 + (130 - 142) * ratio);
            gc.setStroke(Color.rgb(r, g, b));
            gc.strokeLine(0, y, width, y);
        }

        gc.setFill(Color.color(0.92, 0.72, 0.45, 0.35));
        for (int x = 0; x < width; x += 18) {
            for (int y = 0; y < height; y += 18) {
                if (((x + y) / 18) % 3 == 0) {
                    gc.fillOval(x + 2, y + 2, 3, 3);
                }
            }
        }
    }

    private static Image loadTexture(Class<?> owner) {
        try (InputStream stream = owner.getResourceAsStream("/image/pixel_MainMenu/background.png")) {
            if (stream == null) {
                return null;
            }
            return new Image(stream);
        } catch (Exception ex) {
            return null;
        }
    }
}
