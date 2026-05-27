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
    private static final float PANEL_HEIGHT = 220;

    private static final float PANEL_X = Main.SCREEN_WIDTH / 2f - PANEL_WIDTH / 2f;
    private static final float PANEL_Y = Main.SCREEN_HEIGHT / 2f - PANEL_HEIGHT / 2f;

    private final Rectangle panelRect = newRectangle(PANEL_X, PANEL_Y, PANEL_WIDTH, PANEL_HEIGHT);

    private final Rectangle upgradeButton = newRectangle(
            PANEL_X + 40,
            PANEL_Y + 155,
            120,
            40
    );

    private final Rectangle sellButton = newRectangle(
            PANEL_X + 200,
            PANEL_Y + 155,
            120,
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

    public void drawWorld() {
        if (selectedBuilding == null) {
            return;
        }

        // Draw an outline around the selected building
        DrawRectangleLinesEx(selectedBuilding.getRect(), 3.0f, YELLOW);

        drawUI();
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
                newVector2(PANEL_X + 30, PANEL_Y + 25),
                32,
                1.0f,
                WHITE
        );

        // Health
        DrawTextEx(
                pixelFont,
                "Health: " + selectedBuilding.health + " / " + selectedBuilding.type.maxHealth,
                newVector2(PANEL_X + 30, PANEL_Y + 75),
                24,
                1.0f,
                WHITE
        );

        // Temporary level text
        DrawTextEx(
                pixelFont,
                "Level: 1",
                newVector2(PANEL_X + 30, PANEL_Y + 110),
                24,
                1.0f,
                WHITE
        );

        drawButton(upgradeButton, "Upgrade", GREEN);

        Color sellColor = selectedBuilding.type.name.equals("Gold Stash")
                ? GRAY
                : RED;

        drawButton(sellButton, "Sell", sellColor);
    }

    private void drawButton(Rectangle button, String text, Color color) {
        DrawRectangleRounded(button, 0.25f, 0, color);
        DrawRectangleRoundedLinesEx(button, 0.25f, 0, 2.0f, WHITE);

        DrawTextEx(
                pixelFont,
                text,
                newVector2(button.x() + 18, button.y() + 10),
                20,
                1.0f,
                BLACK
        );
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

        // Free occupied grid tiles
        buildSystem.freeTiles(selectedBuilding);

        // Remove building from placed buildings
        EntityManager.placedBuildings.remove(selectedBuilding);

        // Close the selected building UI
        clearSelection();
    }

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public void clearSelection() {
        selectedBuilding = null;
    }
}
