package core;

import static com.raylib.Raylib.*;

public class Main {
    // default screen dimensions
    public static final int screen_width = 1600;
    public static final int screen_height = 900;

    public static void main(String[] args) {

        // initialize and maximize window
        SetConfigFlags(FLAG_WINDOW_RESIZABLE);
        InitWindow(screen_width, screen_height, "zombs.io");
        //MaximizeWindow();
        ClearWindowState(FLAG_WINDOW_RESIZABLE);

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