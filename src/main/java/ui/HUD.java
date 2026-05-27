package ui;

import core.Main;
import core.TextureManager;
import core.WeaponManager;
import entities.Weapon;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

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
    private static int[] weaponCosts = new int[3];

    // SHOP BUTTON
    private static final Rectangle shopButton = newRectangle(menuX + 70, menuY - 70, 140, 50);

    // SHOP PANEL
    private static final Rectangle shopPanel = newRectangle(shopPanX, shopPanY, shopPanW, shopPanH);

    // TAB BUTTONS
    private static final Rectangle weaponsTabButton = newRectangle(shopPanX + 50, shopPanY + 60, 150, 40);
    private static final Rectangle buildingsTabButton = newRectangle(shopPanX + 250, shopPanY + 60, 150, 40);

    // ITEM RECTANGLES - Hardcoded for 1280x720
    private static final Rectangle pickRect = newRectangle(shopPanX + 50, shopPanY + 130, shopPanW - 100, 80);
    private static final Rectangle swordRect = newRectangle(shopPanX + 50, shopPanY + 240, shopPanW - 100, 80);
    private static final Rectangle bowRect = newRectangle(shopPanX + 50, shopPanY + 350, shopPanW - 100, 80);

    // BUY BUTTONS - Hardcoded for 1280x720
    private static final Rectangle buyButton1 = newRectangle(shopPanX + 690, shopPanY + 145, 100, 50);
    private static final Rectangle buyButton2 = newRectangle(shopPanX + 690, shopPanY + 255, 100, 50);
    private static final Rectangle buyButton3 = newRectangle(shopPanX + 690, shopPanY + 365, 100, 50);

    private static boolean shopOpen = false;
    private static float itemScale = 4.0f;
    private static int currentShopTab = 2; // 0 = weapons, 1 = buildings
    private static boolean[] buyButtonClicked = new boolean[3];

    public static void drawHUD() {
        // Draw resource HUD
        DrawRectangleRoundedLinesEx(resourceRect, 0.6f, 0, 2.0f, BLUE);
        DrawRectangleRounded(resourceRect, 0.6f, 0, MENU_FILL);
        DrawText("Stone: " + numStone, menuX + 20, menuY + 20, 20, BLUE);
        DrawText("Gold: " + numGold, menuX + 20, menuY + 50, 20, BLUE);

        // SHOP BUTTON
        DrawRectangleRec(shopButton, DARKGRAY);
        DrawText("SHOP", menuX + 110, menuY - 55, 25, WHITE);

        // SHOP PANEL
        if (shopOpen) {
            DrawRectangleRounded(shopPanel, 0.2f, 0, Fade(BLACK, 0.8f));
            DrawText("SHOP MENU", (int) (shopPanX + shopPanW) / 2 + 50, (int)shopPanY + 30, 24, WHITE);

            // TAB BUTTONS
            DrawRectangleRec(weaponsTabButton, currentShopTab == 0 ? DARKGRAY : GRAY);
            DrawText("Weapons", (int) (shopPanX + 80), (int) (shopPanY + 70), 20, WHITE);

            DrawRectangleRec(buildingsTabButton, currentShopTab == 1 ? DARKGRAY : GRAY);
            DrawText("Buildings", (int) (shopPanX + 280), (int) (shopPanY + 70), 20, WHITE);

            if (currentShopTab == 0){
                displayWeaponsTab();
            }
        }
    }

    public static void updateHUD(){
        Vector2 mouse = GetMousePosition();

        // Toggle shop
        if (IsKeyPressed(KEY_B)) {
            shopOpen = !shopOpen;
            if (shopOpen && currentShopTab == 2) {
                currentShopTab = 0; // Default to weapons tab
                updateDisplayedWeapons();
            }
        }

        // Shop interactions
        if (shopOpen) {
            if (CheckCollisionPointRec(mouse, weaponsTabButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentShopTab = 0;
                updateDisplayedWeapons();
            }

            if (CheckCollisionPointRec(mouse, buildingsTabButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
                currentShopTab = 1;
                // Initialize buildings tab when implemented
            }
        }
    }

    private static void updateDisplayedWeapons() {
        // Get next tier weapons for each type
        Weapon pickaxe = WeaponManager.getNextWeapon("pickaxe");
        Weapon sword = WeaponManager.getNextWeapon("sword");
        Weapon bow = WeaponManager.getNextWeapon("bow");

        displayedWeapons[0] = pickaxe;
        displayedWeapons[1] = sword;
        displayedWeapons[2] = bow;

        // Calculate costs
        for (int i = 0; i < displayedWeapons.length; i++) {
            if (displayedWeapons[i] != null) {
                weaponCosts[i] = displayedWeapons[i].getCost();
            } else {
                weaponCosts[i] = -1; // Max tier reached
            }
        }
    }

    public static void displayWeaponsTab() {
        Vector2 mouse = GetMousePosition();

        // Pickaxe section
        DrawRectangleRounded(pickRect, 0.2f, 0, Fade(GRAY, 0.8f));

        // Sword section
        DrawRectangleRounded(swordRect, 0.2f, 0, Fade(GRAY, 0.8f));

        // Bow section
        DrawRectangleRounded(bowRect, 0.2f, 0, Fade(GRAY, 0.8f));

        // Display Pickaxe
        if (displayedWeapons[0] != null) {
            Weapon pickaxe = displayedWeapons[0];
            DrawTextureEx(TextureManager.getTexture(pickaxe.getTextureName()),
                    newVector2(shopPanX + 75.0f, shopPanY + 145.0f), 0.0f, itemScale, WHITE);
            DrawText(pickaxe.getName(), (int) (shopPanX + 140), (int) (shopPanY + 150), 18, WHITE);
            DrawText("Tier: " + pickaxe.getTier(), (int) (shopPanX + 140), (int) (shopPanY + 170), 16, WHITE);
            DrawText("Damage: " + pickaxe.getDamage(), (int) (shopPanX + 280), (int) (shopPanY + 150), 18, WHITE);
            if (pickaxe.getEfficiency() > 0) {
                DrawText("Efficiency: +" + pickaxe.getEfficiency(), (int) (shopPanX + 280), (int) (shopPanY + 175), 18, WHITE);
            }
            DrawText("Cost: " + weaponCosts[0] + " gold", (int) (shopPanX + 450), (int) (shopPanY + 160), 18, GOLD);
        } else {
            DrawTextureEx(TextureManager.getTexture("stonepickaxe"),
                    newVector2(shopPanX + 75.0f, shopPanY + 145.0f), 0.0f, itemScale, WHITE);
            DrawText("MAX TIER REACHED", (int) (shopPanX + 200), (int) (shopPanY + 170), 20, RED);
        }

        // Display Sword
        if (displayedWeapons[1] != null) {
            Weapon sword = displayedWeapons[1];
            DrawTextureEx(TextureManager.getTexture(sword.getTextureName()),
                    newVector2(shopPanX + 75.0f, shopPanY + 255.0f), 0.0f, itemScale, WHITE);
            DrawText(sword.getName(), (int) (shopPanX + 140), (int) (shopPanY + 260), 18, WHITE);
            DrawText("Tier: " + sword.getTier(), (int) (shopPanX + 140), (int) (shopPanY + 280), 16, WHITE);
            DrawText("Damage: " + sword.getDamage(), (int) (shopPanX + 280), (int) (shopPanY + 260), 18, WHITE);
            DrawText("Cost: " + weaponCosts[1] + " gold", (int) (shopPanX + 450), (int) (shopPanY + 270), 18, GOLD);
        } else {
            DrawTextureEx(TextureManager.getTexture("woodensword"),
                    newVector2(shopPanX + 75.0f, shopPanY + 255.0f), 0.0f, itemScale, WHITE);
            DrawText("MAX TIER REACHED", (int) (shopPanX + 200), (int) (shopPanY + 280), 20, RED);
        }

        // Display Bow
        if (displayedWeapons[2] != null) {
            Weapon bow = displayedWeapons[2];
            DrawTextureEx(TextureManager.getTexture(bow.getTextureName()),
                    newVector2(shopPanX + 75.0f, shopPanY + 365.0f), 0.0f, itemScale, WHITE);
            DrawText(bow.getName(), (int) (shopPanX + 140), (int) (shopPanY + 370), 18, WHITE);
            DrawText("Tier: " + bow.getTier(), (int) (shopPanX + 140), (int) (shopPanY + 390), 16, WHITE);
            DrawText("Damage: " + bow.getDamage(), (int) (shopPanX + 280), (int) (shopPanY + 370), 18, WHITE);
            DrawText("Cost: " + weaponCosts[2] + " gold", (int) (shopPanX + 450), (int) (shopPanY + 380), 18, GOLD);
        } else {
            DrawTextureEx(TextureManager.getTexture("woodenbow"),
                    newVector2(shopPanX + 75.0f, shopPanY + 365.0f), 0.0f, itemScale, WHITE);
            DrawText("MAX TIER REACHED", (int) (shopPanX + 200), (int) (shopPanY + 390), 20, RED);
        }

        // Buy buttons colors
        Color buy1Color = buyButtonClicked[0] ? newColor(0, 120, 0, 120) : GREEN;
        Color buy2Color = buyButtonClicked[1] ? newColor(0, 120, 0, 120) : GREEN;
        Color buy3Color = buyButtonClicked[2] ? newColor(0, 120, 0, 120) : GREEN;

        // Check if player can afford
        if (displayedWeapons[0] != null && numGold < weaponCosts[0]) {
            buy1Color = Fade(RED, 0.5f);
        }
        if (displayedWeapons[1] != null && numGold < weaponCosts[1]) {
            buy2Color = Fade(RED, 0.5f);
        }
        if (displayedWeapons[2] != null && numGold < weaponCosts[2]) {
            buy3Color = Fade(RED, 0.5f);
        }

        // Draw buy buttons
        DrawRectangleRounded(buyButton1, 0.2f, 0, buy1Color);
        DrawRectangleRounded(buyButton2, 0.2f, 0, buy2Color);
        DrawRectangleRounded(buyButton3, 0.2f, 0, buy3Color);

        DrawText("BUY", (int) (shopPanX + 700), (int) (shopPanY + 155), 30, BLACK);
        DrawText("BUY", (int) (shopPanX + 700), (int) (shopPanY + 265), 30, BLACK);
        DrawText("BUY", (int) (shopPanX + 700), (int) (shopPanY + 375), 30, BLACK);

        // Handle purchase clicks
        if (displayedWeapons[0] != null && CheckCollisionPointRec(mouse, buyButton1) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            if (numGold >= weaponCosts[0] && !buyButtonClicked[0]) {
                purchaseWeapon("pickaxe", displayedWeapons[0], weaponCosts[0]);
                buyButtonClicked[0] = true;
            }
        }

        if (displayedWeapons[1] != null && CheckCollisionPointRec(mouse, buyButton2) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            if (numGold >= weaponCosts[1] && !buyButtonClicked[1]) {
                purchaseWeapon("sword", displayedWeapons[1], weaponCosts[1]);
                buyButtonClicked[1] = true;
            }
        }

        if (displayedWeapons[2] != null && CheckCollisionPointRec(mouse, buyButton3) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            if (numGold >= weaponCosts[2] && !buyButtonClicked[2]) {
                purchaseWeapon("bow", displayedWeapons[2], weaponCosts[2]);
                buyButtonClicked[2] = true;
            }
        }
    }

    private static void purchaseWeapon(String weaponType, Weapon weapon, int cost) {
        if (numGold >= cost) {
            numGold -= cost;
            WeaponManager.unlockNextTier(weaponType);
            updateDisplayedWeapons();

            // Reset click states
            for (int i = 0; i < buyButtonClicked.length; i++) {
                buyButtonClicked[i] = false;
            }

            System.out.println("Purchased " + weapon.getName() + "!");
        }
    }
}