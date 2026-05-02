package world;

import core.TextureManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;
import static core.EntityManager.stonePosition;

public class World {
    // constants
    public static final int worldWidth = 2048;
    public static final int worldHeight = 2048;
    public static final int tileSize = 32;

    // Textures
    final private Texture background = TextureManager.getTexture("background");
    final private Texture stone = TextureManager.getTexture("stone");

    public World() {
        // Generate random stone around map
        float stoneScale = 1.5f;
        float stoneWidth = stone.width() * stoneScale;
        float stoneHeight = stone.height() * stoneScale;

        Vector2 playerPos = newVector2(worldWidth / 2.0f, worldHeight / 2.0f);
        float safeZoneRadius = 150.0f;

        // Generate random stone positions
        int numStone = 12;
        for (int i = 0; i < numStone; i++) {
            boolean validPosition = false;
            while (!validPosition) {
                float x, y;

                // Chance that it generates next to the previous one (5%)
                if (i > 0 && Math.random() < 0.05) {
                    // generate coordinates
                    int dir = (int) (Math.random() * 4);
                    x = stonePosition.get(i - 1).x();
                    y = stonePosition.get(i - 1).y();
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
                } else { // Generate random coordinate aligned with tiles
                    x = (float) ((int) ((Math.random() * worldWidth) / tileSize) * tileSize);
                    y = (float) (((int) (Math.random() * (worldHeight - stoneHeight)) / tileSize) * tileSize);
                }

                // Check map boundaries
                if (x < 0 || x > worldWidth - stoneWidth || y < 0 || y > worldHeight - stoneHeight) {
                    continue;
                }

                // Check distance from player
                Vector2 pos = newVector2(x, y);
                if (Vector2Distance(pos, playerPos) > safeZoneRadius) {
                    stonePosition.add(pos);
                    validPosition = true;
                }
            }
        }
    }

    public static void drawStone(Vector2 position) {
        DrawTextureEx(TextureManager.getTexture("stone"), position, 1.0f, 1.5f, WHITE);
    }

    public void draw() {
        // Draw background
        DrawTextureEx(background, newVector2(0, 0), 0, (float) worldWidth / background.width(), WHITE);

        // Draw grid lines
        for (int x = 0; x <= worldWidth; x += tileSize) {
            DrawLine(x, 0, x, worldHeight, BLACK);
            //DrawLineV(newVector2(x, 0), newVector2(x, worldHeight), BLACK);
        }
        for (int y = 0; y <= worldHeight; y += tileSize) {
            DrawLine(0, y, worldWidth, y, BLACK);
            //DrawLineV(newVector2(0, y), newVector2(worldWidth, y), BLACK);
        }
    }
}
