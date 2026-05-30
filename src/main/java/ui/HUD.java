package ui;

import core.Main;
import core.TextureManager;
import core.WeaponManager;
import entities.Weapon;
import systems.PurchaseSystem;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

import static core.Main.pixelFont;
import static entities.Player.numStone;
import static entities.Player.numGold;
import static ui.BuildMenu.MENU_FILL;

public class HUD {
    private static final int menuX = (int) (0.8 * Main.SCREEN_WIDTH);
    private static final int menuY = Main.SCREEN_HEIGHT - 140;
    private static final int menuHeight = 120;
    public static final Rectangle resourceRect = newRectangle(
            menuX, menuY, Main.SCREEN_WIDTH * 0.18f, menuHeight);

    private static final float shopPanW = Main.SCREEN_WIDTH * 2f / 3f;
    private static final float shopPanH = Main.SCREEN_HEIGHT * 2f / 3f;
    private static final float shopPanX = (Main.SCREEN_WIDTH - shopPanW) / 2f;
    private static final float shopPanY = (Main.SCREEN_HEIGHT - shopPanH) / 2f;

    // Store which weapons are currently being displayed
    private static Weapon[] displayedWeapons = new Weapon[3];

    // SHOP BUTTON
    private static final Rectangle shopButton = newRectangle(menuX + 70, menuY - 70, 140, 50);

    // HELP BUTTON (top right)
    private static final Rectangle helpButton = newRectangle(Main.SCREEN_WIDTH - 120, 20, 100, 40);
    private static boolean helpScreenOpen = false;

    // SHOP PANEL
    private static final Rectangle shopPanel = newRectangle(shopPanX, shopPanY, shopPanW, shopPanH);

    // HELP PANEL
    private static final float helpPanW = Main.SCREEN_WIDTH * 2f / 3f;
    private static final float helpPanH = Main.SCREEN_HEIGHT * 2f / 3f;
    private static final float helpPanX = (Main.SCREEN_WIDTH - helpPanW) / 2f;
    private static final float helpPanY = (Main.SCREEN_HEIGHT - helpPanH) / 2f;
    private static final Rectangle helpPanel = newRectangle(helpPanX, helpPanY, helpPanW, helpPanH);
    private static final Rectangle closeHelpButton = newRectangle(helpPanX + helpPanW - 80, helpPanY + 20, 60, 35);

    // TAB BUTTONS
    private static final Rectangle weaponsTabButton = newRectangle(shopPanX + 50, shopPanY + 60, 150, 40);
    private static final Rectangle buildingsTabButton = newRectangle(shopPanX + 250, shopPanY + 60, 150, 40);

    // ITEM RECTANGLES
    private static final Rectangle pickRect = newRectangle(shopPanX + 50, shopPanY + 130, shopPanW - 100, 80);
    private static final Rectangle swordRect = newRectangle(shopPanX + 50, shopPanY + 240, shopPanW - 100, 80);
    private static final Rectangle bowRect = newRectangle(shopPanX + 50, shopPanY + 350, shopPanW - 100, 80);

    // BUY BUTTONS
    private static final Rectangle buyButton1 = newRectangle(shopPanX + 690, shopPanY + 145, 100, 50);
    private static final Rectangle buyButton2 = newRectangle(shopPanX + 690, shopPanY + 255, 100, 50);
    private static final Rectangle buyButton3 = newRectangle(shopPanX + 690, shopPanY + 365, 100, 50);

    private static boolean shopOpen = false;
    private static float itemScale = 4.0f;
    private static int currentShopTab = 2; // 0 = weapons, 1 = buildings

    // Visual feedback fields
    private static float[] buttonFlashTimers = new float[3]; // 0=pickaxe,1=sword,2=bow
    private static String purchaseMessage = "";
    private static float purchaseMessageTimer = 0f;

