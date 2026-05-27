package systems;

import core.EntityManager;
import core.TextureManager;
import entities.Enemy;
import com.raylib.Raylib;
import world.World;

import java.util.ArrayList;

import static com.raylib.Helpers.newColor;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static world.World.WORLD_HEIGHT;
import static world.World.WORLD_WIDTH;

public class WaveSystem {

    // Time of day (in minutes, 0 = 12:00 AM)
    private int timeMinutes = 22 * 60; // Start at 10:00 PM (22:00)
    private final int DAY_MINUTES = 24 * 60;

    // Speed of time (real seconds per in-game hour)
    private float timeSpeed = 5.0f;
    private float timeAccumulator = 0;

    // Spawn control
    private float spawnTimer = 0;
    private float baseSpawnInterval = 2.0f;

    // Wave intensity
    private float currentIntensity = 0;

    // UI
    private String timeString = "10:00 PM";

    // Screen fade (for day/night transition)
    private float darknessAlpha = 0f;

    // Spawn points tracking
    public static ArrayList<int[]> zombieSpawnPoint = new ArrayList<>();

    private final int SAFE_RADIUS = 500;

    public WaveSystem() {
        updateTimeString();
    }

    public void update(float deltaTime) {
        // Advance time
        timeAccumulator += deltaTime;
        float minutesPerSecond = 60.0f / timeSpeed;
        int minutesToAdd = (int)(timeAccumulator * minutesPerSecond);
        if (minutesToAdd > 0) {
            timeAccumulator -= minutesToAdd / minutesPerSecond;
            timeMinutes = (timeMinutes + minutesToAdd) % DAY_MINUTES;
            updateTimeString();
        }

        // Update darkness alpha based on time
        updateDarknessAlpha();

        // Calculate wave intensity based on hour (0-24)
        int hour = timeMinutes / 60;
        currentIntensity = getIntensityAtHour(hour);

        // Spawn zombies during night hours (0 = 12am, 8 = 8am)
        boolean isNight = (hour >= 0 && hour < 8);
        if (isNight && currentIntensity > 0.01f) {
            float dynamicInterval = baseSpawnInterval / currentIntensity;
            spawnTimer += deltaTime;
            while (spawnTimer >= dynamicInterval) {
                spawnTimer -= dynamicInterval;
                spawnWaveBatch();
            }
        } else {
            spawnTimer = 0;
        }
    }

    private void updateDarknessAlpha() {
        int hour = timeMinutes / 60;
        int minute = timeMinutes % 60;
        float timeInHours = hour + minute / 60f;

        float maxDarkness = 0.45f; // maximum darkness opacity (0.45 = 45% dark)

        if (timeInHours >= 22 && timeInHours <= 24) {
            // 22:00 → 24:00 : alpha 0 → maxDarkness
            darknessAlpha = (timeInHours - 22) / 2f * maxDarkness;
        } else if (timeInHours >= 0 && timeInHours < 6) {
            // 00:00 → 06:00 : constant maxDarkness
            darknessAlpha = maxDarkness;
        } else if (timeInHours >= 6 && timeInHours <= 8) {
            // 06:00 → 08:00 : alpha maxDarkness → 0
            darknessAlpha = maxDarkness * (1f - (timeInHours - 6) / 2f);
        } else {
            darknessAlpha = 0f;
        }

        darknessAlpha = Math.max(0f, Math.min(maxDarkness, darknessAlpha));
    }

    private float getIntensityAtHour(int hour) {
        if (hour < 0 || hour > 8) return 0f;
        float intensity;
        if (hour <= 3) {
            intensity = (float) hour / 3.0f;
        } else {
            intensity = 1.0f - (float)(hour - 3) / 5.0f;
        }
        return Math.max(0, Math.min(1, intensity));
    }

    private void spawnWaveBatch() {
        int fastZombieCount = (int)(5 * currentIntensity);
        int toughZombieCount = (int)(2 * currentIntensity);

        if (fastZombieCount < 1 && currentIntensity > 0) fastZombieCount = 1;
        if (toughZombieCount < 0) toughZombieCount = 0;

        for (int i = 0; i < fastZombieCount; i++) {
            int[] pos = getSpawnPosition();
            EntityManager.spawnZombie(
                    pos[0], pos[1],
                    2.0f, 50.0f,
                    TextureManager.getTexture("enemy1"),
                    3, 3
            );
        }

        if (currentIntensity > 0.3f) {
            for (int i = 0; i < toughZombieCount; i++) {
                int[] pos = getSpawnPosition();
                EntityManager.spawnZombie(
                        pos[0], pos[1],
                        2.0f, 75.0f,
                        TextureManager.getTexture("enemy2"),
                        3, 3
                );
            }
        }
    }

    private int[] getSpawnPosition() {
        int centerX = WORLD_WIDTH / 2;
        int centerY = WORLD_HEIGHT / 2;

        double angle = Math.random() * 2 * Math.PI;
        double maxDist = Math.min(WORLD_WIDTH, WORLD_HEIGHT) / 2.0;
        double distance = SAFE_RADIUS + Math.random() * (maxDist - SAFE_RADIUS);

        int x = (int)(centerX + distance * Math.cos(angle));
        int y = (int)(centerY + distance * Math.sin(angle));

        x = Math.max(0, Math.min(WORLD_WIDTH - 1, x));
        y = Math.max(0, Math.min(WORLD_HEIGHT - 1, y));

        zombieSpawnPoint.add(new int[]{x, y});
        return new int[]{x, y};
    }

    private void updateTimeString() {
        int hour24 = timeMinutes / 60;
        int minute = timeMinutes % 60;
        int hour12 = hour24 % 12;
        if (hour12 == 0) hour12 = 12;
        String ampm = (hour24 < 12) ? "AM" : "PM";
        timeString = String.format("%d:%02d %s", hour12, minute, ampm);
    }

    public void draw() {
        // Draw time at top center (screen‑fixed)
        String text = "Time: " + timeString;
        int textWidth = MeasureText(text, 20);
        DrawText(text, (GetScreenWidth() - textWidth) / 2, 10, 20, WHITE);

        // Draw intensity bar
        int barWidth = 200;
        int barHeight = 10;
        int barX = (GetScreenWidth() - barWidth) / 2;
        int barY = 55;
        DrawRectangle(barX, barY, barWidth, barHeight, DARKGRAY);
        DrawRectangle(barX, barY, (int)(barWidth * currentIntensity), barHeight, RED);
        DrawText("Wave Intensity", barX, barY - 20, 20, BLACK);
    }

    /**
     * Draws a full‑screen dark overlay with alpha based on time of day.
     * Call this AFTER EndMode2D() (i.e., in screen space).
     */
    public void drawDarknessOverlay() {
        // black
//        if (darknessAlpha > 0.01f) {
//            Color overlay = newColor(0, 0, 0, (int)(255 * darknessAlpha));
//            DrawRectangle(0, 0, GetScreenWidth(), GetScreenHeight(), overlay);
//        }

        // blue
        if (darknessAlpha > 0.01f) {
            Color overlay = newColor(0, 0, 30, (int)(255 * darknessAlpha));
            DrawRectangle(0, 0, GetScreenWidth(), GetScreenHeight(), overlay);
        }
    }

    public void reset() {
        timeMinutes = 22 * 60;
        timeAccumulator = 0;
        spawnTimer = 0;
        updateTimeString();
    }

    public boolean isNight() {
        int hour = timeMinutes / 60;
        return hour >= 0 && hour < 8;
    }
}