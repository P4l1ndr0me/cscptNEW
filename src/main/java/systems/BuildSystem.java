package systems;

import buildings.Building;
import core.Camera;
import core.EntityManager;
import ui.BuildMenu;
import world.World;

import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;

public class BuildSystem {
    public static final int BUILDING_SIZE = 64;

    // Tracking selected building
    private int selectedBuilding = -1;
    private boolean isPlacing = false;
    private int snappedX, snappedY;
    private boolean validPlacement;

    // Grid occupancy system
    private final int cols = World.worldWidth / World.tileSize;
    private final int rows = World.worldHeight / World.tileSize;
    private boolean[][] occupiedTiles = new boolean[cols][rows];
    private int tileX, tileY;
    private final int buildingTile = 2; // each building is 64x64, and each tile is 32x32, so its a factor of 2

    public void drawPreview() {
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
        // Check if within world boundaries
        if (!(snappedX >= 0 && snappedX <= World.worldWidth - BUILDING_SIZE && snappedY >= 0 && snappedY <= World.worldHeight - BUILDING_SIZE)) {
            return false;
        }

        // Check if clicking on build HUD
        if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.menuRect)) {
            return false;
        }

        // Check if overlapping with already placed building
        for (int x = tileX; x < tileX + buildingTile; x++) {
            for (int y = tileY; y < tileY + buildingTile; y++) {
                if (occupiedTiles[x][y]) {
                    return false;
                }
            }
        }

        // Check if overlapping with stone
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

    public void keyBinds() {
        if (IsKeyPressed(KEY_ONE)) {
            selectedBuilding = 0;
            isPlacing = true;
        }
        if (IsKeyPressed(KEY_TWO)) {
            selectedBuilding = 1;
            isPlacing = true;
        }
        if (IsKeyPressed(KEY_THREE)) {
            selectedBuilding = 2;
            isPlacing = true;
        }
    }

    public void occupyTiles() {
        for (int x = tileX; x < tileX + buildingTile; x++) {
            for (int y = tileY; y < tileY + buildingTile; y++) {
                occupiedTiles[x][y] = true;
            }
        }
    }

    public void update() {
        int clicked = getClickedBuilding();

        if (clicked != -1) {
            selectedBuilding = clicked;
            isPlacing = true;
        }

        keyBinds();

        // cancel placing if user clicks rmb
        if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
            isPlacing = false;
            selectedBuilding = -1;
        }

        // get mouse position (world, not screen)
        Vector2 mouse = GetMousePosition();
        mouse = GetScreenToWorld2D(mouse, Camera.camera);

        // snap mouse pos so player can only place on tiles
        snappedX = (int) Math.floor(mouse.x() / World.tileSize) * World.tileSize;
        snappedY = (int) Math.floor(mouse.y() / World.tileSize) * World.tileSize;

        // update tile xy
        tileX = snappedX / World.tileSize;
        tileY = snappedY / World.tileSize;

        // check if building can be placed
        validPlacement = checkValidPlacement();

        // if user places a building in a valid position
        if (isPlacing && IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && validPlacement) {
            // add building to arraylist
            EntityManager.placedBuildings.add(new Building(newVector2(snappedX, snappedY), selectedBuilding));
            occupyTiles();

            // if placing a building will keep the preview on (uncomment to toggle)
            // isPlacing = false;
            // selectedBuilding = -1;
        }
    }
}
