package world;

import com.raylib.Raylib;
import core.TextureManager;
import core.EntityManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

public class ResourceNode {
    public static float stoneRadius = 48f;

    public static void init() {
        // Generate random stone around map
        Texture stone = TextureManager.getTexture("stone");

        float stoneWidth = stone.width();
        float stoneHeight = stone.height();

        Vector2 playerPos = newVector2(World.worldWidth / 2.0f, World.worldHeight / 2.0f);

        // Generate random stone positions
        int numStone = 12;
        for (int i = 0; i < numStone; i++) {
            boolean validPosition = false;
            while (!validPosition) {
                float x, y;

                // Generate random center coordinate aligned with tiles
                x = (float) ((int) ((Math.random() * World.worldWidth) / World.tileSize) * World.tileSize - 16);
                y = (float) (((int) (Math.random() * (World.worldHeight - stoneHeight)) / World.tileSize) * World.tileSize - 16);

                // Check map boundaries
                if (x < 0 || x > World.worldWidth - stoneWidth || y < 0 || y > World.worldHeight - stoneHeight) {
                    continue;
                }

                // Check distance from player
                Raylib.Vector2 pos = newVector2(x, y);
                float safeZoneRadius = 150.0f;
                if (Vector2Distance(pos, playerPos) > safeZoneRadius) {
                    EntityManager.stoneCenters.add(pos);
                    validPosition = true;
                }
            }
        }
    }
}
