package world;

import static com.raylib.Helpers.newVector2;
import static com.raylib.Helpers.newColor;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class World {
    public static final int worldWidth = 2048;
    public static final int worldHeight = 2048;
    public static final int tileSize = 32;

    private Texture background = LoadTexture("src/main/assets/images/bg1.png");

    public void drawBg() {
        DrawTextureEx(background, newVector2(0, 0), 0, (float) worldWidth / background.width(), WHITE);
    }

    public void drawGrid(Vector2 position) {
        float leftX = Math.max(0, position.x() - GetScreenWidth() / 2f);
        float rightX = Math.min(worldWidth, position.x() + GetScreenWidth() / 2);
        float topY = Math.max(0, position.y() - GetScreenHeight() / 2f);
        float bottomY = Math.min(worldHeight, position.y() + GetScreenHeight() / 2);

        float leftXAligned = (float) Math.floor(leftX / tileSize) * tileSize;
        float topYAligned = (float) Math.floor(topY / tileSize) * tileSize;

        for (float x = leftXAligned; x <= rightX; x += tileSize) {
            DrawLineEx(newVector2(x, topY), newVector2(x, bottomY), 1f, BLACK);
        }
        for (float y = topYAligned; y <= bottomY; y += tileSize) {
            DrawLineEx(newVector2(leftX, y), newVector2(rightX, y), 1f, BLACK);
        }
    }

    public void unload() {
        UnloadTexture(background);
    }
}
