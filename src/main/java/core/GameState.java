package core;

import static com.raylib.Colors.RAYWHITE;
import static com.raylib.Raylib.*;

public class GameState {
    public GameState() {

    }

    public void update(float dt) {

    }

    public void draw() {
        int length = MeasureText("Game loop running!", 24);
        DrawText("Game loop running!", GetScreenWidth()/2-length/2, GetScreenHeight()/2 - 12, 24, RAYWHITE);
        DrawFPS(16, 16);
    }
}
