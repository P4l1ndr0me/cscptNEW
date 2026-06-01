package ui;

import core.Main;
import core.TextureManager;
import core.WeaponManager;
import core.EntityManager;
import entities.Weapon;
import systems.PurchaseSystem;
import systems.WaveSystem;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

import static core.Main.pixelFont;
import static entities.Player.numStone;
import static entities.Player.numGold;
import static ui.BuildMenu.MENU_FILL;

public class HUD {
    // Resource panel
    private static final int menuX = (int) (0.8 * Main.SCREEN_WIDTH);
    private static final int menuY = Main.SCREEN_HEIGHT - 140;
    private static final int menuHeight = 120;
    public static final Rectangle resourceRect = newRectangle(
            menuX, menuY, Main.SCREEN_WIDTH * 0.18f, menuHeight);

    // Shop panel dimensions
    private static final float shopPanW = Main.SCREEN_WIDTH * 2f / 3f;
    private static final float shopPanH = Main.SCREEN_HEIGHT * 2f / 3f;
    private static final float shopPanX = (Main.SCREEN_WIDTH - shopPanW) / 2f;
    private static final float shopPanY = (Main.SCREEN_HEIGHT - shopPanH) / 2f;

    // Store which weapons are currently being displayed (pickaxe and sword only)
    private static Weapon[] displayedWeapons = new Weapon[2];

    // Buttons
    private static final Rectangle shopButton = newRectangle(menuX + 70, menuY - 70, 140, 50);
    private static final Rectangle helpButton = newRectangle(Main.SCREEN_WIDTH - 120, 20, 100, 40);
    private static boolean helpScreenOpen = false;

    // Shop panel
    private static final Rectangle shopPanel = newRectangle(shopPanX, shopPanY, shopPanW, shopPanH);

    // Help panel
    private static final float helpPanW = Main.SCREEN_WIDTH * 2f / 3f + 150;
    private static final float helpPanH = Main.SCREEN_HEIGHT * 2f / 3f + 150;
    private static final float helpPanX = (Main.SCREEN_WIDTH - helpPanW) / 2f;
    private static final float helpPanY = (Main.SCREEN_HEIGHT - helpPanH) / 2f;
    private static final Rectangle helpPanel = newRectangle(helpPanX, helpPanY, helpPanW, helpPanH);
    private static final Rectangle closeHelpButton = newRectangle(helpPanX + helpPanW - 80, helpPanY + 20, 60, 35);

    // Weapon item backgrounds (pickaxe and sword only)
    private static final Rectangle pickRect = newRectangle(shopPanX + 50, shopPanY + 130, shopPanW - 100, 80);
    private static final Rectangle swordRect = newRectangle(shopPanX + 50, shopPanY + 240, shopPanW - 100, 80);

    // Buy buttons
    private static final Rectangle buyButton1 = newRectangle(shopPanX + 690, shopPanY + 145, 100, 50);
    private static final Rectangle buyButton2 = newRectangle(shopPanX + 690, shopPanY + 255, 100, 50);

    private static boolean shopOpen = false;
    private static float itemScale = 4.0f;
    private static int currentShopTab = 0; // 0 = weapons only now

    // Visual feedback for purchases
    private static float[] buttonFlashTimers = new float[2];
    private static String purchaseMessage = "";
    private static float purchaseMessageTimer = 0f;

    private static core.GameState gameState;

