package world;

import core.TextureManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class World {
    // constants
    public static final int WORLD_WIDTH = 2048;
    public static final int WORLD_HEIGHT = 2048;
    public static final int TILE_SIZE = 32;

    // Textures
    private static final Texture BACKGROUND = TextureManager.getTexture("background");
    private static final float SCALE = (float) WORLD_WIDTH / BACKGROUND.width();

    public static void draw() {
        // Draw background
        DrawTextureEx(BACKGROUND, newVector2(0, 0), 0, SCALE, WHITE);

        // Draw grid lines
        for (int x = 0; x <= WORLD_WIDTH; x += TILE_SIZE) {
            DrawLine(x, 0, x, WORLD_HEIGHT, BLACK);
//            DrawLineV(newVector2(x, 0), newVector2(x, worldHeight), BLACK);
        }
        for (int y = 0; y <= WORLD_HEIGHT; y += TILE_SIZE) {
            DrawLine(0, y, WORLD_WIDTH, y, BLACK);
//            DrawLineV(newVector2(0, y), newVector2(worldWidth, y), BLACK);
        }
    }
}
