package systems;

import buildings.*;
import core.Camera;
import core.EntityManager;
import core.TextureManager;
import entities.Player;
import ui.BuildMenu;
import world.*;

import static com.raylib.Colors.RED;
import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;
import static buildings.Building.size;

public class BuildSystem {
    public static Texture[] baseBuildingTextures = {
            TextureManager.getTexture("Gold Mine 1"),
            TextureManager.getTexture("Tower Base"),
            TextureManager.getTexture("Tower Base"),
            TextureManager.getTexture("Gold Stash 1")
    };

    public static Texture[] buildingIconTextures = {
            TextureManager.getTexture("Gold Mine 1"),
            TextureManager.getTexture("Cannon Tower Combined"),
            TextureManager.getTexture("Arrow Tower Combined"),
            TextureManager.getTexture("Gold Stash 1")
    };

    public static BuildingType[] buildingTypes = {
            new BuildingType("Gold Mine", baseBuildingTextures[0], 50, 0, 8, 200, 0),
            new BuildingType("Cannon Tower", baseBuildingTextures[1], 75, 40, 10, 150, 15),
            new BuildingType("Arrow Tower", baseBuildingTextures[2], 60, 30, 10, 100, 5),
            new BuildingType("Gold Stash", baseBuildingTextures[3], 0,0, 1, 500, 0)
    };

    private static final int GOLD_STASH_INDEX = 3;
    private static final float PLACEMENT_RADIUS = 750f;
    private boolean placedBuildingThisFrame = false;

    // Tracking selected building
    public BuildingType selectedBuilding = null;
    private int selectedIndex = -1;
    private int snappedX, snappedY;
    private Rectangle previewRec;
    private boolean validPlacement;

    // Grid occupancy system
    private final int COLS = World.WORLD_WIDTH / World.TILE_SIZE;
    private final int ROWS = World.WORLD_HEIGHT / World.TILE_SIZE;
    private final boolean[][] occupiedTiles = new boolean[COLS][ROWS];
    private int tileX, tileY;
    private final int BUILDING_GRID_TILE_SIZE = size / World.TILE_SIZE; // each building is 64x64, and each tile is 32x32, so it's a factor of 2

    public static boolean hasGoldStashPlaced() {
        for (Building building : EntityManager.placedBuildings) {
            if (building.type.name.equals("Gold Stash")) {
                return true;
            }
        }

        return false;
    }

    public static Building getGoldStash() {
        for (Building building : EntityManager.placedBuildings) {
            if (building.type.name.equals("Gold Stash")) {
                return building;
            }
        }

        return null;
    }

    public static boolean isIconDisabled(int index) {
        BuildingType type = buildingTypes[index];

        // Before gold stash is placed, disable very icon except gold stash itself
        if (!hasGoldStashPlaced() && index != GOLD_STASH_INDEX) {
            return true;
        }

        // Disable icon if this building already reached max placements
        if (countPlacedBuildings(type) >= type.maxPlacements) {
            return true;
        }

        return false;
    }

    public static int countPlacedBuildings(BuildingType type) {
        int count = 0;

        for (Building building : EntityManager.placedBuildings) {
            if (building.type == type) {
                count++;
            }
        }
        return count;
    }

