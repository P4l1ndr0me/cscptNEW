import core.GameState;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;

public class Main {
    public static void main(String[] args) {
        int screen_width = 1280;
        int screen_height = 720;

        InitWindow(screen_width, screen_height, "zombs.io");
        SetTargetFPS(60);

        GameState gameState = new GameState();

        while(!WindowShouldClose()) {
            float dt = GetFrameTime();

            gameState.update(dt);

            BeginDrawing();
            ClearBackground(DARKGREEN);
            gameState.draw();
            DrawCircle(screen_width/2, screen_height/2, 20, WHITE); // testing
            EndDrawing();
        }
        CloseWindow();
    }
}