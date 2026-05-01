package core;

import static com.raylib.Raylib.*;

public class Main {
    // render texture dimensions
    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    public static void main(String[] args) {

        // initialize and maximize window
        InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "zombs.io");

        // set fps
        SetTargetFPS(60);

        // Create new instance
        GameState gameState = new GameState();

        while (!WindowShouldClose()) {

            // update
            gameState.update();

            // draw
            gameState.draw();
        }

        // unload textures
        TextureManager.unloadAll();
        CloseWindow();
    }
}