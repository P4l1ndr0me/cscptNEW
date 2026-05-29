package systems;

import buildings.Building;
import core.EntityManager;
import core.TextureManager;
import entities.Enemy;

import static com.raylib.Helpers.newColor;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.Main.pixelFont;
import static world.World.WORLD_HEIGHT;
import static world.World.WORLD_WIDTH;

public class WaveSystem {

    // Time of day
    private int timeMinutes = 20 * 60;
    private final int DAY_MINUTES = 24 * 60;

    // Night settings
    private static final int NIGHT_START_HOUR = 21; // 9 PM
    private static final int NIGHT_END_HOUR = 6;    // 6 AM

    // Warning message timing
    private static final int NIGHT_WARNING_MINUTES = 30;   // 8:30 PM - 9:00 PM
    private static final int MORNING_MESSAGE_MINUTES = 30; // 6:00 AM - 6:30 AM

    // Speed of time
    private float minutesPerSecond = 12f;
    private float timeAccumulator = 0;

    // Wave progression
    private int waveNumber = 0;
    private boolean waveActive = false;
    private boolean firstWaveStarted = false;
    private boolean waveStartedThisNight = false;

    // Spawn control
    private float spawnTimer = 0;
    private float baseSpawnInterval = 3.0f;

    // UI
    private String timeString = "8:00 PM";
    private float darknessAlpha = 0f;

    // Spawn radius around Gold Stash
    private final int MIN_SPAWN_RADIUS = 420;
    private final int MAX_SPAWN_RADIUS = 560;

    // Ring spawning
    private final float ANGLE_JITTER = 0.18f;
    private final float RADIUS_JITTER = 45f;

    public WaveSystem() {
        updateTimeString();
    }

    public void update(float dt) {
        updateTime(dt);
        updateDarknessAlpha();

        boolean night = isNight();
        boolean hasGoldStash = BuildSystem.getGoldStash() != null;

        // Once daytime starts, allow the next night to start a new wave
        if (!night) {
            waveStartedThisNight = false;
        }

        // Start one wave per night, only after Gold Stash exists
        if (night && !waveActive && !waveStartedThisNight && hasGoldStash) {
            startWave();
        }

        // End the wave when night ends.
        // This only stops spawning; existing zombies stay alive.
        if (!night && waveActive) {
            endWave();
        }

        // During an active night wave, spawn zombies every few seconds
        if (waveActive && night) {
            updateSpawning(dt);
        } else {
            spawnTimer = 0;
        }
    }

    private void updateTime(float dt) {
        timeAccumulator += dt;

        int minutesToAdd = (int)(timeAccumulator * minutesPerSecond);

        if (minutesToAdd > 0) {
            timeAccumulator -= minutesToAdd / minutesPerSecond;
            timeMinutes = (timeMinutes + minutesToAdd) % DAY_MINUTES;
            updateTimeString();
        }
    }

    private void startWave() {
        waveActive = true;
        firstWaveStarted = true;
        waveStartedThisNight = true;
        spawnTimer = 0;

        waveNumber++;
    }

    private void endWave() {
        waveActive = false;
        spawnTimer = 0;
    }

    private void updateSpawning(float dt) {
        float dynamicInterval = baseSpawnInterval;

        // Later waves spawn rings slightly faster
        dynamicInterval -= waveNumber * 0.04f;

        // Prevent spawning from becoming too fast
        dynamicInterval = Math.max(1.25f, dynamicInterval);

        spawnTimer += dt;

        while (spawnTimer >= dynamicInterval) {
            spawnTimer -= dynamicInterval;
            spawnZombieRing();
        }
    }

    private int getRingSpawnCount() {
        // Increase ring size as waves go up
        return Math.min(6 + waveNumber * 2, 26);
    }

    private void spawnZombieRing() {
        Building goldStash = BuildSystem.getGoldStash();

        if (goldStash == null) {
            return;
        }

        float stashCenterX = goldStash.position.x() + Building.size / 2f;
        float stashCenterY = goldStash.position.y() + Building.size / 2f;

        int count = getRingSpawnCount();

        float startAngle = (float)(Math.random() * Math.PI * 2.0);
        float angleStep = (float)((Math.PI * 2.0) / count);

        for (int i = 0; i < count; i++) {
            ZombieTier tier = chooseZombieTier();

            float angle = startAngle
                    + i * angleStep
                    + (float)((Math.random() - 0.5) * 2.0 * ANGLE_JITTER);

            float radius = (MIN_SPAWN_RADIUS + MAX_SPAWN_RADIUS) / 2f
                    + (float)((Math.random() - 0.5) * 2.0 * RADIUS_JITTER);

            float x = stashCenterX + (float)Math.cos(angle) * radius;
            float y = stashCenterY + (float)Math.sin(angle) * radius;

            x = Math.max(0, Math.min(WORLD_WIDTH - 1, x));
            y = Math.max(0, Math.min(WORLD_HEIGHT - 1, y));

            Enemy enemy = new Enemy(
                    newVector2(x, y),
                    tier.scale,
                    tier.speed,
                    TextureManager.getTexture(tier.textureName),
                    3,
                    3,
                    tier.health,
                    tier.damage
            );

            EntityManager.addEnemy(enemy);
        }
    }

