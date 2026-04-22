import core.GameState;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {
    public static void main(String[] args) {
        // default screen dimensions
        int screen_width = 1280;
        int screen_height = 720;

        // initialize window and set fps
        InitWindow(screen_width, screen_height, "zombs.io");
        SetTargetFPS(60);

        // Create new instance
        GameState gameState = new GameState();

        while(!WindowShouldClose()) {

            // update
            gameState.update();

            // draw
            BeginDrawing();
                ClearBackground(GRAY);
                gameState.draw();
            EndDrawing();
        }
        gameState.unload();
        CloseWindow();
    }
}