package world;

import com.raylib.Raylib;
import core.TextureManager;
import core.EntityManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

public class ResourceNode {
    public static float stoneScale = 1.5f;

    public ResourceNode() {
        // Generate random stone around map
        Texture stone = TextureManager.getTexture("stone");

        float stoneWidth = stone.width() * stoneScale;
        float stoneHeight = stone.height() * stoneScale;

        Vector2 playerPos = newVector2(World.worldWidth / 2.0f, World.worldHeight / 2.0f);

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
                    x = EntityManager.stoneRects.get(i - 1).x();
                    y = EntityManager.stoneRects.get(i - 1).y();
                    switch (dir) {
                        case 0:
                            x += World.tileSize;
                            break; // Right
                        case 1:
                            x -= World.tileSize;
                            break; // Left
                        case 2:
                            y += World.tileSize;
                            break; // Down
                        case 3:
                            y -= World.tileSize;
                            break; // Up
                    }
                }
                else { // Generate random coordinate aligned with tiles
                    x = (float) ((int) ((Math.random() * World.worldWidth) / World.tileSize) * World.tileSize);
                    y = (float) (((int) (Math.random() * (World.worldHeight - stoneHeight)) / World.tileSize) * World.tileSize);
                }

                // Check map boundaries
                if (x < 0 || x > World.worldWidth - stoneWidth || y < 0 || y > World.worldHeight - stoneHeight) {
                    continue;
                }

                // Check distance from player
                Raylib.Vector2 pos = newVector2(x, y);
                float safeZoneRadius = 150.0f;
                if (Vector2Distance(pos, playerPos) > safeZoneRadius) {
                    EntityManager.stoneRects.add(newRectangle(pos.x(), pos.y(), stoneWidth, stoneHeight));
                    validPosition = true;
                }
            }
        }
    }
}
