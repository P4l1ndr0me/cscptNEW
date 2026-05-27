package systems;

import buildings.Building;
import core.Camera;
import core.EntityManager;
import ui.BuildMenu;

import static com.raylib.Colors.YELLOW;
import static com.raylib.Raylib.*;

public class BuildingSelectionSystem {
    private Building selectedBuilding = null;
    private final BuildSystem buildSystem;

    public BuildingSelectionSystem(BuildSystem buildSystem) {
        this.buildSystem = buildSystem;
    }
    public void update() {
        // If player has just placed building, do not instantly select it
        if (buildSystem.placedBuildingThisFrame()) {
            return;
        }

//        // Right click deselects the currently selected building
//        if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
//            selectedBuilding = null;
//            return;
//        }

        // Only check selection on left click
        if (!IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            return;
        }

        // Assume the player clicked empty space
        selectedBuilding = null;

        // Do not select/deselect buildings when clicking the build menu
        if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.MENU_RECT)) {
            return;
        }

        // Convert mouse screen position to world position
        Vector2 mouseWorld = GetScreenToWorld2D(GetMousePosition(), Camera.camera);

        // Check if the player clicked any placed building
        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionPointRec(mouseWorld, building.getRect())) {
                selectedBuilding = building;
                break;
            }
        }
    }

    public void draw() {
        if (selectedBuilding == null) {
            return;
        }

        // Draw an outline around the selected building
        DrawRectangleLinesEx(selectedBuilding.getRect(), 3.0f, YELLOW);
    }

    public Building getSelectedBuilding() {
        return selectedBuilding;
    }

    public void clearSelection() {
        selectedBuilding = null;
    }
}
