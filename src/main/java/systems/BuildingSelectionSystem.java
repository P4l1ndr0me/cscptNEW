package systems;

import buildings.Building;
import core.Camera;
import core.EntityManager;
import core.Main;
import entities.Player;
import ui.BuildMenu;

import static com.raylib.Colors.*;
import static com.raylib.Helpers.newRectangle;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static core.Main.pixelFont;

public class BuildingSelectionSystem {
    private Building selectedBuilding = null;
    private final BuildSystem buildSystem;

    // Center screen UI panel
    private static final float PANEL_WIDTH = 360;
    private static final float PANEL_HEIGHT = 250;

    private static final float PANEL_X = Main.SCREEN_WIDTH / 2f - PANEL_WIDTH / 2f;
    private static final float PANEL_Y = Main.SCREEN_HEIGHT / 2f - PANEL_HEIGHT / 2f;

    private final Rectangle panelRect = newRectangle(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);

    private final Rectangle upgradeButton = newRectangle(
            PANEL_X + 30,
            PANEL_Y + 190,
            135,
            40
    );

    private final Rectangle sellButton = newRectangle(
            PANEL_X + 195,
            PANEL_Y + 190,
            135,
            40
    );

    public BuildingSelectionSystem(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }

    public void update() {
        // If player has just placed building, do not instantly select it
        if (buildSystem.placedBuildingThisFrame()) {
            return;
        }

        // If player is currently placing building, prevent user from selecting
        if (buildSystem.isPlacingBuilding()) {
            selectedBuilding = null;
            return;
        }

        handleKeybinds();

        // Only check selection if player left clicks
        if (!IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            return;
        }


        Vector2 mouseScreen = GetMousePosition();

        // Do not select buildings when clicking the build menu
        if (CheckCollisionPointRec(mouseScreen, BuildMenu.MENU_RECT)) {
            clearSelection();
            return;
        }

        // Prevent building panel from disappearing when clicking it
        if (selectedBuilding != null && CheckCollisionPointRec(mouseScreen, panelRect)) {
            if (CheckCollisionPointRec(mouseScreen, upgradeButton)) {
                upgradeSelectedBuilding();
            }

            if (CheckCollisionPointRec(mouseScreen, sellButton)) {
                sellSelectedBuilding();
            }

            return;
        }

        // Convert mouse screen position to world position
        Vector2 mouseWorld = GetScreenToWorld2D(mouseScreen, Camera.camera);

        // Assume the player clicked empty space
        clearSelection();

        // Check if the player clicked any placed building
        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionPointRec(mouseWorld, building.getRect())) {
                selectedBuilding = building;
                break;
            }
        }
    }

    public void drawUI() {
        if (selectedBuilding == null) {
            return;
        }

        // Panel background
        DrawRectangleRounded(panelRect, 0.15f, 0, BuildMenu.MENU_FILL);
        DrawRectangleRoundedLinesEx(panelRect, 0.15f, 0, 3.0f, BLACK);

        // Title
        DrawTextEx(
                pixelFont,
                selectedBuilding.type.name,
                newVector2(PANEL_X + 30, PANEL_Y + 20),
                32,
                1.0f,
                WHITE
        );

        // Health
        DrawTextEx(
                pixelFont,
                "Health: " + selectedBuilding.health + " / " + selectedBuilding.maxHealth,
                newVector2(PANEL_X + 30, PANEL_Y + 60),
                24,
                1.0f,
                WHITE
        );

        // Level
        DrawTextEx(
                pixelFont,
                "Level: " + selectedBuilding.level + " / " + selectedBuilding.maxLevel,
                newVector2(PANEL_X + 30, PANEL_Y + 90),
                24,
                1.0f,
                WHITE
        );

        // Damage
        DrawTextEx(
                pixelFont,
                "Damage: " + selectedBuilding.damage,
                newVector2(PANEL_X + 30, PANEL_Y + 120),
                24,
                1.0f,
                WHITE
        );

        String upgradeCostText;
        if (selectedBuilding.canUpgrade()) {
            upgradeCostText = "Upgrade: "
                    + selectedBuilding.getUpgradeStoneCost()
                    + " stone, "
                    + selectedBuilding.getUpgradeGoldCost()
                    + " gold";
        } else if (selectedBuilding.level < selectedBuilding.maxLevel) {
            upgradeCostText = "Upgrade Gold Stash first";
        } else {
            upgradeCostText = "";
        }


        DrawTextEx(
                pixelFont,
                upgradeCostText,
                newVector2(PANEL_X + 30, PANEL_Y + 150),
                18,
                1.0f,
                WHITE
        );

        // Draw upgrade button
        Color upgradeColor = selectedBuilding.canUpgrade()
                ? GREEN
                : GRAY;

        String upgradeText;
        if (selectedBuilding.canUpgrade()) {
            upgradeText = "Upgrade (E)";
        } else if (selectedBuilding.level < selectedBuilding.maxLevel) {
            upgradeText = "Locked";
        } else {
            upgradeText = "MAX LEVEL";
        }

        drawButton(upgradeButton, upgradeText, upgradeColor);

        // Draw sell button
        Color sellColor = selectedBuilding.type.name.equals("Gold Stash")
                ? GRAY
                : RED;

        drawButton(sellButton, "Sell (Q)", sellColor);
    }

    private void drawButton(Rectangle button, String text, Color color) {
        DrawRectangleRounded(button, 0.25f, 0, color);
        DrawRectangleRoundedLinesEx(button, 0.25f, 0, 2.0f, WHITE);

        float fontSize = 20f;
        float spacing = 1.0f;

        Vector2 textSize = MeasureTextEx(pixelFont, text, fontSize, spacing);

        float textX = button.x() + (button.width() - textSize.x()) / 2f;
        float textY = button.y() + (button.height() - textSize.y()) / 2f;

        DrawTextEx(
                pixelFont,
                text,
                newVector2(textX, textY),
                fontSize,
                spacing,
                BLACK
        );
    }

    private void upgradeSelectedBuilding() {
        if (selectedBuilding == null) {
            return;
        }

        if (!selectedBuilding.canUpgrade()) {
            return;
        }

        int stoneCost = selectedBuilding.getUpgradeStoneCost();
        int goldCost = selectedBuilding.getUpgradeGoldCost();

        if (Player.numStone < stoneCost || Player.numGold < goldCost) {
            return;
        }

        Player.numStone -= stoneCost;
        Player.numGold -= goldCost;

        selectedBuilding.upgrade();
    }

    private void sellSelectedBuilding() {
        if (selectedBuilding == null) {
            return;
        }

        // Gold Stash cannot be sold
        if (selectedBuilding.type.name.equals("Gold Stash")) {
            return;
        }

        // Refund 50% of the original cost
        int stoneRefund = selectedBuilding.type.stoneCost / 2;
        int goldRefund = selectedBuilding.type.goldCost / 2;

        Player.numStone += stoneRefund;
        Player.numGold += goldRefund;

        BuildSystem.destroyBuilding(selectedBuilding);

        // Close the selected building UI
        clearSelection();
    }

    private void handleKeybinds() {
        if (selectedBuilding == null) {
            return;
        }

        if (IsKeyPressed(KEY_E)) {
            upgradeSelectedBuilding();
        }

        if (IsKeyPressed(KEY_Q)) {
            sellSelectedBuilding();
        }
    }

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public void clearSelection() {
        selectedBuilding = null;
    }
}
