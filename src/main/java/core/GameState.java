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
        MENU,
        PLAYING,
        GAME_OVER
    }

    private static State currentState = State.MENU;
    private static GameState instance;

    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;
    final private WaveSystem waveSystem;
    final private BuildingSelectionSystem buildingSelectionSystem;

    public GameState() {
        instance = this;

        TextureManager.init();
        SetTextureFilter(pixelFont.texture(), TEXTURE_FILTER_POINT);

        player = new Player();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();
        waveSystem = new WaveSystem();
        buildingSelectionSystem = new BuildingSelectionSystem(buildSystem);

        Camera.init();
        ResourceNode.init();
        WeaponManager.init();
    }

    public void update() {
        float dt = GetFrameTime();

        switch (currentState) {
            case MENU:
                MenuScreen.update();
                break;
            case PLAYING:
                updatePlaying(dt);
                break;
            case GAME_OVER:
                HUD.updateHUD();
                break;
        }
    }

    private void updatePlaying(float dt) {
        HUD.updateHUD();

        if (currentState == State.GAME_OVER) {
            buildSystem.cancelPlacement();
            buildingSelectionSystem.clearSelection();
            return;
        }

        boolean uiBlocking = HUD.isModalOpen();

        if (uiBlocking) {
            buildSystem.cancelPlacement();
            buildingSelectionSystem.clearSelection();
            EntityManager.updateEntities(dt);
            waveSystem.update(dt);
            return;
        }

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

        switch (currentState) {
            case MENU:
                MenuScreen.draw();
                break;
            case PLAYING:
                drawPlaying();
                break;
            case GAME_OVER:
                HUD.drawHUD();
                break;
        }

        EndDrawing();
    }

    private void drawPlaying() {
        boolean uiBlocking = HUD.isModalOpen();

        BeginMode2D(Camera.camera);

        World.draw();
        EntityManager.drawEntities();
        player.draw();

        if (!uiBlocking) {
            buildSystem.draw();
        }

        EndMode2D();

        if (!uiBlocking) {
            buildingSelectionSystem.drawUI();
        }

        buildMenu.draw(!uiBlocking);
        HUD.drawHUD();
        waveSystem.draw();
        waveSystem.drawDarknessOverlay();
    }

    public void reset() {
        currentState = State.PLAYING;

        EntityManager.reset();
        WeaponManager.reset();
        Camera.reset();
        HUD.reset();
        ResourceNode.reset();

        waveSystem.reset();
        player.reset();
        buildSystem.reset();
        buildingSelectionSystem.reset();

        EntityManager.stoneCenters.clear();
        EntityManager.placedBuildings.clear();
        EntityManager.spawnedEnemies.clear();
        EntityManager.towerBullets.clear();

        ResourceNode.init();
    }

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

    public static GameState getInstance() {
        return instance;
    }
}