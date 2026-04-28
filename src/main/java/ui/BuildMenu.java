package ui;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class BuildMenu {

    // Build menu data
    Color menuFill = newColor(203, 203, 203, 100);
    int menuX = 500;
    int menuY = GetScreenHeight() - 70;
    int menuHeight = 50;


    public static final Texture building1 = LoadTexture("src/main/assets/images/buildings/building1.png");
    public static final Texture building2 = LoadTexture("src/main/assets/images/buildings/building2.png");
    public static final Texture building3 = LoadTexture("src/main/assets/images/buildings/building3.png");

    float scale = 0.5f;

    // xy position of first building
    int topLeftX = menuX + 9;
    int topLeftY = (int) (menuY + (menuHeight - building1.height() * scale) / 2f);

    Texture[] buildingTextures = {building1, building2, building3};
    Rectangle[] buildingPositions = {new Rectangle(), new Rectangle(), new Rectangle()};

    public BuildMenu() {
        for (int i = 0; i < buildingPositions.length; i++) {
            buildingPositions[i].x(topLeftX + 60 * i);
            buildingPositions[i].y(topLeftY);
            buildingPositions[i].width(buildingTextures[i].width() * scale);
            buildingPositions[i].height(buildingTextures[i].height() * scale);
        }
    }

    public void draw() {
        // Draw outline and fill of build menu
        DrawRectangleRoundedLinesEx(newRectangle(menuX, menuY, GetScreenWidth() - 2 * menuX, menuHeight), 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(newRectangle(menuX, menuY, GetScreenWidth() - 2 * menuX, menuHeight), 0.6f, 0, menuFill);

        // Draw building
        for (int i = 0; i < buildingPositions.length; i++) {
            DrawTextureEx(buildingTextures[i], newVector2(buildingPositions[i].x(), buildingPositions[i].y()), 0.0f, scale, WHITE);
        }
    }

    public int getClickedBuilding(Vector2 mouse) {
        if (IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {

            for (int i = 0; i < buildingPositions.length; i++) {
                if (CheckCollisionPointRec(mouse, buildingPositions[i])) {
                    return i; // which building was clicked
                }
            }
        }
        return -1; // if none were clicked
    }

    public void unload() {
        UnloadTexture(building1);
        UnloadTexture(building2);
        UnloadTexture(building3);
    }
}
