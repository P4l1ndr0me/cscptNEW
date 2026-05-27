package ui;

import buildings.Building;
import buildings.BuildingType;
import core.Main;
import systems.BuildSystem;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;
import static core.Main.pixelFont;
import static systems.BuildSystem.*;

public class BuildMenu {
    // Build menu data
    public static final Color MENU_FILL = newColor(80, 80, 80, 255);
    private static final float MENU_X = 0.3f * Main.SCREEN_WIDTH;
    private static final int MENU_HEIGHT = 60;
    private static final float MENU_Y = Main.SCREEN_HEIGHT - MENU_HEIGHT - 20;
    public static final Rectangle MENU_RECT = newRectangle(MENU_X, MENU_Y, 0.4f * Main.SCREEN_WIDTH, MENU_HEIGHT);
    private static final int HUD_BUILDING_SIZE = 32;
    private static final float HUD_SCALE = (float) HUD_BUILDING_SIZE / Building.size; // 0.5

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
                    HUD_BUILDING_SIZE,
                    HUD_BUILDING_SIZE);
        }
    }

    public void draw() {
        // Draw outline and fill of build menu
        DrawRectangleRoundedLinesEx(MENU_RECT, 0.6f, 0, 2.0f, BLACK);
        DrawRectangleRounded(MENU_RECT, 0.6f, 0, MENU_FILL);

        // Draw building icons inside the build menu
        for (int i = 0; i < menuRects.length; i++) {
            Color iconColor = BuildSystem.isIconDisabled(i)
                    ? MENU_FILL
                    : WHITE;

            DrawTextureEx(buildingIconTextures[i], newVector2(menuRects[i].x(), menuRects[i].y()), 0.0f, HUD_SCALE, iconColor);
        }

        // Draw tooltip if mouse is hovering over an icon
        for (int i = 0; i < menuRects.length; i++) {
            if (CheckCollisionPointRec(GetMousePosition(), menuRects[i])) {
                drawBuildingTooltip(i);
                break;
            }
        }
    }

    private void drawBuildingTooltip(int index) {
        BuildingType type = buildingTypes[index];

        float tooltipWidth = 240;
        float tooltipHeight = 80;

        float tooltipX = menuRects[index].x() - (tooltipWidth - HUD_BUILDING_SIZE) / 2;
        float tooltipY = menuRects[index].y() - tooltipHeight - 25;


        Rectangle tooltipRect = newRectangle(
                tooltipX,
                tooltipY,
                tooltipWidth,
                tooltipHeight
        );

        // Background
        DrawRectangleRounded(
                tooltipRect,
                0.15f,
                0,
                MENU_FILL
        );

        // Border
        DrawRectangleRoundedLinesEx(
                tooltipRect,
                0.15f,
                0,
                2.0f,
                BLACK
        );

        int textX = (int) tooltipX + 10;
        int textY = (int) tooltipY + 10;

        DrawTextEx(
                pixelFont,
                type.name,
                newVector2(textX, textY)
                ,24,
                1.0f,
                WHITE);

        DrawTextEx(
                pixelFont,
                type.stoneCost + " stone, " + type.goldCost + " gold",
                newVector2(textX, tooltipY + tooltipHeight - 30),
                18,
                1.0f,
                WHITE
        );

        String text = countPlacedBuildings(type) + " / " + type.maxPlacements;
        Vector2 textSize = MeasureTextEx(pixelFont, text, 24, 1.0f);
        DrawTextEx(
                pixelFont,
                text,
                newVector2(tooltipX + tooltipWidth - textSize.x() - 10, textY),
                24,
                1.0f,
                WHITE
        );
    }
}
