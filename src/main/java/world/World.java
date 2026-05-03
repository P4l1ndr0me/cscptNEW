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

    // Textures
    final private Texture background = TextureManager.getTexture("background");

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
