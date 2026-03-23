package com.nhom_01.robot_pathfinding.ui.components;

import com.nhom_01.robot_pathfinding.core.CollectedPowerUp;
import com.nhom_01.robot_pathfinding.core.PowerUp;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class InventoryPanel {
    private final FlowPane inventoryContainer;
    private final List<CollectedPowerUp> inventory;
    private Consumer<CollectedPowerUp> onActivateCallback;

    public InventoryPanel() {
        this.inventory = new ArrayList<>();
        this.onActivateCallback = null;

        inventoryContainer = new FlowPane(10, 10);
        inventoryContainer.setAlignment(Pos.CENTER_LEFT);
        inventoryContainer.setPrefWrapLength(800);
        inventoryContainer.setStyle(
            "-fx-padding: 10;" +
            "-fx-background-color: rgba(255,255,255,0.92);" +
            "-fx-border-color: rgba(0,0,0,0.10);" +
            "-fx-border-width: 1;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;"
        );
    }

    public void setOnActivateCallback(Consumer<CollectedPowerUp> callback) {
        this.onActivateCallback = callback;
    }

    public void addCollectedPowerUp(PowerUp powerUp) {
        CollectedPowerUp collected = new CollectedPowerUp(powerUp);
        inventory.add(collected);
        updateDisplay();
    }

    public void activatePowerUp(int index) {
        if (index >= 0 && index < inventory.size()) {
            CollectedPowerUp collected = inventory.get(index);
            collected.activate();
            if (onActivateCallback != null) {
                onActivateCallback.accept(collected);
            }
            updateDisplay();
        }
    }

    public void removePowerUp(int index) {
        if (index >= 0 && index < inventory.size()) {
            inventory.remove(index);
            updateDisplay();
        }
    }

    public List<CollectedPowerUp> getInventory() {
        return inventory;
    }

    public int getInventorySize() {
        return inventory.size();
    }

    private void updateDisplay() {
        inventoryContainer.getChildren().clear();

        if (inventory.isEmpty()) {
            Text emptyText = new Text("No items collected yet");
            emptyText.setFont(Font.font("Arial", 12));
            emptyText.setFill(Color.web("#6B7A82"));
            inventoryContainer.getChildren().add(emptyText);
            return;
        }

        for (int i = 0; i < inventory.size(); i++) {
            CollectedPowerUp collected = inventory.get(i);
            HBox itemBox = createInventoryItem(collected, i);
            inventoryContainer.getChildren().add(itemBox);
        }
    }

    private HBox createInventoryItem(CollectedPowerUp collected, int index) {
        HBox itemBox = new HBox(8);
        itemBox.setAlignment(Pos.CENTER);
        itemBox.setPrefWidth(120);
        itemBox.setPrefHeight(80);
        itemBox.setPadding(new Insets(8));

        PowerUp powerUp = collected.getPowerUp();
        Color borderColor = collected.isActive() ? Color.web("#2E7D32") : Color.web("#EF6C00");
        String bgColor = collected.isActive() ? "rgba(76,175,80,0.14)" : "rgba(255,183,77,0.16)";

        itemBox.setStyle(
            "-fx-background-color: " + bgColor + ";" +
            "-fx-border-color: " + borderColor.toString().replace("0x", "#") + ";" +
            "-fx-border-width: 2;" +
            "-fx-border-radius: 8;" +
            "-fx-background-radius: 8;" +
            "-fx-cursor: hand;"
        );

        VBox content = new VBox(4);
        content.setAlignment(Pos.CENTER);
        content.setPrefWidth(104);

        Text nameText = new Text(powerUp.getDisplayName());
        nameText.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        nameText.setFill(Color.web("#1F2D3A"));
        nameText.setTextAlignment(TextAlignment.CENTER);
        nameText.setWrappingWidth(100);

        Text statusText = new Text(collected.isActive() ? "ACTIVE" : "READY");
        statusText.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        statusText.setFill(collected.isActive() ? Color.web("#00FF9C") : Color.web("#FFB800"));

        content.getChildren().addAll(nameText, statusText);
        itemBox.getChildren().add(content);

        // Click to activate
        itemBox.setOnMouseClicked(e -> {
            if (onActivateCallback != null) {
                activatePowerUp(index);
            }
        });

        // Hover effect
        itemBox.setOnMouseEntered(e -> {
            itemBox.setStyle(
                "-fx-background-color: " + bgColor.replace("0.15", "0.25") + ";" +
                "-fx-border-color: " + borderColor.toString().replace("0x", "#") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
        });

        itemBox.setOnMouseExited(e -> {
            itemBox.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-border-color: " + borderColor.toString().replace("0x", "#") + ";" +
                "-fx-border-width: 2;" +
                "-fx-border-radius: 8;" +
                "-fx-background-radius: 8;" +
                "-fx-cursor: hand;"
            );
        });

        return itemBox;
    }

    public FlowPane getContainer() {
        return inventoryContainer;
    }
}
