package world;

import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class World {
    public static final int worldWidth = 2048;
    public static final int worldHeight = 2048;
    public static final int tileSize = 32;

    // Generate random stone positions
    int numStone = 10;
    Vector2[] stonePosition = new Vector2[numStone];


    final private Texture background = LoadTexture("src/main/assets/images/bg.png");
    final private Texture stone = LoadTexture("src/main/assets/images/stone.png");

    public World() {
        float stoneScale = 2.5f;
        float stoneWidth = stone.width() * stoneScale;
        float stoneHeight = stone.height() * stoneScale;

        Vector2 playerPos = newVector2(worldWidth / 2.0f, worldHeight / 2.0f);
        float safeZoneRadius = 150.0f;

        for (int i = 0; i < numStone; i++) {
            boolean validPosition = false;
            while (!validPosition) {
                float x, y;

                // Chance that it generates next to the previous one
                if (i > 0 && Math.random() < 0.2) {
                    // generate coordinates
                    int dir = (int) (Math.random() * 4);
                    x = stonePosition[i - 1].x();
                    y = stonePosition[i - 1].y();
                    switch (dir) {
                        case 0: x += tileSize; break; // Right
                        case 1: x -= tileSize; break; // Left
                        case 2: y += tileSize; break; // Down
                        case 3: y -= tileSize; break; // Up
                    }
                } else {
                    x = (float) (Math.random() * (worldWidth - stoneWidth));
                    y = (float) (Math.random() * (worldHeight - stoneHeight));
                }

                // Check map boundaries
                if (x < 0 || x > worldWidth - stoneWidth || y < 0 || y > worldHeight - stoneHeight) {
                    continue;
                }

                // Check distance from player
                Vector2 pos = newVector2(x, y);
                if (Vector2Distance(pos, playerPos) > safeZoneRadius) {
                    stonePosition[i] = pos;
                    validPosition = true;
                }
            }
        }
    }

    public void drawBg() {
        DrawTextureEx(background, newVector2(0, 0), 0, (float) worldWidth / background.width(), WHITE);
    }

    public void drawGrid(Vector2 position) {
        float leftX = Math.max(0, position.x() - GetScreenWidth() / 2f);
        float rightX = Math.min(worldWidth, position.x() + GetScreenWidth() / 2f);
        float topY = Math.max(0, position.y() - GetScreenHeight() / 2f);
        float bottomY = Math.min(worldHeight, position.y() + GetScreenHeight() / 2f);

        float leftXAligned = (float) Math.floor(leftX / tileSize) * tileSize;
        float topYAligned = (float) Math.floor(topY / tileSize) * tileSize;

        for (float x = leftXAligned; x <= rightX; x += tileSize) {
            DrawLineEx(newVector2(x, topY), newVector2(x, bottomY), 1f, BLACK);
        }
        for (float y = topYAligned; y <= bottomY; y += tileSize) {
            DrawLineEx(newVector2(leftX, y), newVector2(rightX, y), 1f, BLACK);
        }
    }

    public void drawStone() {
        for (Vector2 position : stonePosition) {
            DrawTextureEx(stone, position, 0, 2.5f, WHITE);
        }

    }
    public void unload() {
        UnloadTexture(background);
        UnloadTexture(stone);
    }
}
