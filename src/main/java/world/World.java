package world;

import core.TextureManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class World {
    // constants
    public static final int worldWidth = 2048;
    public static final int worldHeight = 2048;
    public static final int tileSize = 32;

    // Generate random stone positions
    int numStone = 12;
    Vector2[] stonePosition = new Vector2[numStone];

    // Textures
    final private Texture background = TextureManager.getTexture("background");
    final private Texture stone = TextureManager.getTexture("stone");

    public World() {
        // Generate random stone around map
        float stoneScale = 2.5f;
        float stoneWidth = stone.width() * stoneScale;
        float stoneHeight = stone.height() * stoneScale;

        Vector2 playerPos = newVector2(worldWidth / 2.0f, worldHeight / 2.0f);
        float safeZoneRadius = 150.0f;

        for (int i = 0; i < numStone; i++) {
            boolean validPosition = false;
            while (!validPosition) {
                float x, y;

                // Chance that it generates next to the previous one (5%)
                if (Math.random() < 0.05) {
                    // generate coordinates
                    int dir = (int) (Math.random() * 4);
                    x = stonePosition[i - 1].x();
                    y = stonePosition[i - 1].y();
                    switch (dir) {
                        case 0:
                            x += tileSize;
                            break; // Right
                        case 1:
                            x -= tileSize;
                            break; // Left
                        case 2:
                            y += tileSize;
                            break; // Down
                        case 3:
                            y -= tileSize;
                            break; // Up
                    }
                } else { // Generate random coordinate
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

    public void draw() {
        // Draw background
        DrawTextureEx(background, newVector2(0, 0), 0, (float) worldWidth / background.width(), WHITE);

        // Draw grid
        for (int x = 0; x <= worldWidth; x += tileSize) {
//            DrawLine(x, 0, x, worldHeight, BLACK);
            DrawLineV(newVector2(x, 0), newVector2(x, worldHeight), BLACK);
        }
        for (int y = 0; y <= worldHeight; y += tileSize) {
//            DrawLine(0, y, worldWidth, y, BLACK);
            DrawLineV(newVector2(0, y), newVector2(worldWidth, y), BLACK);

        }
    }

    public void drawStone() {
        for (Vector2 position : stonePosition) {
            DrawTextureEx(stone, newVector2(Math.round(position.x()), Math.round(position.y())), 0, 2.5f, WHITE);
        }

    }


}
