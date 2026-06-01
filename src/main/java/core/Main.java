package core;

import ui.HUD;

import static com.raylib.Raylib.*;

public class Main {
    // Screen dimensions
    public static final int SCREEN_WIDTH = 1280;
    public static final int SCREEN_HEIGHT = 720;

    public static Font pixelFont;

    public static void main(String[] args) {

        // Initialize window
        InitWindow(SCREEN_WIDTH, SCREEN_HEIGHT, "zombs.io");

        // Set window icon
//        Image icon = LoadImage("src/main/assets/images/icon.png");
//        SetWindowIcon(icon);
//        UnloadImage(icon);

        // Set target FPS
        SetTargetFPS(60);

        // Load font
        pixelFont = LoadFont("src/main/assets/fonts/pixel.ttf");

        // Create new instance
        GameState gameState = new GameState();
        HUD.setGameState(gameState);

        while (!WindowShouldClose()) {

            // Update
            gameState.update();

            // Draw
            gameState.draw();
        }

        // Unload textures & fonts
        TextureManager.unloadAll();
        UnloadFont(pixelFont);
        CloseWindow();
    }
}