    private ZombieTier chooseZombieTier() {
        double roll = Math.random();

        if (waveNumber >= 9 && roll < 0.10) {
            return ZombieTier.TIER_4;
        }

        if (waveNumber >= 6 && roll < 0.25) {
            return ZombieTier.TIER_3;
        }

        if (waveNumber >= 3 && roll < 0.55) {
            return ZombieTier.TIER_2;
        }

        return ZombieTier.TIER_1;
    }

    private void updateDarknessAlpha() {
        int hour = timeMinutes / 60;
        int minute = timeMinutes % 60;
        float timeInHours = hour + minute / 60f;

        float maxDarkness = 0.28f;

        // 9 PM to midnight: slowly get darker
        if (timeInHours >= 21 && timeInHours < 24) {
            darknessAlpha = (timeInHours - 21f) / 3f * maxDarkness;
        }

        // Midnight to 4 AM: darkest
        else if (timeInHours >= 0 && timeInHours < 4) {
            darknessAlpha = maxDarkness;
        }

        // 4 AM to 6 AM: brighten up
        else if (timeInHours >= 4 && timeInHours < 6) {
            darknessAlpha = maxDarkness * (1f - (timeInHours - 4f) / 2f);
        }

        // Daytime
        else {
            darknessAlpha = 0f;
        }

        darknessAlpha = Math.max(0f, Math.min(maxDarkness, darknessAlpha));
    }

    private void updateTimeString() {
        int hour24 = timeMinutes / 60;
        int minute = timeMinutes % 60;

        int hour12 = hour24 % 12;

        if (hour12 == 0) {
            hour12 = 12;
        }

        String ampm = (hour24 < 12) ? "AM" : "PM";

        timeString = String.format("%d:%02d %s", hour12, minute, ampm);
    }

    public void draw() {
        drawCenteredText("Time: " + timeString, 10, 24, WHITE);

        // Show warning messages even before first wave starts
        //drawTransitionMessages();

        // Before the first wave starts, only show time + transition messages
        if (!firstWaveStarted) {
            return;
        }

        drawCenteredText("Wave: " + waveNumber, 38, 24, WHITE);
        drawCenteredText("Zombies: " + EntityManager.spawnedEnemies.size(), 66, 24, WHITE);
    }

    private void drawCenteredText(String text, float y, float fontSize, Color color) {
        Vector2 textSize = MeasureTextEx(pixelFont, text, fontSize, 1.0f);

        DrawTextEx(
                pixelFont,
                text,
                newVector2((GetScreenWidth() - textSize.x()) / 2f, y),
                fontSize,
                1.0f,
                color
        );
    }

    public void drawDarknessOverlay() {
        if (darknessAlpha > 0.01f) {
            Color overlay = newColor(0, 0, 20, (int)(255 * darknessAlpha));
            DrawRectangle(0, 0, GetScreenWidth(), GetScreenHeight(), overlay);
        }
    }

    public void reset() {
        timeMinutes = 20 * 60;
        timeAccumulator = 0;
        spawnTimer = 0;

        waveNumber = 0;
        waveActive = false;
        firstWaveStarted = false;
        waveStartedThisNight = false;

        updateTimeString();
    }

    public boolean isNight() {
        int hour = timeMinutes / 60;

        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR;
    }

    private enum ZombieTier {
        TIER_1(
                "Zombie Tier 1",
                2.0f,
                50.0f,
                100,
                10,
                1
        ),

        TIER_2(
                "Zombie Tier 2",
                2.0f,
                65.0f,
                140,
                15,
                3
        ),

        TIER_3(
                "Zombie Tier 3",
                2.0f,
                45.0f,
                250,
                25,
                6
        ),

        TIER_4(
                "Zombie Tier 4",
                2.0f,
                35.0f,
                400,
                40,
                9
        );

        final String textureName;
        final float scale;
        final float speed;
        final int health;
        final int damage;
        final int unlockWave;

        ZombieTier(
                String textureName,
                float scale,
                float speed,
                int health,
                int damage,
                int unlockWave
        ) {
            this.textureName = textureName;
            this.scale = scale;
            this.speed = speed;
            this.health = health;
            this.damage = damage;
            this.unlockWave = unlockWave;
        }
    }
}