    public static void drawHUD() {
        // Resource panel
        DrawRectangleRoundedLinesEx(resourceRect, 0.6f, 0, 2.0f, BLACK);
        DrawRectangleRounded(resourceRect, 0.6f, 0, MENU_FILL);
        DrawTextEx(pixelFont, "Stone: " + numStone, newVector2(menuX + 20, menuY + 16), 24, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Gold: " + numGold, newVector2(menuX + 20, menuY + 45), 24, 1.0f, WHITE);

        // HELP BUTTON (top right)
        DrawRectangleRounded(helpButton, 0.2f, 0, DARKGRAY);
        DrawTextEx(pixelFont, "HELP", newVector2(helpButton.x() + 25, helpButton.y() + 12), 16, 1.0f, WHITE);

        // Shop button
        DrawRectangleRec(shopButton, DARKGRAY);
        DrawTextEx(pixelFont, "SHOP", newVector2(menuX + 110, menuY - 55), 25, 1.0f, WHITE);

        // Help Screen (draws over everything else when open)
        if (helpScreenOpen) {
            drawHelpScreen();
        }

        // Shop panel
        if (shopOpen && !helpScreenOpen) {
            DrawRectangleRounded(shopPanel, 0.2f, 0, Fade(BLACK, 0.8f));
            DrawTextEx(pixelFont, "SHOP MENU", newVector2((int) (shopPanX + shopPanW) / 2 + 50, (int) shopPanY + 30), 24, 1.0f, WHITE);

            // Tabs
            DrawRectangleRec(weaponsTabButton, currentShopTab == 0 ? DARKGRAY : GRAY);
            DrawTextEx(pixelFont, "Weapons", newVector2(shopPanX + 80, shopPanY + 70), 20, 1.0f, WHITE);

            DrawRectangleRec(buildingsTabButton, currentShopTab == 1 ? DARKGRAY : GRAY);
            DrawTextEx(pixelFont, "Buildings", newVector2(shopPanX + 280, shopPanY + 70), 20, 1.0f, WHITE);

            if (currentShopTab == 0) {
                displayWeaponsTab();
            }

            // Draw purchase message if active
            if (purchaseMessageTimer > 0) {
                int msgWidth = (int) MeasureTextEx(pixelFont, purchaseMessage, 20, 1.0f).x();
                DrawTextEx(pixelFont, purchaseMessage,
                        newVector2(shopPanX + shopPanW / 2 - msgWidth / 2, shopPanY + shopPanH - 40),
                        20, 1.0f, YELLOW);
            }
        }
    }

    private static void drawHelpScreen() {
        // Darken background
        DrawRectangle(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, Fade(BLACK, 0.85f));

        // Help panel background
        DrawRectangleRounded(helpPanel, 0.2f, 0, Fade(DARKGRAY, 0.95f));
        DrawRectangleRoundedLinesEx(helpPanel, 0.2f, 0, 3.0f, GOLD);

        // Title
        DrawTextEx(pixelFont, "HOW TO PLAY",
                newVector2(helpPanX + helpPanW / 2 - MeasureTextEx(pixelFont, "HOW TO PLAY", 32, 1.0f).x() / 2, helpPanY + 40),
                32, 1.0f, GOLD);

        // Close button
        DrawRectangleRounded(closeHelpButton, 0.2f, 0, RED);
        DrawTextEx(pixelFont, "X", newVector2(closeHelpButton.x() + 22, closeHelpButton.y() + 8), 20, 1.0f, WHITE);

        // Controls sections
        float startX = helpPanX + 50;
        float startY = helpPanY + 110;
        int lineHeight = 35;

        // Movement
        DrawTextEx(pixelFont, "MOVEMENT", newVector2(startX, startY), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "W / A / S / D     -     Move around the world", newVector2(startX + 30, startY + lineHeight), 18, 1.0f, WHITE);

        // Mining
        DrawTextEx(pixelFont, "MINING", newVector2(startX, startY + lineHeight * 3), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "R     -     Equip / Unequip pickaxe", newVector2(startX + 30, startY + lineHeight * 4), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "SPACE     -     Start / Stop auto-mining", newVector2(startX + 30, startY + lineHeight * 5), 18, 1.0f, WHITE);

        // Shop
        DrawTextEx(pixelFont, "SHOP", newVector2(startX, startY + lineHeight * 7), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "B     -     Open / Close shop", newVector2(startX + 30, startY + lineHeight * 8), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Click BUY     -     Purchase weapons and upgrades", newVector2(startX + 30, startY + lineHeight * 9), 18, 1.0f, WHITE);

        // Resources
        DrawTextEx(pixelFont, "RESOURCES", newVector2(helpPanX + helpPanW / 2 + 50, startY), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "Stone     -    Mined from stone nodes", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Gold     -    Earned from placing Gold Mines", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight * 2), 18, 1.0f, WHITE);

        // Upgrades
        DrawTextEx(pixelFont, "UPGRADES", newVector2(helpPanX + helpPanW / 2 + 50, startY + lineHeight * 4), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "Better pickaxes = faster mining", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight * 5), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "& more stone per hit!", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight * 6), 18, 1.0f, WHITE);

        // Buildings (future)
        DrawTextEx(pixelFont, "BUILDINGS", newVector2(helpPanX + helpPanW / 2 + 50, startY + lineHeight * 7), 24, 1.0f, SKYBLUE);
    }

    public static void updateHUD() {
        Vector2 mouse = GetMousePosition();

        // Update timers for visual feedback
        for (int i = 0; i < buttonFlashTimers.length; i++) {
            if (buttonFlashTimers[i] > 0) {
                buttonFlashTimers[i] -= GetFrameTime();
            }
        }
        if (purchaseMessageTimer > 0) {
            purchaseMessageTimer -= GetFrameTime();
        }

        // Toggle help screen with H key
        if (IsKeyPressed(KEY_H)) {
            if (helpScreenOpen) {
                helpScreenOpen = false;
            } else {
                helpScreenOpen = true;
                shopOpen = false; // Close shop if open when help opens
            }
        }

        // Close help with ESC
        if (helpScreenOpen && IsKeyPressed(KEY_ESCAPE)) {
            helpScreenOpen = false;
        }

        // Toggle shop (only if help screen is not open)
        if (!helpScreenOpen && IsKeyPressed(KEY_B)) {
            shopOpen = !shopOpen;
            if (shopOpen && currentShopTab == 2) {
                currentShopTab = 0;
                updateDisplayedWeapons();
            }
        }

        // Help screen button interactions
        if (helpScreenOpen) {
            // Close button click
            if (CheckCollisionPointRec(mouse, closeHelpButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                helpScreenOpen = false;
            }
            return; // Don't process other UI when help is open
        }

        // Tab switching (only if shop open)
        if (shopOpen) {
            if (CheckCollisionPointRec(mouse, weaponsTabButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentShopTab = 0;
                updateDisplayedWeapons();
            }
            if (CheckCollisionPointRec(mouse, buildingsTabButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentShopTab = 1;
                // Buildings tab placeholder
            }
        }

        // Help button click (top right)
        if (CheckCollisionPointRec(mouse, helpButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            helpScreenOpen = true;
            shopOpen = false;
        }
    }

    private static void updateDisplayedWeapons() {
        displayedWeapons[0] = WeaponManager.getNextWeapon("pickaxe");
        displayedWeapons[1] = WeaponManager.getNextWeapon("sword");
        displayedWeapons[2] = WeaponManager.getNextWeapon("bow");
    }

    private static void displayWeaponsTab() {
        Vector2 mouse = GetMousePosition();

        // Background sections
        DrawRectangleRounded(pickRect, 0.2f, 0, Fade(GRAY, 0.8f));
        DrawRectangleRounded(swordRect, 0.2f, 0, Fade(GRAY, 0.8f));
        DrawRectangleRounded(bowRect, 0.2f, 0, Fade(GRAY, 0.8f));

        // ─── PICKAXE ──────────────────────────────────────────
        if (displayedWeapons[0] != null) {
            Weapon w = displayedWeapons[0];
            DrawTextureEx(TextureManager.getTexture(w.getTextureName()),
                    newVector2(shopPanX + 75, shopPanY + 145), 0, itemScale, WHITE);
            DrawTextEx(pixelFont, w.getName(), newVector2(shopPanX + 140, shopPanY + 150), 18, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Tier: " + w.getTier(), newVector2(shopPanX + 140, shopPanY + 170), 16, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Damage: " + w.getDamage(), newVector2(shopPanX + 280, shopPanY + 150), 18, 1.0f, WHITE);
            if (w.getEfficiency() > 0) {
                DrawTextEx(pixelFont, "Efficiency: +" + w.getEfficiency(), newVector2(shopPanX + 280, shopPanY + 175), 18, 1.0f, WHITE);
            }
            DrawTextEx(pixelFont, "Cost: " + w.getCost() + " gold", newVector2(shopPanX + 450, shopPanY + 160), 18, 1.0f, GOLD);
        } else {
            DrawTextureEx(TextureManager.getTexture("stonepickaxe"),
                    newVector2(shopPanX + 75, shopPanY + 145), 0, itemScale, WHITE);
            DrawTextEx(pixelFont, "MAX TIER REACHED", newVector2(shopPanX + 200, shopPanY + 170), 20, 1.0f, RED);
        }

        // ─── SWORD ────────────────────────────────────────────
        if (displayedWeapons[1] != null) {
            Weapon w = displayedWeapons[1];
            DrawTextureEx(TextureManager.getTexture(w.getTextureName()),
                    newVector2(shopPanX + 75, shopPanY + 255), 0, itemScale, WHITE);
            DrawTextEx(pixelFont, w.getName(), newVector2(shopPanX + 140, shopPanY + 260), 18, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Tier: " + w.getTier(), newVector2(shopPanX + 140, shopPanY + 280), 16, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Damage: " + w.getDamage(), newVector2(shopPanX + 280, shopPanY + 260), 18, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Cost: " + w.getCost() + " gold", newVector2(shopPanX + 450, shopPanY + 270), 18, 1.0f, GOLD);
        } else {
            DrawTextureEx(TextureManager.getTexture("woodensword"),
                    newVector2(shopPanX + 75, shopPanY + 255), 0, itemScale, WHITE);
            DrawTextEx(pixelFont, "MAX TIER REACHED", newVector2(shopPanX + 200, shopPanY + 280), 20, 1.0f, RED);
        }

        // ─── BOW ──────────────────────────────────────────────
        if (displayedWeapons[2] != null) {
            Weapon w = displayedWeapons[2];
            DrawTextureEx(TextureManager.getTexture(w.getTextureName()),
                    newVector2(shopPanX + 75, shopPanY + 365), 0, itemScale, WHITE);
            DrawTextEx(pixelFont, w.getName(), newVector2(shopPanX + 140, shopPanY + 370), 18, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Tier: " + w.getTier(), newVector2(shopPanX + 140, shopPanY + 390), 16, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Damage: " + w.getDamage(), newVector2(shopPanX + 280, shopPanY + 370), 18, 1.0f, WHITE);
            DrawTextEx(pixelFont, "Cost: " + w.getCost() + " gold", newVector2(shopPanX + 450, shopPanY + 380), 18, 1.0f, GOLD);
        } else {
            DrawTextureEx(TextureManager.getTexture("woodenbow"),
                    newVector2(shopPanX + 75, shopPanY + 365), 0, itemScale, WHITE);
            DrawTextEx(pixelFont, "MAX TIER REACHED", newVector2(shopPanX + 200, shopPanY + 390), 20, 1.0f, RED);
        }

        // ─── BUY BUTTONS with flash effect ────────────────────
        // Pickaxe button
        if (displayedWeapons[0] != null) {
            Color btnColor = GREEN;
            if (buttonFlashTimers[0] > 0) btnColor = WHITE;
            DrawRectangleRounded(buyButton1, 0.2f, 0, btnColor);
            DrawTextEx(pixelFont, "BUY", newVector2(shopPanX + 700, shopPanY + 155), 30, 1.0f, BLACK);
            if (CheckCollisionPointRec(mouse, buyButton1) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                purchaseWeapon("pickaxe", displayedWeapons[0], 0);
            }
        } else {
            DrawRectangleRounded(buyButton1, 0.2f, 0, DARKGRAY);
            DrawTextEx(pixelFont, "MAX", newVector2(shopPanX + 705, shopPanY + 155), 25, 1.0f, BLACK);
        }

        // Sword button
        if (displayedWeapons[1] != null) {
            Color btnColor = GREEN;
            if (buttonFlashTimers[1] > 0) btnColor = WHITE;
            DrawRectangleRounded(buyButton2, 0.2f, 0, btnColor);
            DrawTextEx(pixelFont, "BUY", newVector2(shopPanX + 700, shopPanY + 265), 30, 1.0f, BLACK);
            if (CheckCollisionPointRec(mouse, buyButton2) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                purchaseWeapon("sword", displayedWeapons[1], 1);
            }
        } else {
            DrawRectangleRounded(buyButton2, 0.2f, 0, DARKGRAY);
            DrawTextEx(pixelFont, "MAX", newVector2(shopPanX + 705, shopPanY + 265), 25, 1.0f, BLACK);
        }

        // Bow button
        if (displayedWeapons[2] != null) {
            Color btnColor = GREEN;
            if (buttonFlashTimers[2] > 0) btnColor = WHITE;
            DrawRectangleRounded(buyButton3, 0.2f, 0, btnColor);
            DrawTextEx(pixelFont, "BUY", newVector2(shopPanX + 700, shopPanY + 375), 30, 1.0f, BLACK);
            if (CheckCollisionPointRec(mouse, buyButton3) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                purchaseWeapon("bow", displayedWeapons[2], 2);
            }
        } else {
            DrawRectangleRounded(buyButton3, 0.2f, 0, DARKGRAY);
            DrawTextEx(pixelFont, "MAX", newVector2(shopPanX + 705, shopPanY + 375), 25, 1.0f, BLACK);
        }
    }

    private static void purchaseWeapon(String weaponType, Weapon weapon, int buttonIndex) {
        boolean success = PurchaseSystem.purchaseWeapon(weaponType, weapon);
        if (success) {
            // Visual feedback: flash button and show message
            buttonFlashTimers[buttonIndex] = 0.5f; // flash for 0.5 seconds
            purchaseMessage = "Purchased " + weapon.getName() + "!";
            purchaseMessageTimer = 1.5f;
            updateDisplayedWeapons(); // Refresh shop with next tier
        }
    }
}