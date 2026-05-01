package systems;

import buildings.Building;
import core.Camera;
import core.EntityManager;
import ui.BuildMenu;
import world.World;

import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;

public class BuildSystem {

    // Tracking selected building
    private int selectedBuilding = -1;
    private boolean isPlacing = false;
    private int snappedX, snappedY;
    private Rectangle previewRec;
    private boolean validPlacement;

    public void draw() {
        Color previewColor = validPlacement
                ? newColor(255,255,255,150)
                : newColor(255,0,0,150);

        // Draw preview
        if (isPlacing && selectedBuilding != -1) {
            DrawTextureEx(
                    BuildMenu.buildingTextures[selectedBuilding],
                    newVector2(snappedX, snappedY),
                    0,
                    1.0f,
                    previewColor);
        }
    }

    public boolean checkValidPlacement() {
        // within world boundaries
        if (!(snappedX >= 0 && snappedX <= World.worldWidth - 64 && snappedY >= 0 && snappedY <= World.worldHeight - 64)) {
            return false;
        }

        // not clicking on build HUD
        if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.menuRect)) {
            return false;
        }

        // overlap with already placed building
        for (Building building : EntityManager.placedBuildings) {
            Rectangle curRec = newRectangle(building.position.x(), building.position.y(), 64, 64);
            if (CheckCollisionRecs(previewRec, curRec)) {
                return false;
            }
        }

        return true;
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

        // cancel placing if user clicks rmb
        if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
            isPlacing = false;
            selectedBuilding = -1;
        }

        Vector2 mouse = GetMousePosition();
        mouse = GetScreenToWorld2D(mouse, Camera.camera);

        // snap mouse pos so player can only place on tiles
        snappedX = (int) Math.floor(mouse.x() / World.tileSize) * World.tileSize;
        snappedY = (int) Math.floor(mouse.y() / World.tileSize) * World.tileSize;

        // update previewRec
        previewRec = newRectangle(snappedX, snappedY, 64, 64);

        // check if building can be placed
        validPlacement = checkValidPlacement();

        // if user places a building in a valid position
        if (isPlacing && IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && validPlacement) {
            // add building to arraylist
            EntityManager.placedBuildings.add(new Building(
                    newVector2(snappedX, snappedY),
                    selectedBuilding
            ));
//            isPlacing = false;
//            selectedBuilding = -1;
        }
    }
}
