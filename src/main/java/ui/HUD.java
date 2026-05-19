package ui;


import core.Main;
import core.TextureManager;
import world.World;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

import static entities.Player.numStone;
import static ui.BuildMenu.menuFill;

public class HUD {
    private static final int menuX = (int) (0.8 * Main.SCREEN_WIDTH);
    private static final int menuY = Main.SCREEN_HEIGHT - 140;
    private static final int menuHeight = 120;
    public static final Rectangle resourceRect = newRectangle(
            menuX, menuY, Main.SCREEN_WIDTH * 0.18f, menuHeight);


    private static final float shopPanW = Main.SCREEN_WIDTH * 2f / 3f;
    private static final float shopPanH = Main.SCREEN_HEIGHT * 2f / 3f;

    private static final float shopPanX =
            (Main.SCREEN_WIDTH - shopPanW) / 2f;

    private static final float shopPanY =
            (Main.SCREEN_HEIGHT - shopPanH) / 2f;

    // SHOP BUTTON
    private static final Rectangle shopButton =
            newRectangle(menuX + 70, menuY - 70, 140, 50);

    // SHOP PANEL
    private static final Rectangle shopPanel = newRectangle(
            shopPanX,
            shopPanY,
            shopPanW,
            shopPanH
    );
    // ITEM BUTTONS
    private static final Rectangle weaponsTabButton = newRectangle(
            shopPanX + 50,
            shopPanY + 60,
            150,
            40
    );

    private static final Rectangle buildingsTabButton =
            newRectangle(
                    shopPanX + 250,
                    shopPanY + 60,
                    150,
                    40);

    private static boolean shopOpen = false;
    private static float itemScale = 6.0f;
    private static int currentShopTab = 2;
    private static int hoveredWeaponIndex = -1;// 0 = weapons, 1 = buildings

    public static void drawHUD() {
        // Draw outline and fill of resourceRect
        DrawRectangleRoundedLinesEx(resourceRect, 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(resourceRect, 0.6f, 0, menuFill);
        DrawText("Stone: " + numStone, menuX + 20, menuY + 20, 20, BLUE);

        // SHOP BUTTON
        DrawRectangleRec(shopButton, DARKGRAY);
        DrawText("SHOP",
                menuX + 110,
                menuY - 55,
                25,
                WHITE);

        // SHOP PANEL
        if (shopOpen) {

            DrawRectangleRounded(shopPanel,
                    0.2f,
                    0,
                    Fade(BLACK, 0.8f));

            DrawText("SHOP MENU",
                    (int) (shopPanX + shopPanW) / 2 + 50,
                    (int)shopPanY + 30,
                    24,
                    WHITE);

            // WALL BUTTON
            DrawRectangleRec(weaponsTabButton, GRAY);
            DrawText("Weapons",
                    (int) (shopPanX + 80),
                    (int) (shopPanY + 70),
                    20,
                    WHITE);

            // TURRET BUTTON
            DrawRectangleRec(buildingsTabButton, GRAY);
            DrawText("Buildings",
                    (int) (shopPanX + 280),
                    (int) (shopPanY + 70),
                    20,
                    WHITE);

            if (currentShopTab == 0){
                displayWeaponsTab("stonepickaxe");
            }
        }
    }

    public static void updateHUD(){
        Vector2 mouse = GetMousePosition();

        // Toggle shop
        if (IsKeyPressed(KEY_B)) {

            shopOpen = !shopOpen;
        }

        // Shop buttons
        if (shopOpen) {

            if (CheckCollisionPointRec(mouse, weaponsTabButton)
                    && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentShopTab = 0;
            }

            if (CheckCollisionPointRec(mouse, buildingsTabButton)
                    && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {

                currentShopTab = 1;
            }
        }
    }

    public static void displayWeaponsTab(String texture){
        DrawTextureEx(
                TextureManager.getTexture(texture),
                newVector2(shopPanX + 75.0f, shopPanY + 175.0f),
                0.0f,
                itemScale,
                WHITE
        );


    }
}
