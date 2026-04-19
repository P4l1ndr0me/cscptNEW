package world;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class World {
    public static final int worldWidth = 1000;
    public static final int worldHeight = 1000;
    public static final int tileSize = 50;

    public void drawGrid() {
        for (int x = 0; x <= worldWidth; x += tileSize) {
            DrawLine(x, 0, x, worldHeight, LIGHTGRAY);
        }
        for (int y = 0; y <= worldHeight; y += tileSize) {
            DrawLine(0, y, worldWidth, y, LIGHTGRAY);
        }
    }


}