    private boolean checkValidPlacement() {
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
        for (int x = tileX; x < tileX + BUILDING_GRID_TILE_SIZE; x++) {
            for (int y = tileY; y < tileY + BUILDING_GRID_TILE_SIZE; y++) {
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

        // Check if within predefined distance from goldstash
        if (!selectedBuilding.name.equals("Gold Stash")) {
            Building goldStash = getGoldStash();

            if (goldStash == null) {
                return false;
            }

            float previewCenterX = snappedX + size / 2f;
            float previewCenterY = snappedY + size / 2f;

            float stashCenterX = goldStash.position.x() + size / 2f;
            float stashCenterY = goldStash.position.y() + size / 2f;

            float distance = Vector2Distance(
                    newVector2(previewCenterX, previewCenterY),
                    newVector2(stashCenterX, stashCenterY)
            );

            if (distance > PLACEMENT_RADIUS) {
                return false;
            }
        }

        // if all checks pass, return true
        return true;
    }

    private void keyBinds() {
        if (IsKeyPressed(KEY_ONE) && !isIconDisabled(0)) {
            selectedBuilding = buildingTypes[0];
            selectedIndex = 0;
        }
        if (IsKeyPressed(KEY_TWO) && !isIconDisabled(1)) {
            selectedBuilding = buildingTypes[1];
            selectedIndex = 1;
        }
        if (IsKeyPressed(KEY_THREE) && !isIconDisabled(2)) {
            selectedBuilding = buildingTypes[2];
            selectedIndex = 2;
        }
        if (IsKeyPressed(KEY_FOUR) && !isIconDisabled(3)) {
            selectedBuilding = buildingTypes[3];
            selectedIndex = 3;
        }
    }

    private void getClickedBuilding() {
        // Check if player uses the build HUD
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            for (int i = 0; i < BuildMenu.menuRects.length; i++) {
                if (CheckCollisionPointRec(GetMousePosition(), BuildMenu.menuRects[i])) {

                    // If icon is disabled, do not select it
                    if (isIconDisabled(i)) {
                        selectedBuilding = null;
                        selectedIndex = -1;
                        return;
                    }

                    // Else, update variables
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

    private void occupyTiles() {
        // Update grid of occupied tiles after player places a new building
        for (int x = tileX; x < tileX + BUILDING_GRID_TILE_SIZE; x++) {
            for (int y = tileY; y < tileY + BUILDING_GRID_TILE_SIZE; y++) {
                occupiedTiles[x][y] = true;
            }
        }
    }

    public void freeTiles(Building building) {
        int buildingTileX = (int) building.position.x() / World.TILE_SIZE;
        int buildingTileY = (int) building.position.y() / World.TILE_SIZE;

        for (int x = buildingTileX; x < buildingTileX + BUILDING_GRID_TILE_SIZE; x++) {
            for (int y = buildingTileY; y < buildingTileY + BUILDING_GRID_TILE_SIZE; y++) {
                occupiedTiles[x][y] = false;
            }
        }
    }

    private void getPreviewPosition() {
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
        return switch (type.name) {
            case "Gold Mine" -> new GoldMine(position, type);
            case "Gold Stash" -> new GoldStash(position, type);
            case "Cannon Tower" -> new CannonTower(position, type);
            case "Arrow Tower" -> new ArrowTower(position, type);
            default -> new Building(position, type);
        };
    }

    public void update() {
        placedBuildingThisFrame = false;

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

            placedBuildingThisFrame = true;

            // If this building has now reached its max placements, stop showing the preview
            if (isIconDisabled(selectedIndex)) {
                selectedBuilding = null;
                selectedIndex = -1;
            }
        }
    }

    public void draw() {
        // For testing purposes, draw the placement radius
//        Building goldStash = getGoldStash();
//
//        if (goldStash != null) {
//            DrawCircleLines(
//                    (int) (goldStash.position.x() + size / 2f),
//                    (int) (goldStash.position.y() + size / 2f),
//                    PLACEMENT_RADIUS,
//                    RED
//            );
//        }

        if (selectedBuilding == null || selectedIndex == -1) {
            return;
        }

        // Show red tint if not valid placement. Otherwise, show default white tint
        Color previewColor = validPlacement
                ? newColor(255, 255, 255, 150)
                : newColor(255, 0, 0, 150);

        // Draw building preview
        DrawTextureEx(
                buildingIconTextures[selectedIndex],
                newVector2(snappedX, snappedY),
                0,
                1.0f,
                previewColor);

    }

    // Getters
    public boolean placedBuildingThisFrame() {
        return placedBuildingThisFrame;
    }

    public boolean isPlacingBuilding() {
        return selectedBuilding != null;
    }
}
