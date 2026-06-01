package systems;

import buildings.Building;
import core.EntityManager;
import core.TextureManager;
import entities.Enemy;

import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.Main.pixelFont;
import static world.World.WORLD_HEIGHT;
import static world.World.WORLD_WIDTH;

public class WaveSystem {

    // Time of day
    private int timeMinutes = 12 * 60; // Start game at noon
    private final int DAY_MINUTES = 24 * 60;

    // Night settings
    private static final int NIGHT_START_HOUR = 21; // 9 PM
    private static final int NIGHT_END_HOUR = 6;    // 6 AM

    // Warning message timing
    private static final int NIGHT_WARNING_MINUTES = 30;   // 8:30 PM - 9:00 PM
    private static final int MORNING_MESSAGE_MINUTES = 30; // 6:00 AM - 6:30 AM

    // Speed of time
    private float minutesPerSecond = 6f;
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
    private float baseSpawnInterval = 3.0f;

    // UI
    private String timeString;
    private float darknessAlpha = 0f;

    // Zombie spawning
    private final float SPAWN_BUFFER_MIN = 200f;
    private final float SPAWN_BUFFER_MAX = 400f;
    private final int MAX_SPAWN_ATTEMPTS = 40;

    private static boolean isPaused = false;

    // Add this method
    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    private enum ZombieTier {
        TIER_1("Zombie Tier 1", 2.0f, 100.0f, 70, 3, 5),
        TIER_2("Zombie Tier 2", 2.0f, 40.0f, 500, 9, 12),
        TIER_3("Zombie Tier 3", 2.0f, 180.0f, 40, 5, 25),
        TIER_4("Zombie Tier 4", 2.0f, 130.0f, 300, 26, 45);

        final String textureName;
        final float scale;
        final float speed;
        final int health;
        final int damage;
        final int goldDrop;

        ZombieTier(
                String textureName,
                float scale,
                float speed,
                int health,
                int damage,
                int goldDrop
        ) {
            this.textureName = textureName;
            this.scale = scale;
            this.speed = speed;
            this.health = health;
            this.damage = damage;
            this.goldDrop = goldDrop;
        }
    }

    public WaveSystem() {
        updateTimeString();
    }

