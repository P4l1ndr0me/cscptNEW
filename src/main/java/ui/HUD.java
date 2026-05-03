package ui;


import static com.raylib.Colors.BLUE;
import static com.raylib.Raylib.*;
import static entities.Player.numStone;

public class HUD {
    public static void drawUI() {
        DrawText("Stone: " + numStone, 1400, 850, 20, BLUE);
    }
}
