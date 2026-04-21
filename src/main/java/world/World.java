package world;

import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class World {
    public static final int worldWidth = 2048;
    public static final int worldHeight = 2048;
    public static final int tileSize = 25;

    public static Texture background = LoadTexture("src/main/assets/images/background.png");

    public void drawWorld() {
        DrawTextureEx(background, newVector2(0, 0), 0, (float) worldWidth /background.width(), WHITE);
    }

    public void unload() {
        UnloadTexture(background);
    }
}
