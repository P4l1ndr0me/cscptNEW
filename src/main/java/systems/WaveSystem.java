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
    private int timeMinutes = 21 * 60;
    private final int DAY_MINUTES = 24 * 60;

    // Night settings
    private static final int NIGHT_START_HOUR = 21; // 9 PM
    private static final int NIGHT_END_HOUR = 6;    // 6 AM

    // Warning message timing
    private static final int NIGHT_WARNING_MINUTES = 30;   // 8:30 PM - 9:00 PM
    private static final int MORNING_MESSAGE_MINUTES = 30; // 6:00 AM - 6:30 AM

    // Speed of time
    private float minutesPerSecond = 5f;
    private float timeAccumulator = 0;

    // Wave progression
    private int waveNumber = 1;
    private boolean waveActive = false;
    private boolean firstWaveStarted = false;
    private boolean waveStartedThisNight = false;

    // Wave intensity
    private float currentIntensity = 0f;

    // Spawn control
    private float spawnTimer = 0;
    private float baseSpawnInterval = 2.0f;

    // UI
    private String timeString = "8:00 PM";
    private float darknessAlpha = 0f;

    // Spawn radius around Gold Stash
    private final int MIN_SPAWN_RADIUS = 700;
    private final int MAX_SPAWN_RADIUS = 800;

    public WaveSystem() {
        updateTimeString();
    }

    public void update(float dt) {
        updateTime(dt);
        updateDarknessAlpha();

        if (IsKeyPressed(KEY_E)) {
            skipToNextNight();
        }

        currentIntensity = getIntensityAtTime();

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
        if (waveActive && night && currentIntensity > 0.01f) {
            updateSpawning(dt);
        } else {
            spawnTimer = 0;
        }
    }

    private void skipToNextNight() {
        // Do not skip if there is no Gold Stash yet
        if (BuildSystem.getGoldStash() == null) {
            return;
        }

        // If a wave is currently active, end it first
        if (waveActive) {
            endWave();
        }

        // Set time to exactly 9:00 PM
        timeMinutes = NIGHT_START_HOUR * 60;
        timeAccumulator = 0;
        spawnTimer = 0;

        // Allow the wave to start immediately this frame
        waveStartedThisNight = false;

        updateTimeString();
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
    }

    private void endWave() {
        waveActive = false;
        spawnTimer = 0;

        waveNumber++;
    }

    private float getIntensityAtTime() {
        int hour = timeMinutes / 60;
        int minute = timeMinutes % 60;
        float timeInHours = hour + minute / 60f;

        if (!isNight()) {
            return 0f;
        }

        float intensity;

        // 9 PM to midnight: ramp up
        if (timeInHours >= 21 && timeInHours < 24) {
            intensity = 0.25f + (timeInHours - 21f) / 3f * 0.75f;
        }

        // Midnight to 3 AM: maximum danger
        else if (timeInHours >= 0 && timeInHours < 3) {
            intensity = 1.0f;
        }

        // 3 AM to 6 AM: ramp down
        else {
            intensity = 1.0f - (timeInHours - 3f) / 3f;
        }

        return Math.max(0f, Math.min(1f, intensity));
    }

    private void updateSpawning(float dt) {
        float dynamicInterval = baseSpawnInterval / currentIntensity;

        // Later waves spawn slightly faster
        dynamicInterval -= waveNumber * 0.04f;

        // Prevent spawning from becoming too fast
        dynamicInterval = Math.max(0.75f, dynamicInterval);

        spawnTimer += dt;

        while (spawnTimer >= dynamicInterval) {
            spawnTimer -= dynamicInterval;
            spawnWaveBatch();
        }
    }

    private void spawnWaveBatch() {
        int zombieCount = getBatchZombieCount();

        for (int i = 0; i < zombieCount; i++) {
            spawnZombie();
        }
    }

    private int getBatchZombieCount() {
        int baseCount = 2 + waveNumber / 3;
        int intensityBonus = (int)(currentIntensity * 3);

        return Math.min(baseCount + intensityBonus, 14);
    }

    private void spawnZombie() {
        ZombieTier tier = chooseZombieTier();

        if (tier == null) {
            return;
        }

        Vector2 spawnPos = getSpawnPositionAroundGoldStash();

        if (spawnPos == null) {
            return;
        }

        Enemy enemy = new Enemy(
                spawnPos,
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

    private Vector2 getSpawnPositionAroundGoldStash() {
        Building goldStash = BuildSystem.getGoldStash();

        if (goldStash == null) {
            return null;
        }

        float stashCenterX = goldStash.position.x() + Building.size / 2f;
        float stashCenterY = goldStash.position.y() + Building.size / 2f;

        double angle = Math.random() * 2 * Math.PI;
        double distance = MIN_SPAWN_RADIUS + Math.random() * (MAX_SPAWN_RADIUS - MIN_SPAWN_RADIUS);

        float x = (float)(stashCenterX + distance * Math.cos(angle));
        float y = (float)(stashCenterY + distance * Math.sin(angle));

        x = Math.max(0, Math.min(WORLD_WIDTH - 1, x));
        y = Math.max(0, Math.min(WORLD_HEIGHT - 1, y));

        return newVector2(x, y);
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

        drawIntensityBar();
    }

    private void drawIntensityBar() {
        int barWidth = 220;
        int barHeight = 12;
        int barX = (GetScreenWidth() - barWidth) / 2;
        int barY = 105;

        DrawRectangle(barX, barY, barWidth, barHeight, DARKGRAY);
        DrawRectangle(barX, barY, (int)(barWidth * currentIntensity), barHeight, RED);

        String text = "Wave Intensity";
        Vector2 textSize = MeasureTextEx(pixelFont, text, 20, 1.0f);

        DrawTextEx(
                pixelFont,
                text,
                newVector2((GetScreenWidth() - textSize.x()) / 2f, barY - 24),
                20,
                1.0f,
                WHITE
        );
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
                30.0f,
                30,
                5,
                1
        ),

        TIER_2(
                "Zombie Tier 2",
                2.0f,
                60.0f,
                175,
                9,
                3
        ),

        TIER_3(
                "Zombie Tier 3",
                2.0f,
                50.0f,
                300,
                16,
                6
        ),

        TIER_4(
                "Zombie Tier 4",
                2.0f,
                36.0f,
                500,
                26,
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