package systems;

import buildings.Building;
import buildings.BuildingType;
import core.Camera;
import core.EntityManager;
import entities.Player;
import ui.BuildMenu;
import world.World;

import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;
import static ui.BuildMenu.buildingTextures;
import static buildings.Building.size;

public class BuildSystem {
    BuildingType[] buildingType = {
            new BuildingType(buildingTextures[0], 20),
            new BuildingType(buildingTextures[1], 30),
            new BuildingType(buildingTextures[2], 40)
    };

    // Tracking selected building
    private BuildingType selectedBuilding = null;
    private int snappedX, snappedY;
    final private Rectangle previewRec = newRectangle(0, 0, size, size);
    private boolean validPlacement;

    // Grid occupancy system
    private final int cols = World.worldWidth / World.tileSize;
    private final int rows = World.worldHeight / World.tileSize;
    private final boolean[][] occupiedTiles = new boolean[cols][rows];
    private int tileX, tileY;
    private final int buildingTile = size / World.tileSize; // each building is 64x64, and each tile is 32x32, so it's a factor of 2

    public boolean checkValidPlacement() {
        if (selectedBuilding == null) return false;

        // Check if user has enough material
        if (Player.numStone < selectedBuilding.cost) return false;

        // Check if within world boundaries
        if (!(snappedX >= 0 && snappedX <= World.worldWidth - size && snappedY >= 0 && snappedY <= World.worldHeight - size)) return false;

        // Check if clicking on build HUD
        if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.menuRect)) return false;

        // Check if overlapping with already placed building
        for (int x = tileX; x < tileX + buildingTile; x++) {
            for (int y = tileY; y < tileY + buildingTile; y++) {
                if (occupiedTiles[x][y]) {
                    return false;
                }
            }
        }

        // Check if overlapping with stone
        for (Rectangle stoneRect : EntityManager.stoneRects) {
            if (CheckCollisionRecs(previewRec, stoneRect)) {
                return false;
            }
        }

        // Check if overlapping with player
        if (CheckCollisionRecs(previewRec, Player.playerRec)) return false;

        // if all checks pass, return true
        return true;
    }

    public void keyBinds() {
        if (IsKeyPressed(KEY_ONE)) {
            selectedBuilding = buildingType[0];
        }
        if (IsKeyPressed(KEY_TWO)) {
            selectedBuilding = buildingType[1];
        }
        if (IsKeyPressed(KEY_THREE)) {
            selectedBuilding = buildingType[2];
        }
    }

    public void getClickedBuilding() {
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            for (int i = 0; i < BuildMenu.buildingPositions.length; i++) {
                if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.buildingPositions[i])) {
                    selectedBuilding = buildingType[i];
                }
            }
        }

        // check if user uses any keybinds
        keyBinds();

        // cancel placing if user clicks rmb
        if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
            selectedBuilding = null;
        }
    }

    public void occupyTiles() {
        for (int x = tileX; x < tileX + buildingTile; x++) {
            for (int y = tileY; y < tileY + buildingTile; y++) {
                occupiedTiles[x][y] = true;
            }
        }
    }

    public void getPreviewPosition() {
        // get mouse position (world, not screen)
        Vector2 mouse = GetMousePosition();
        mouse = GetScreenToWorld2D(mouse, Camera.camera);

        // snap mouse pos so player can only place on tiles
        snappedX = (int) Math.floor(mouse.x() / World.tileSize) * World.tileSize;
        snappedY = (int) Math.floor(mouse.y() / World.tileSize) * World.tileSize;

        // update tile xy
        tileX = snappedX / World.tileSize;
        tileY = snappedY / World.tileSize;

        // update preview rectangle
        previewRec.x(snappedX);
        previewRec.y(snappedY);
    }

    public void update() {
        getClickedBuilding();

        getPreviewPosition();

        // check if building can be placed
        validPlacement = checkValidPlacement();

        // check if user fits all requirements to place a building
        if (selectedBuilding != null && IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && validPlacement) {
            // add building to arraylist
            EntityManager.placedBuildings.add(new Building(newVector2(snappedX, snappedY), selectedBuilding));
            occupyTiles();
            Player.numStone -= selectedBuilding.cost;
        }
    }

    public void drawPreview() {
        // show red tint if not valid placement (e.x. overlapping with already placed building)
        Color previewColor = validPlacement
                ? newColor(255,255,255,150)
                : newColor(255,0,0,150);

        // Draw preview
        if (selectedBuilding != null) {
            DrawTextureEx(
                    selectedBuilding.texture,
                    newVector2(snappedX, snappedY),
                    0,
                    1.0f,
                    previewColor);
        }
    }
}
