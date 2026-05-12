package ui;


import core.Main;

import static com.raylib.Colors.BLUE;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

import static entities.Player.numStone;
import static ui.BuildMenu.menuFill;

public class HUD {
    private static final int menuX = (int) (0.8 * Main.SCREEN_WIDTH);
    private static final int menuY = Main.SCREEN_HEIGHT - 140;
    private static final int menuHeight = 120;
    public static final Rectangle resourceRect = newRectangle(menuX, menuY, Main.SCREEN_WIDTH * 0.18f, menuHeight);

    public static void drawHUD() {
        // Draw outline and fill of resourceRect
        DrawRectangleRoundedLinesEx(resourceRect, 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(resourceRect, 0.6f, 0, menuFill);
        DrawText("Stone: " + numStone, menuX + 20, menuY + 20, 20, BLUE);
    }
}