    // Draws the entire HUD including resources, buttons, shop, and help screens
    public static void drawHUD() {
        // Show game over screen instead of normal HUD
        if (core.GameState.isGameOver()) {
            drawGameOverScreen();
            return;
        }

        // Resource panel (rounded with outline)
        DrawRectangleRoundedLinesEx(resourceRect, 0.6f, 0, 2.0f, BLACK);
        DrawRectangleRounded(resourceRect, 0.6f, 0, MENU_FILL);
        DrawTextEx(pixelFont, "Stone: " + numStone, newVector2(menuX + 70, menuY + 26), 24, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Gold: " + numGold, newVector2(menuX + 70, menuY + 60), 24, 1.0f, WHITE);

        // HELP BUTTON (top right) - rounded with outline
        DrawRectangleRounded(helpButton, 0.2f, 0, DARKGRAY);
        DrawRectangleRoundedLinesEx(helpButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "HELP", newVector2(helpButton.x() + 29, helpButton.y() + 10), 20, 1.0f, WHITE);

        // Shop button - rounded with outline
        DrawRectangleRounded(shopButton, 0.2f, 0, DARKGRAY);
        DrawRectangleRoundedLinesEx(shopButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "SHOP", newVector2(shopButton.x() + 44, shopButton.y() + 12), 26, 1.0f, WHITE);

        // Help Screen (draws over everything else when open)
        if (helpScreenOpen) {
            drawHelpScreen();
        }

        // Shop panel - NO rounded corners, only black outline
        if (shopOpen && !helpScreenOpen) {
            // Darken background
            DrawRectangle(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, Fade(BLACK, 0.85f));

            DrawRectangleRounded(shopPanel, 0.2f, 0, Fade(BLACK, 0.8f));
            DrawTextEx(pixelFont, "SHOP MENU", newVector2((int) (shopPanX + shopPanW) / 2 + 50, (int) shopPanY + 30), 24, 1.0f, WHITE);

            displayWeaponsTab();

            // Draw purchase message if active
            if (purchaseMessageTimer > 0) {
                int msgWidth = (int) MeasureTextEx(pixelFont, purchaseMessage, 20, 1.0f).x();
                DrawTextEx(pixelFont, purchaseMessage,
                        newVector2(shopPanX + shopPanW / 2 - msgWidth / 2, shopPanY + shopPanH - 40),
                        20, 1.0f, YELLOW);
            }
        }
    }

    // Draws the help screen with game controls and information
    private static void drawHelpScreen() {
        // Darken background
        DrawRectangle(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, Fade(BLACK, 0.85f));

        // Help panel background (rounded with outline)
        float panelRoundness = 0.6f;
        DrawRectangleRounded(helpPanel, panelRoundness, 0, Fade(DARKGRAY, 0.95f));
        DrawRectangleRoundedLinesEx(helpPanel, panelRoundness, 0, 2.0f, BLACK);

        // Title
        DrawTextEx(pixelFont, "HOW TO PLAY",
                newVector2(helpPanX + helpPanW / 2 - MeasureTextEx(pixelFont, "HOW TO PLAY", 32, 1.0f).x() / 2, helpPanY + 40),
                32, 1.0f, GOLD);

        // Close button
        float closeWidth = 60;
        float closeHeight = 35;
        float closeX = helpPanX + helpPanW - closeWidth - 20;
        float closeY = helpPanY + 20;
        Rectangle closeButton = newRectangle(closeX, closeY, closeWidth, closeHeight);
        DrawRectangleRounded(closeButton, 0.2f, 0, RED);
        DrawRectangleRoundedLinesEx(closeButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "X", newVector2(closeX + 22, closeY + 8), 20, 1.0f, WHITE);

        // Update static close button for input detection
        closeHelpButton.x(closeX);
        closeHelpButton.y(closeY);
        closeHelpButton.width(closeWidth);
        closeHelpButton.height(closeHeight);

        // Controls sections
        float startX = helpPanX + 50;
        float startY = helpPanY + 110;
        int lineHeight = 35;

        // Movement
        DrawTextEx(pixelFont, "MOVEMENT", newVector2(startX, startY), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "W / A / S / D     -     Move around the world", newVector2(startX + 30, startY + lineHeight), 18, 1.0f, WHITE);

        // Tools (mining & sword)
        DrawTextEx(pixelFont, "TOOLS", newVector2(startX, startY + lineHeight * 3), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "R     -     Equip / Unequip pickaxe", newVector2(startX + 30, startY + lineHeight * 4), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "G     -     Equip / Unequip sword", newVector2(startX + 30, startY + lineHeight * 5), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "SPACE     -     Mine/Attack", newVector2(startX + 30, startY + lineHeight * 6), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "F      -        Skip to night", newVector2(startX + 30, startY + lineHeight * 7), 18, 1.0f, WHITE);

        // Shop
        DrawTextEx(pixelFont, "SHOP", newVector2(startX, startY + lineHeight * 9), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "B     -     Open / Close shop", newVector2(startX + 30, startY + lineHeight * 10), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Click BUY     -     Purchase weapons and upgrades", newVector2(startX + 30, startY + lineHeight * 11), 18, 1.0f, WHITE);

        // Resources
        DrawTextEx(pixelFont, "RESOURCES", newVector2(helpPanX + helpPanW / 2 + 50, startY), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "Stone     -    Mined from stone nodes", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Gold     -    Earned from placing Gold Mines", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight * 2), 18, 1.0f, WHITE);

        // Upgrades
        DrawTextEx(pixelFont, "UPGRADES", newVector2(helpPanX + helpPanW / 2 + 50, startY + lineHeight * 4), 24, 1.0f, SKYBLUE);
        DrawTextEx(pixelFont, "Better pickaxes = faster mining", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight * 5), 18, 1.0f, WHITE);
        DrawTextEx(pixelFont, "Better swords = more damage", newVector2(helpPanX + helpPanW / 2 + 80, startY + lineHeight * 6), 18, 1.0f, WHITE);
    }

    // Updates HUD input, timers, and UI state (shop, help screen, buttons)
    public static void updateHUD() {
        // Game over screen only checks exit button
        if (core.GameState.isGameOver()) {
            Vector2 mouse = GetMousePosition();
            Rectangle exitButton = newRectangle(Main.SCREEN_WIDTH/2 - 60, Main.SCREEN_HEIGHT/2 + 50, 120, 40);
            if (CheckCollisionPointRec(mouse, exitButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                System.exit(0);
            }
            return;
        }

        Vector2 mouse = GetMousePosition();

        // Update visual feedback timers
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
            helpScreenOpen = !helpScreenOpen;
            WaveSystem.setPaused(helpScreenOpen);
            EntityManager.setPaused(helpScreenOpen);
            if (helpScreenOpen) shopOpen = false;
        }

        // Close help with ESC
        if (helpScreenOpen && IsKeyPressed(KEY_ESCAPE)) {
            helpScreenOpen = false;
            WaveSystem.setPaused(false);
            EntityManager.setPaused(false);
        }

        // Toggle shop with B key
        if (!helpScreenOpen && IsKeyPressed(KEY_B)) {
            shopOpen = !shopOpen;
            if (shopOpen) {
                updateDisplayedWeapons();
            }
        }

        // Help screen button interactions
        if (helpScreenOpen) {
            if (CheckCollisionPointRec(mouse, closeHelpButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                helpScreenOpen = false;
                WaveSystem.setPaused(false);
                EntityManager.setPaused(false);
            }
            return;
        }

        // Help button click
        if (CheckCollisionPointRec(mouse, helpButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            helpScreenOpen = true;
            WaveSystem.setPaused(true);
            EntityManager.setPaused(true);
            shopOpen = false;
        }

        // Shop button click
        if (CheckCollisionPointRec(mouse, shopButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            shopOpen = !shopOpen;
            if (shopOpen) updateDisplayedWeapons();
        }
    }

    // Refreshes the displayed weapons based on current unlocked tiers
    private static void updateDisplayedWeapons() {
        displayedWeapons[0] = WeaponManager.getNextWeapon("pickaxe");
        displayedWeapons[1] = WeaponManager.getNextWeapon("sword");
    }

    // Draws the weapons shop tab with all purchasable items
    private static void displayWeaponsTab() {
        Vector2 mouse = GetMousePosition();

        // Background sections
        DrawRectangleRounded(pickRect, 0.2f, 0, Fade(GRAY, 0.8f));
        DrawRectangleRounded(swordRect, 0.2f, 0, Fade(GRAY, 0.8f));

        // Pickaxe
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

        // Sword
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

        // Pickaxe buy button (faded if can't afford)
        if (displayedWeapons[0] != null) {
            boolean canAfford = numGold >= displayedWeapons[0].getCost();
            Color btnColor = canAfford ? GREEN : Fade(GREEN, 0.4f);
            if (buttonFlashTimers[0] > 0) btnColor = WHITE;
            DrawRectangleRounded(buyButton1, 0.2f, 0, btnColor);
            DrawRectangleRoundedLinesEx(buyButton1, 0.2f, 0, 2.0f, BLACK);
            DrawTextEx(pixelFont, "BUY", newVector2(shopPanX + 700, shopPanY + 155), 30, 1.0f, BLACK);
            if (canAfford && CheckCollisionPointRec(mouse, buyButton1) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                purchaseWeapon("pickaxe", displayedWeapons[0], 0);
            }
        } else {
            DrawRectangleRounded(buyButton1, 0.2f, 0, DARKGRAY);
            DrawRectangleRoundedLinesEx(buyButton1, 0.2f, 0, 2.0f, BLACK);
            DrawTextEx(pixelFont, "MAX", newVector2(shopPanX + 705, shopPanY + 155), 25, 1.0f, BLACK);
        }

        // Sword buy button (faded if can't afford)
        if (displayedWeapons[1] != null) {
            boolean canAfford = numGold >= displayedWeapons[1].getCost();
            Color btnColor = canAfford ? GREEN : Fade(GREEN, 0.4f);
            if (buttonFlashTimers[1] > 0) btnColor = WHITE;
            DrawRectangleRounded(buyButton2, 0.2f, 0, btnColor);
            DrawRectangleRoundedLinesEx(buyButton2, 0.2f, 0, 2.0f, BLACK);
            DrawTextEx(pixelFont, "BUY", newVector2(shopPanX + 700, shopPanY + 265), 30, 1.0f, BLACK);
            if (canAfford && CheckCollisionPointRec(mouse, buyButton2) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                purchaseWeapon("sword", displayedWeapons[1], 1);
            }
        } else {
            DrawRectangleRounded(buyButton2, 0.2f, 0, DARKGRAY);
            DrawRectangleRoundedLinesEx(buyButton2, 0.2f, 0, 2.0f, BLACK);
            DrawTextEx(pixelFont, "MAX", newVector2(shopPanX + 705, shopPanY + 265), 25, 1.0f, BLACK);
        }
    }

    private static void purchaseWeapon(String weaponType, Weapon weapon, int buttonIndex) {
        boolean success = PurchaseSystem.purchaseWeapon(weaponType, weapon);
        if (success) {
            buttonFlashTimers[buttonIndex] = 0.5f;
            purchaseMessage = "Purchased " + weapon.getName() + "!";
            purchaseMessageTimer = 1.5f;
            updateDisplayedWeapons();
        }
    }

    public static void drawGameOverScreen() {
        DrawRectangle(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT, Fade(BLACK, 0.9f));

        DrawTextEx(pixelFont, "GAME OVER",
                newVector2(Main.SCREEN_WIDTH/2 - 80, Main.SCREEN_HEIGHT/2 - 80),
                40, 1.0f, RED);

        DrawTextEx(pixelFont, "Your Gold Stash was destroyed!",
                newVector2(Main.SCREEN_WIDTH/2 - 120, Main.SCREEN_HEIGHT/2 - 30),
                18, 1.0f, WHITE);

        Rectangle playAgainButton = newRectangle(Main.SCREEN_WIDTH/2 - 130, Main.SCREEN_HEIGHT/2 + 20, 120, 45);
        DrawRectangleRounded(playAgainButton, 0.2f, 0, GREEN);
        DrawRectangleRoundedLinesEx(playAgainButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "PLAY AGAIN",
                newVector2(playAgainButton.x() + 18, playAgainButton.y() + 12),
                16, 1.0f, BLACK);

        Rectangle exitButton = newRectangle(Main.SCREEN_WIDTH/2 + 10, Main.SCREEN_HEIGHT/2 + 20, 120, 45);
        DrawRectangleRounded(exitButton, 0.2f, 0, DARKGRAY);
        DrawRectangleRoundedLinesEx(exitButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "EXIT",
                newVector2(exitButton.x() + 38, exitButton.y() + 12),
                16, 1.0f, WHITE);

        Vector2 mouse = GetMousePosition();

        if (CheckCollisionPointRec(mouse, playAgainButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            if (gameState != null) {
                gameState.reset();
            }
        }

        if (CheckCollisionPointRec(mouse, exitButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            System.exit(0);
        }
    }

    public static void setGameState(core.GameState gs) {
        gameState = gs;
    }

    public static boolean isModalOpen() {
        return shopOpen || helpScreenOpen;
    }

    public static void reset() {
        helpScreenOpen = false;
        shopOpen = false;
        purchaseMessage = "";
        purchaseMessageTimer = 0f;

        for (int i = 0; i < buttonFlashTimers.length; i++) {
            buttonFlashTimers[i] = 0f;
        }

        updateDisplayedWeapons();

        // Reset pause states
        WaveSystem.setPaused(false);
        EntityManager.setPaused(false);
    }
}