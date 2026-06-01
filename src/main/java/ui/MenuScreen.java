package ui;

import core.GameState;
import core.Main;
import core.TextureManager;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static core.Main.pixelFont;

public class MenuScreen {
    // Actual button rectangles (used for both drawing and click detection)
    private static final Rectangle beginButton = newRectangle(
            Main.SCREEN_WIDTH / 2f - 100,
            Main.SCREEN_HEIGHT / 2f + 110,  // was +80 then shifted +30
            200, 50);
    private static final Rectangle exitButton = newRectangle(
            Main.SCREEN_WIDTH / 2f - 100,
            Main.SCREEN_HEIGHT / 2f + 180,  // was +150 then shifted +30
            200, 50);

    public static void update() {
        Vector2 mouse = GetMousePosition();

        if (CheckCollisionPointRec(mouse, beginButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            GameState.setState(GameState.State.PLAYING);
        }

        if (CheckCollisionPointRec(mouse, exitButton) && IsMouseButtonPressed(MOUSE_BUTTON_LEFT)) {
            System.exit(0);
        }
    }

    public static void draw() {
        ClearBackground(newColor(20, 20, 30, 255));

        // Title
        String title = "WELCOME TO JOMBIE";
        float titleFontSize = 60;
        Vector2 titleSize = MeasureTextEx(pixelFont, title, titleFontSize, 1.0f);
        DrawTextEx(pixelFont, title,
                newVector2(Main.SCREEN_WIDTH / 2f - titleSize.x() / 2, 60),
                titleFontSize, 1.0f, GOLD);

        // Credits
        String credit1 = "by: Jayden Fu";
        String credit2 = "by: Joshua Yu";
        String credit3 = "by: Austin Huang";
        float creditFontSize = 24;
        Vector2 credit1Size = MeasureTextEx(pixelFont, credit1, creditFontSize, 1.0f);
        Vector2 credit2Size = MeasureTextEx(pixelFont, credit2, creditFontSize, 1.0f);
        Vector2 credit3Size = MeasureTextEx(pixelFont, credit3, creditFontSize, 1.0f);
        float creditStartY = Main.SCREEN_HEIGHT / 2f - 110;
        DrawTextEx(pixelFont, credit1,
                newVector2(Main.SCREEN_WIDTH / 2f - credit1Size.x() / 2, creditStartY),
                creditFontSize, 1.0f, WHITE);
        DrawTextEx(pixelFont, credit2,
                newVector2(Main.SCREEN_WIDTH / 2f - credit2Size.x() / 2, creditStartY + 35),
                creditFontSize, 1.0f, WHITE);
        DrawTextEx(pixelFont, credit3,
                newVector2(Main.SCREEN_WIDTH / 2f - credit3Size.x() / 2, creditStartY + 70),
                creditFontSize, 1.0f, WHITE);

        // Tagline
        String tagline = "The night belongs to them... unless you fight back.";
        float taglineFontSize = 20;
        Vector2 taglineSize = MeasureTextEx(pixelFont, tagline, taglineFontSize, 1.0f);
        float taglineX = Main.SCREEN_WIDTH / 2f - taglineSize.x() / 2;
        float taglineY = creditStartY + 110;
        for (int offset = -1; offset <= 1; offset++) {
            DrawTextEx(pixelFont, tagline,
                    newVector2(taglineX + offset, taglineY + offset),
                    taglineFontSize, 1.0f, newColor(255, 255, 200, 40));
        }
        DrawTextEx(pixelFont, tagline,
                newVector2(taglineX, taglineY),
                taglineFontSize, 1.0f, RAYWHITE);

        // Player
        Texture playerTex = TextureManager.getTexture("playerpic");
        if (playerTex != null) {
            float playerScale = 10f;
            float playerWidth = playerTex.width() * playerScale;
            float playerHeight = playerTex.height() * playerScale;
            float playerX = beginButton.x() - playerWidth - 200;
            float playerY = beginButton.y() + (beginButton.height() - playerHeight) / 2f - 40;
            DrawTextureEx(playerTex, newVector2(playerX, playerY), 0, playerScale, WHITE);
        }

        // Zombies
        Texture[] zombieSheets = {
                TextureManager.getTexture("Zombie Tier 1"),
                TextureManager.getTexture("Zombie Tier 2"),
                TextureManager.getTexture("Zombie Tier 3"),
                TextureManager.getTexture("Zombie Tier 4")
        };
        float zombieScale = 6.0f;
        java.util.Random rand = new java.util.Random(54321);
        float startX = beginButton.x() + beginButton.width() + 120;
        float verticalRange = 480f;

        for (int i = 0; i < zombieSheets.length; i++) {
            Texture sheet = zombieSheets[i];
            if (sheet == null) continue;

            int frameWidth = sheet.width() / 3;
            int frameHeight = sheet.height() / 4;
            Rectangle source = new Rectangle().x(0).y(0).width(frameWidth).height(frameHeight);

            float yOffset = (rand.nextFloat() - 0.5f) * verticalRange;
            float x = startX + i * (frameWidth * zombieScale + 20);
            float y = (Main.SCREEN_HEIGHT / 2f) + yOffset - (frameHeight * zombieScale / 2f);
            y = Math.max(20, Math.min(Main.SCREEN_HEIGHT - frameHeight * zombieScale - 20, y));

            Rectangle dest = new Rectangle()
                    .x(x)
                    .y(y)
                    .width(frameWidth * zombieScale)
                    .height(frameHeight * zombieScale);

            DrawTexturePro(sheet, source, dest, newVector2(0, 0), 0, WHITE);
        }

        // Draw buttons using the same rectangles
        DrawRectangleRounded(beginButton, 0.2f, 0, GREEN);
        DrawRectangleRoundedLinesEx(beginButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "BEGIN",
                newVector2(beginButton.x() + 60, beginButton.y() + 12),
                28, 1.0f, BLACK);

        DrawRectangleRounded(exitButton, 0.2f, 0, RED);
        DrawRectangleRoundedLinesEx(exitButton, 0.2f, 0, 2.0f, BLACK);
        DrawTextEx(pixelFont, "EXIT",
                newVector2(exitButton.x() + 68, exitButton.y() + 12),
                28, 1.0f, BLACK);
    }
}