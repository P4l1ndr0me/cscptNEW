package world;

import core.TextureManager;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class World {
    // World dimensions
    public static final int WORLD_WIDTH = 4096;
    public static final int WORLD_HEIGHT = 4096;

    // Size of each grid tile in pixels
    public static final int TILE_SIZE = 32;

    // Background texture for the map
    private static final Texture BACKGROUND = TextureManager.getTexture("background");

    // Scale the background so it matches the full world width
    private static final float SCALE = (float) WORLD_WIDTH / BACKGROUND.width();

    public static void draw() {
        // Draw the map background first so everything else appears on top
        DrawTextureEx(
                BACKGROUND,
                newVector2(0, 0),
                0,
                SCALE,
                WHITE
        );

        // Draw vertical grid lines
        for (int x = 0; x <= WORLD_WIDTH; x += TILE_SIZE) {
            DrawLine(
                    x,
                    0,
                    x,
                    WORLD_HEIGHT,
                    BLACK
            );
        }

        // Draw horizontal grid lines
        for (int y = 0; y <= WORLD_HEIGHT; y += TILE_SIZE) {
            DrawLine(
                    0,
                    y,
                    WORLD_WIDTH,
                    y,
                    BLACK
            );
        }
    }
}
