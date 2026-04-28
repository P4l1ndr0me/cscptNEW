package core;

import static com.raylib.Raylib.*;

public class Main {
    // default screen dimensions
    public static final int screen_width = 1920;
    public static final int screen_height = 1080;

    public static void main(String[] args) {

        // initialize and maximize window
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow(0, 0, "zombs.io");
        MaximizeWindow();
        ClearWindowState(FLAG_WINDOW_RESIZABLE);

        // set fps
        SetTargetFPS(60);

        // Create new instance
        GameState gameState = new GameState();

        while (!WindowShouldClose()) {

            // update
            gameState.update();

            // draw
            BeginDrawing();
            gameState.draw();
            EndDrawing();
        }

        // unload textures
        TextureManager.unloadAll();
        CloseWindow();
    }
}