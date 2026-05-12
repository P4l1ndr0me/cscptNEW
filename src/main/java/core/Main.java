package core;

import static com.raylib.Raylib.*;

public class Main {
    // Screen dimensions
    public static final int SCREEN_WIDTH = 1600;
    public static final int SCREEN_HEIGHT = 900;

    public static void main(String[] args) {

        // Initialize window
        SetWindowState(FLAG_WINDOW_UNDECORATED);
        InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "zombs.io");

        // Set target fps
        SetTargetFPS(60);

        // Create new instance
        GameState gameState = new GameState();

        while (!WindowShouldClose()) {

            // Update
            gameState.update();

            // Draw
            gameState.draw();
        }

        // Unload textures
        TextureManager.unloadAll();
        CloseWindow();
    }
}