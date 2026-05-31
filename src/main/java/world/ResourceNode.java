package world;

import core.TextureManager;
import core.EntityManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

public class ResourceNode {
    // Radius of each stone node
    public static final float STONE_RADIUS = 48f;

    // Stone generation settings
    private static final int NUM_STONES = 30;
    private static final float PLAYER_SAFE_ZONE_RADIUS = 150f;

    public static void init() {
        Texture stoneTexture = TextureManager.getTexture("stone");

        float stoneWidth = stoneTexture.width();
        float stoneHeight = stoneTexture.height();

        // Player starts in the middle of the map
        Vector2 playerSpawnPos = newVector2(World.WORLD_WIDTH / 2.0f, World.WORLD_HEIGHT / 2.0f);

        // Keep generating random positions until this stone has a valid spawn point.
        for (int i = 0; i < NUM_STONES; i++) {
            boolean validPosition = false;
            while (!validPosition) {
                float x;
                float y;

                // Generate random center coordinate aligned with tiles
                x = (float) ((int) ((Math.random() * World.WORLD_WIDTH) / World.TILE_SIZE) * World.TILE_SIZE - 16);
                y = (float) ((int) ((Math.random() * World.WORLD_HEIGHT) / World.TILE_SIZE) * World.TILE_SIZE - 16);

                // Check map boundaries
                if (x < 0 || x > World.WORLD_WIDTH - stoneWidth || y < 0 || y > World.WORLD_HEIGHT - stoneHeight) {
                    continue;
                }

                Vector2 pos = newVector2(x, y);

                // Prevent stones from spawning too close to the player's starting position.
                if (Vector2Distance(pos, playerSpawnPos) > PLAYER_SAFE_ZONE_RADIUS) {
                    EntityManager.stoneCenters.add(pos);
                    validPosition = true;
                }
            }
        }
    }

    public static void reset() {
        // EntityManager.reset() should clear stoneCenters before this is called.
        // Then this method regenerates a new set of stones.
        init();
    }
}
