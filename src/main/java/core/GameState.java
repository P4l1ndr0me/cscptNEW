package core;

import systems.*;
import ui.*;
import world.*;
import entities.*;
import buildings.*;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.Main.pixelFont;
import systems.WaveSystem;

public class GameState {
    public enum State {
        PLAYING,
        GAME_OVER
    }

    private static State currentState = State.PLAYING;

    // Store instance for reset
    private static GameState instance;

    public static void setState(State newState) {
        currentState = newState;
    }

    public static State getState() {
        return currentState;
    }

    public static boolean isPlaying() {
        return currentState == State.PLAYING;
    }

    public static boolean isGameOver() {
        return currentState == State.GAME_OVER;
    }

    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;
    final private WaveSystem waveSystem;
    final private BuildingSelectionSystem buildingSelectionSystem;

    public GameState() {
        instance = this;

        TextureManager.init();
        SetTextureFilter(pixelFont.texture(), TEXTURE_FILTER_POINT);

        // Create new instances
        player = new Player();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();
        waveSystem = new WaveSystem();
        buildingSelectionSystem = new BuildingSelectionSystem(buildSystem);

        // initialize
        Camera.init();
        ResourceNode.init();
        WeaponManager.init();
    }

    public void update() {
        float dt = GetFrameTime();

        // If game over, skip all game logic
        if (currentState == State.GAME_OVER) {
            HUD.updateHUD();
            return;
        }

        // Normal game update (only reaches here if NOT game over)
        HUD.updateHUD();
        player.update(dt);
        Camera.update(player.getPosition());
        buildSystem.update();
        buildingSelectionSystem.update();
        EntityManager.updateEntities(dt);
        waveSystem.update(dt);
    }

    public void draw() {
        BeginDrawing();
        ClearBackground(GRAY);

        BeginMode2D(Camera.camera);

        // Draw background and grid lines
        World.draw();

        // Draw entities
        EntityManager.drawEntities();

        // Draw player
        player.draw();

        // Draw building preview
        buildSystem.draw();

        // Hitboxes (debugging)
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            DrawCircleLinesV(stoneCenter, ResourceNode.stoneRadius, RED);
        }
        DrawRectangleLinesEx(Player.playerRec, 1.0f, RED);
        DrawRectangleLinesEx(Player.miningRec, 1.0f, RED);

        EndMode2D();

        // Draw UI & HUD
        buildMenu.draw();
        HUD.drawHUD();

        buildingSelectionSystem.drawUI();

        waveSystem.draw();
        waveSystem.drawDarknessOverlay();

        EndDrawing();
    }

    public void reset() {
        // Reset game state
        currentState = State.PLAYING;

        // Reset all static systems
        EntityManager.reset();
        WeaponManager.reset();
        Camera.reset();
        HUD.reset();
        ResourceNode.reset();

        // Reset instance systems
        waveSystem.reset();
        player.reset();
        buildSystem.reset();
        buildingSelectionSystem.reset();

        // Clear all dynamic entities
        EntityManager.stoneCenters.clear();
        EntityManager.placedBuildings.clear();
        EntityManager.spawnedEnemies.clear();
        EntityManager.towerBullets.clear();

        // Regenerate stones
        ResourceNode.init();
    }

    public static GameState getInstance() {
        return instance;
    }
}