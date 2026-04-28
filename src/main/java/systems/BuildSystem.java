package systems;

import buildings.Building;
import core.Camera;
import core.EntityManager;
import ui.BuildMenu;
import world.World;

import static com.raylib.Helpers.newColor;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class BuildSystem {

    // Tracking selected building
    private int selectedBuilding = -1;
    private boolean isPlacing = false;
    private float snappedX, snappedY;

    public void drawPreview() {
        // Draw preview
        if (isPlacing && selectedBuilding != -1) {
            DrawTextureEx(
                    BuildMenu.buildingTextures[selectedBuilding],
                    newVector2(snappedX, snappedY),
                    0,
                    1.0f,
                    newColor(255, 255, 255, 150) // transparent
            );

            // cancel placing if user clicks rmb
            if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
                isPlacing = false;
            }
        }

        // if user clicks lmb and it is not on the ui
        if (isPlacing && IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && !CheckCollisionPointRec(GetMousePosition(), BuildMenu.menuRect)) {
            // add building to arraylist
            EntityManager.placedBuildings.add(new Building(
                    newVector2(snappedX, snappedY),
                    selectedBuilding
            ));
            isPlacing = false; // exit placement mode
        }
    }

    public int getClickedBuilding() {
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            for (int i = 0; i < BuildMenu.buildingPositions.length; i++) {
                if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.buildingPositions[i])) {
                    return i; // which building was clicked
                }
            }
        }
        return -1; // if none were clicked
    }

    public void update() {
        int clicked = getClickedBuilding();

        if (clicked != -1) {
            selectedBuilding = clicked;
            isPlacing = true;
        }

        Vector2 mouse = GetMousePosition();
        mouse = GetScreenToWorld2D(mouse, Camera.camera);

        // snap mouse pos so player can only place on tiles
        snappedX = (float) Math.floor(mouse.x() / World.tileSize) * World.tileSize;
        snappedY = (float) Math.floor(mouse.y() / World.tileSize) * World.tileSize;
    }
}
