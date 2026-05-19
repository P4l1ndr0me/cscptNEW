package ui;

import buildings.Building;
import core.Main;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;
import static systems.BuildSystem.buildingTextures;

public class BuildMenu {
    // Build menu data
    public static final Color MENU_FILL = newColor(203, 203, 203, 100);
    private static final float MENU_X = 0.3f * Main.SCREEN_WIDTH;
    private static final int MENU_HEIGHT = 60;
    private static final float MENU_Y = Main.SCREEN_HEIGHT - MENU_HEIGHT - 20;
    public static final Rectangle MENU_RECT = newRectangle(MENU_X, MENU_Y, 0.4f * Main.SCREEN_WIDTH, MENU_HEIGHT);
    private static final int HUD_BUILDING_SIZE = 32;
    private static final float HUD_SCALE = (float) HUD_BUILDING_SIZE / Building.size;

    // Stores the clickable rectangle for each building icon in the build menu
    public static Rectangle[] menuRects = new Rectangle[4];

    public BuildMenu() {
        // xy position of first building
        float topLeftX = MENU_X + 20;
        float topLeftY = MENU_Y + (MENU_HEIGHT - HUD_BUILDING_SIZE) / 2f;

        // Set each building's length, width, and XY position in the HUD
        for (int i = 0; i < menuRects.length; i++) {
            menuRects[i] = newRectangle(
                    topLeftX + 60 * i, // hardcoded for now, will need to change later
                    topLeftY,
                    buildingTextures[i].width() * HUD_SCALE,
                    buildingTextures[i].height() * HUD_SCALE);
        }
    }

    public void drawUI() {
        // Draw outline and fill of build menu
        DrawRectangleRoundedLinesEx(MENU_RECT, 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(MENU_RECT, 0.6f, 0, MENU_FILL);

        // Draw building icons inside the build menu
        for (int i = 0; i < menuRects.length; i++) {
            DrawTextureEx(buildingTextures[i], newVector2(menuRects[i].x(), menuRects[i].y()), 0.0f, HUD_SCALE, WHITE);
        }
    }
}
