package core;

import systems.*;
import ui.*;
import world.*;
import entities.*;

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

        // Always update HUD first because it controls help/shop/game over UI.
        HUD.updateHUD();

        // If game over, skip all gameplay logic.
        if (currentState == State.GAME_OVER) {
            buildSystem.cancelPlacement();
            buildingSelectionSystem.clearSelection();
            return;
        }

        boolean uiBlocking = HUD.isModalOpen();

        // If a modal UI is open, stop building placement/selection immediately.
        if (uiBlocking) {
            buildSystem.cancelPlacement();
            buildingSelectionSystem.clearSelection();

            // Keep world  running behind shop/help:
            EntityManager.updateEntities(dt);
            waveSystem.update(dt);

            return;
        }

        // Normal gameplay update
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

        boolean uiBlocking = HUD.isModalOpen();

        BeginMode2D(Camera.camera);

        // Draw background and grid lines
        World.draw();

        // Draw entities
        EntityManager.drawEntities();

        // Draw player
        player.draw();

        // Only draw building preview when no modal UI is open.
        if (!uiBlocking) {
            buildSystem.draw();
        }

        // Debug hitboxes
        //DrawRectangleLinesEx(Player.playerRec, 1.0f, RED);
        //DrawRectangleLinesEx(Player.miningRec, 1.0f, RED);

        EndMode2D();

        // Draw normal build menu only when no modal UI is open.
        // This prevents tooltips from appearing behind shop/help.
        if (!uiBlocking) {
            buildingSelectionSystem.drawUI();
        }

        buildMenu.draw(!uiBlocking);
        HUD.drawHUD();

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