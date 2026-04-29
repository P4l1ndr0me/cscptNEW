package core;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class InputHandler {

    public void drawMouseCoord() {
        //Vector2 worldPos = GetScreenToWorld2D(mousePos, Camera.camera);
        Vector2 mousePos = GetMousePosition();
        DrawText("Mouse XY: " + (int) mousePos.x() + ", " + (int) mousePos.y(), 10, 800, 20, BLUE);
    }

}
