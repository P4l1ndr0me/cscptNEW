package ui;

import buildings.Building;
import core.Main;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;
import static systems.BuildSystem.buildingTextures;

public class BuildMenu {
    // Build menu data
    public static final Color menuFill = newColor(203, 203, 203, 100);
    private static final float menuX = 0.3f * Main.SCREEN_WIDTH;
    private static final int menuHeight = 60;
    private static final float menuY = Main.SCREEN_HEIGHT - menuHeight - 20;
    public static final Rectangle menuRect = newRectangle(menuX, menuY, 0.4f * Main.SCREEN_WIDTH, menuHeight);
    private static final int HUDBuildingSize = 32;
    private static final float HUDScale = (float) HUDBuildingSize / Building.size;

    public static Rectangle[] menuRects = new Rectangle[4];

    public BuildMenu() {
        // xy position of first building
        float topLeftX = menuX + 20;
        float topLeftY = menuY + (menuHeight - HUDBuildingSize) / 2f;

        // set each building's length, width, and XY position in the HUD
        for (int i = 0; i < menuRects.length; i++) {
            menuRects[i] = newRectangle(
                    topLeftX + 60 * i, // hardcoded for now, will need to change later
                    topLeftY,
                    buildingTextures[i].width() * HUDScale,
                    buildingTextures[i].height() * HUDScale);
        }
    }

    public void drawUI() {
        // Draw outline and fill of build menu
        DrawRectangleRoundedLinesEx(menuRect, 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(menuRect, 0.6f, 0, menuFill);

        // Draw building
        for (int i = 0; i < menuRects.length; i++) {
            DrawTextureEx(buildingTextures[i], newVector2(menuRects[i].x(), menuRects[i].y()), 0.0f, HUDScale, WHITE);
//            DrawText();
        }
    }
}
