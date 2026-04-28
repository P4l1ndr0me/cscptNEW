package world;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class World {
    public static final int worldWidth = 2048;
    public static final int worldHeight = 2048;
    public static final int tileSize = 32;

    // Generate random stone positions
    int numStone = 12;
    Vector2[] stonePosition = new Vector2[numStone];

    final private Texture background = LoadTexture("src/main/assets/images/bgNEW.png");
    final private Texture stone = LoadTexture("src/main/assets/images/stone.png");

    public World() {
        float stoneScale = 2.5f;
        float stoneWidth = stone.width() * stoneScale;
        float stoneHeight = stone.height() * stoneScale;

        Vector2 playerPos = newVector2(worldWidth / 2.0f, worldHeight / 2.0f);
        float safeZoneRadius = 150.0f;

        for (int i = 0; i < numStone; i++) {
            boolean validPosition = false;
            while (!validPosition) {
                float x, y;

                // Chance that it generates next to the previous one
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
                } else {
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

    public void drawBg() {
        DrawTextureEx(background, newVector2(0, 0), 0, (float) worldWidth / background.width(), WHITE);
    }

    public void drawGrid() {
//        Vector2 topLeft = GetScreenToWorld2D(newVector2(0, 0), Camera.camera);
//        Vector2 bottomRight = GetScreenToWorld2D(newVector2(GetScreenWidth(), GetScreenHeight()), Camera.camera);
//
//        float left = Math.max(0, topLeft.x());
//        float right = Math.min(worldWidth, bottomRight.x());
//        float top = Math.max(0, topLeft.y());
//        float bottom = Math.min(worldHeight, bottomRight.y());
//
//        int leftXAligned = (int) Math.floor(left / tileSize) * tileSize;
//        int rightXAligned = (int) Math.ceil(right / tileSize) * tileSize;
//        int topYAligned = (int) Math.floor(top / tileSize) * tileSize;
//        int bottomYAligned = (int) Math.ceil(bottom / tileSize) * tileSize;
//
//        //float thickness = 1.0f / Camera.camera.zoom();
//
//        for (int x = leftXAligned; x <= rightXAligned; x += tileSize) {
//            //DrawLineEx(newVector2(x, topYAligned), newVector2(x, bottomYAligned), thickness, BLACK);
//            DrawLineV(newVector2(x, topYAligned), newVector2(x, bottomYAligned), BLACK);
//        }
//        for (int y = topYAligned; y <= bottomYAligned; y += tileSize) {
//            //DrawLineEx(newVector2(leftXAligned, y), newVector2(rightXAligned, y), thickness, BLACK);
//            DrawLineV(newVector2(leftXAligned, y), newVector2(rightXAligned, y), BLACK);
//        }

        for (int x = 0; x <= worldWidth; x += tileSize) {
            DrawLine(x, 0, x, worldHeight, BLACK);
        }
        for (int y = 0; y <= worldHeight; y += tileSize) {
            DrawLine(0, y, worldWidth, y, BLACK);
        }
    }

    public void drawStone() {
        for (Vector2 position : stonePosition) {
            DrawTextureEx(stone, position, 0, 2.5f, WHITE);
        }

    }

    public void unload() {
        UnloadTexture(background);
        UnloadTexture(stone);
    }
}
