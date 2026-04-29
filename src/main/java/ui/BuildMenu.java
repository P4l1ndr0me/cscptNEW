package ui;

import core.Main;
import core.TextureManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class BuildMenu {

    // Build menu data
    private final Color menuFill = newColor(203, 203, 203, 100);
    private static final int menuX = 500;
    private static final int menuY = Main.screen_height - 70;
    private static final int menuHeight = 50;
    public static final Rectangle menuRect = newRectangle(menuX, menuY, GetScreenWidth() - 2 * menuX, menuHeight);

    public static Texture[] buildingTextures = {
            TextureManager.getTexture("building1"),
            TextureManager.getTexture("building2"),
            TextureManager.getTexture("building3")};
    public static Rectangle[] buildingPositions = new Rectangle[3];

    private final float scale = 0.5f;

    public BuildMenu() {
        // xy position of first building
        int topLeftX = menuX + 9;
        int topLeftY = (int) (menuY + (menuHeight - buildingTextures[0].height() * scale) / 2f);

        // set each building's length, width, and XY position in the HUD
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
}
