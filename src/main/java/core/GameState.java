package core;

import systems.*;
import ui.*;
import world.*;
import entities.*;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.EntityManager.spawnedEnemies;
import static core.Main.pixelFont;
import systems.WaveSystem;

public class GameState {
    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;
    final private WaveSystem waveSystem;
    final private BuildingSelectionSystem buildingSelectionSystem;

    public GameState() {
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
        float dt = GetFrameTime(); // get delta time (time since last frame)

        player.update(dt);

        Camera.update(player.getPosition());

        buildSystem.update();

        buildingSelectionSystem.update();

        EntityManager.updateEntities(dt);

        waveSystem.update(dt);

        HUD.updateHUD();
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
}
