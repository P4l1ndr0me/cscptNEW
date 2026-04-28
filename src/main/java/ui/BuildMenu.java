package ui;

import buildings.Building;
import core.Camera;
import core.EntityManager;
import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class BuildMenu {

    // Build menu data
    private final Color menuFill = newColor(203, 203, 203, 100);
    private final int menuX = 500;
    private final int menuY = GetScreenHeight() - 70;
    private final int menuHeight = 50;
    private final Rectangle menuRect = newRectangle(menuX, menuY, GetScreenWidth() - 2 * menuX, menuHeight);

    // Load textures (in future will make texture manager)
    public static final Texture building1 = LoadTexture("src/main/assets/images/buildings/building1.png");
    public static final Texture building2 = LoadTexture("src/main/assets/images/buildings/building2.png");
    public static final Texture building3 = LoadTexture("src/main/assets/images/buildings/building3.png");

    public static Texture[] buildingTextures = {building1, building2, building3};
    public static Rectangle[] buildingPositions = new Rectangle[3];

    private final float scale = 0.5f;

    // Tracking selected building
    private int selectedBuilding = -1;
    private boolean isPlacing = false;
    private float snappedX, snappedY;

    public BuildMenu() {
        // xy position of first building
        int topLeftX = menuX + 9;
        int topLeftY = (int) (menuY + (menuHeight - building1.height() * scale) / 2f);

        // set building xy and lw
        for (int i = 0; i < buildingPositions.length; i++) {
            buildingPositions[i] = newRectangle(
                    topLeftX + 60 * i,
                    topLeftY,
                    buildingTextures[i].width() * scale,
                    buildingTextures[i].height() * scale);
        }
    }

    public void drawUI() {
        // Draw outline and fill of build menu
        DrawRectangleRoundedLinesEx(menuRect, 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(menuRect, 0.6f, 0, menuFill);

        // Draw building
        for (int i = 0; i < buildingPositions.length; i++) {
            DrawTextureEx(buildingTextures[i], newVector2(buildingPositions[i].x(), buildingPositions[i].y()), 0.0f, scale, WHITE);
        }
    }

    public void drawPreview() {
        // Draw preview
        if (isPlacing && selectedBuilding != -1) {
            DrawTextureEx(
                    buildingTextures[selectedBuilding],
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
        if (isPlacing && IsMouseButtonPressed(MOUSE_BUTTON_LEFT) && !CheckCollisionPointRec(GetMousePosition(), menuRect)) {
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
            for (int i = 0; i < buildingPositions.length; i++) {
                if (CheckCollisionPointRec(GetMousePosition(), buildingPositions[i])) {
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

    public void unload() {
        UnloadTexture(building1);
        UnloadTexture(building2);
        UnloadTexture(building3);
    }
}
