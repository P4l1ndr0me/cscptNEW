package systems;

import buildings.*;
import core.Camera;
import core.EntityManager;
import core.TextureManager;
import entities.Player;
import ui.BuildMenu;
import world.*;

import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;
import static buildings.Building.size;

public class BuildSystem {
    public static Texture[] buildingTextures = {
            TextureManager.getTexture("Gold Mine"),
            TextureManager.getTexture("Cannon Base"),
            TextureManager.getTexture("Arrow Base"),
            TextureManager.getTexture("Gold Stash")
    };

    public static Texture[] buildingIconTextures = {
            TextureManager.getTexture("Gold Mine"),
            TextureManager.getTexture("Cannon Tower Combined"),
            TextureManager.getTexture("Arrow Base"), // will change to arrow tower combined
            TextureManager.getTexture("Gold Stash")
    };

    public static BuildingType[] buildingTypes = {
            new BuildingType("Gold Mine", buildingTextures[0], 50, 0, 8, 200),
            new BuildingType("Cannon Tower", buildingTextures[1], 75, 40, 10, 150),
            new BuildingType("Arrow Tower", buildingTextures[2], 60, 30, 10, 100),
            new BuildingType("Gold Stash", buildingTextures[3], 0,0, 1, 500)
    };

    // Tracking selected building
    private BuildingType selectedBuilding = null;
    private int selectedIndex = -1;
    private int snappedX, snappedY;
    private Rectangle previewRec;
    private boolean validPlacement;

    // Grid occupancy system
    private final int cols = World.WORLD_WIDTH / World.TILE_SIZE;
    private final int rows = World.WORLD_HEIGHT / World.TILE_SIZE;
    private final boolean[][] occupiedTiles = new boolean[cols][rows];
    private int tileX, tileY;
    private final int buildingTileSize = size / World.TILE_SIZE; // each building is 64x64, and each tile is 32x32, so it's a factor of 2

    public boolean checkValidPlacement() {
        // Check if player has selected a building
        if (selectedBuilding == null) return false;

        // Check if player has enough material
        if (Player.numStone < selectedBuilding.stoneCost || Player.numGold < selectedBuilding.goldCost) return false;

        // Check if over max # of buildings (i.e. goldstash = 1)
        if (countPlacedBuildings(selectedBuilding) >= selectedBuilding.maxPlacements) {
            return false;
        }

        // Check if placement is within world boundaries
        if (!(snappedX >= 0 && snappedX <= World.WORLD_WIDTH - size && snappedY >= 0 && snappedY <= World.WORLD_HEIGHT - size))
            return false;

        // Check if player is clicking on build HUD
        if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.MENU_RECT)) return false;

        // Check if placement overlaps with occupied tiles
        for (int x = tileX; x < tileX + buildingTileSize; x++) {
            for (int y = tileY; y < tileY + buildingTileSize; y++) {
                if (occupiedTiles[x][y]) {
                    return false;
                }
            }
        }

        // Check if placement overlaps with stone
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            if (CheckCollisionCircleRec(stoneCenter, ResourceNode.stoneRadius, previewRec)) {
                return false;
            }
        }

        // Check if placement overlaps with player
        if (CheckCollisionRecs(previewRec, Player.playerRec)) return false;

        // if all checks pass, return true
        return true;
    }

    private int countPlacedBuildings(BuildingType type) {
        int count = 0;

        for (Building building : EntityManager.placedBuildings) {
            if (building.type == type) {
                count++;
            }
        }
        return count;
    }

    public void keyBinds() {
        if (IsKeyPressed(KEY_ONE)) {
            selectedBuilding = buildingTypes[0];
            selectedIndex = 0;
        }
        if (IsKeyPressed(KEY_TWO)) {
            selectedBuilding = buildingTypes[1];
            selectedIndex = 1;
        }
        if (IsKeyPressed(KEY_THREE)) {
            selectedBuilding = buildingTypes[2];
            selectedIndex = 2;
        }
        if (IsKeyPressed(KEY_FOUR)) {
            selectedBuilding = buildingTypes[3];
            selectedIndex = 3;
        }
    }

    public void getClickedBuilding() {
        // Check if player uses the build HUD
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            for (int i = 0; i < BuildMenu.menuRects.length; i++) {
                if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.menuRects[i])) {
                    selectedBuilding = buildingTypes[i];
                    selectedIndex = i;
                }
            }
        }

        // Check if player uses any keybinds
        keyBinds();

        // Cancel placing if player clicks rmb
        if (IsMouseButtonPressed(MOUSE_BUTTON_RIGHT)) {
            selectedBuilding = null;
            selectedIndex = -1;
        }
    }

    public void occupyTiles() {
        // Update grid of occupied tiles after player places a new building
        for (int x = tileX; x < tileX + buildingTileSize; x++) {
            for (int y = tileY; y < tileY + buildingTileSize; y++) {
                occupiedTiles[x][y] = true;
            }
        }
    }

    public void getPreviewPosition() {
        // Get player mouse position
        Vector2 mouse = GetMousePosition();
        mouse = GetScreenToWorld2D(mouse, Camera.camera);

        // Snap mouse position
        snappedX = (int) Math.floor(mouse.x() / World.TILE_SIZE) * World.TILE_SIZE;
        snappedY = (int) Math.floor(mouse.y() / World.TILE_SIZE) * World.TILE_SIZE;

        // Update x & y position of current tile
        tileX = snappedX / World.TILE_SIZE;
        tileY = snappedY / World.TILE_SIZE;

        // Update preview rectangle
        previewRec = newRectangle(snappedX, snappedY, size, size);
    }

    private Building createBuilding(Vector2 position, BuildingType type) {
        if (type.name.equals("Gold Mine")) {
            return new GoldMine(position, type);
        }
        if (type.name.equals("Gold Stash")) {
            return new GoldStash(position, type);
        }
        if (type.name.equals("Cannon Tower")) {
            return new CannonTower(position, type);
        }

        return new Building(position, type);
    }

    public void update() {
        getClickedBuilding();

        getPreviewPosition();

        // Check if building can be placed
        validPlacement = checkValidPlacement();

        // Check if player fits all requirements to place a building
        if (selectedBuilding != null && IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && validPlacement) {
            // add building to arraylist
            EntityManager.placedBuildings.add(createBuilding(newVector2(snappedX, snappedY), selectedBuilding));
            occupyTiles();

            // update material
            Player.numStone -= selectedBuilding.stoneCost;
            Player.numGold -= selectedBuilding.goldCost;
        }
    }

    public void draw() {
        // Show red tint if not valid placement (e.x. overlapping with already placed building)
        // Otherwise, show default white tint
        Color previewColor = validPlacement
                ? newColor(255, 255, 255, 150)
                : newColor(255, 0, 0, 150);

        // Draw building preview
        if (selectedBuilding != null) {
            DrawTextureEx(
                    buildingIconTextures[selectedIndex],
                    newVector2(snappedX, snappedY),
                    0,
                    1.0f,
                    previewColor);
        }
    }
}