    public void update(float dt) {
        updateTime(dt);
        updateDarknessAlpha();

        if (IsKeyPressed(KEY_F)) {
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
        if (isPaused) return;  // Don't advance time when paused

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

        Vector2 spawnPos = getSpawnPositionAroundBase();

        if (spawnPos == null) {
            return;
        }

        Enemy enemy = new Enemy(
                spawnPos,
                tier.scale,
                tier.speed,
                TextureManager.getTexture(tier.textureName),
                4,
                3,
                tier.health,
                tier.damage,
                tier.goldDrop
        );

        EntityManager.spawnedEnemies.add(enemy);
    }

    private Vector2 getSpawnPositionAroundBase() {
        Building goldStash = BuildSystem.getGoldStash();

        if (goldStash == null) {
            return null;
        }

        float stashCenterX = goldStash.position.x() + Building.size / 2f;
        float stashCenterY = goldStash.position.y() + Building.size / 2f;

        float furthestBuildingDistance = 0f;

        for (Building building : EntityManager.placedBuildings) {
            float buildingCenterX = building.position.x() + Building.size / 2f;
            float buildingCenterY = building.position.y() + Building.size / 2f;

            float distance = Vector2Distance(
                    newVector2(stashCenterX, stashCenterY),
                    newVector2(buildingCenterX, buildingCenterY)
            );

            if (distance > furthestBuildingDistance) {
                furthestBuildingDistance = distance;
            }
        }

        float minRadius = furthestBuildingDistance + SPAWN_BUFFER_MIN;
        float maxRadius = furthestBuildingDistance + SPAWN_BUFFER_MAX;

        for (int attempt = 0; attempt < MAX_SPAWN_ATTEMPTS; attempt++) {
            double angle = Math.random() * 2 * Math.PI;
            double distance = minRadius + Math.random() * (maxRadius - minRadius);

            float x = (float) (stashCenterX + distance * Math.cos(angle));
            float y = (float) (stashCenterY + distance * Math.sin(angle));

            x = Math.max(0, Math.min(WORLD_WIDTH - 1, x));
            y = Math.max(0, Math.min(WORLD_HEIGHT - 1, y));

            Vector2 spawnPos = newVector2(x, y);

            if (isValidZombieSpawn(spawnPos)) {
                return spawnPos;
            }
        }

        return null;
    }

    private boolean isValidZombieSpawn(Vector2 spawnPos) {
        float zombieRadius = 14f;

        // Do not spawn on stone
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            float combinedRadius = zombieRadius + world.ResourceNode.STONE_RADIUS;

            if (Vector2Distance(spawnPos, stoneCenter) < combinedRadius) {
                return false;
            }
        }

        // Do not spawn inside buildings
        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionCircleRec(spawnPos, zombieRadius, building.getRect())) {
                return false;
            }
        }

        // Do not spawn directly on top of another zombie
        for (Enemy enemy : EntityManager.spawnedEnemies) {
            if (Vector2Distance(spawnPos, enemy.getHitCenter()) < zombieRadius * 2f) {
                return false;
            }
        }

        return true;
    }

    private ZombieTier chooseZombieTier() {
        double roll = Math.random();

        if (waveNumber >= 9 && roll < 0.2) {
            return ZombieTier.TIER_4;
        }

        if (waveNumber >= 6 && roll < 0.5) {
            return ZombieTier.TIER_3;
        }

        if (waveNumber >= 3 && roll < 0.8) {
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

        drawTransitionMessages();

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

    private void drawTransitionMessages() {
        if (shouldShowNightApproaching()) {
            drawBanner(
                    "Night is approaching...",
                    145,
                    newColor(0, 0, 0, 190),
                    ORANGE
            );
        } else if (shouldShowMorningArrived()) {
            drawBanner(
                    "Morning has arrived!",
                    145,
                    newColor(0, 0, 0, 190),
                    YELLOW
            );
        }
    }

    private boolean shouldShowNightApproaching() {
        int nightStartMinutes = NIGHT_START_HOUR * 60;

        return timeMinutes >= nightStartMinutes - NIGHT_WARNING_MINUTES
                && timeMinutes < nightStartMinutes;
    }

    private boolean shouldShowMorningArrived() {
        int morningStartMinutes = NIGHT_END_HOUR * 60;

        return timeMinutes >= morningStartMinutes
                && timeMinutes < morningStartMinutes + MORNING_MESSAGE_MINUTES;
    }

    private void drawBanner(String text, float y, Color fillColor, Color textColor) {
        float fontSize = 28f;
        float spacing = 1.0f;

        Vector2 textSize = MeasureTextEx(pixelFont, text, fontSize, spacing);

        float paddingX = 20f;
        float paddingY = 10f;

        Rectangle bannerRect = newRectangle(
                (GetScreenWidth() - textSize.x()) / 2f - paddingX,
                y - paddingY,
                textSize.x() + paddingX * 2f,
                textSize.y() + paddingY * 2f
        );

        DrawRectangleRounded(bannerRect, 0.25f, 0, fillColor);
        DrawRectangleRoundedLinesEx(bannerRect, 0.25f, 0, 2.0f, WHITE);

        DrawTextEx(
                pixelFont,
                text,
                newVector2((GetScreenWidth() - textSize.x()) / 2f, y),
                fontSize,
                spacing,
                textColor
        );
    }

    public void drawDarknessOverlay() {
        if (darknessAlpha > 0.01f) {
            Color overlay = newColor(0, 0, 20, (int)(255 * darknessAlpha));
            DrawRectangle(0, 0, GetScreenWidth(), GetScreenHeight(), overlay);
        }
    }

    public void reset() {
        timeMinutes = 12 * 60;
        timeAccumulator = 0;
        spawnTimer = 0;
        waveNumber = 1;
        waveActive = false;
        firstWaveStarted = false;
        waveStartedThisNight = false;
        currentIntensity = 0f;
        darknessAlpha = 0f;
        updateTimeString();
    }

    public boolean isNight() {
        int hour = timeMinutes / 60;

        return hour >= NIGHT_START_HOUR || hour < NIGHT_END_HOUR;
    }
}