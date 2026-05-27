package core;

import systems.BuildSystem;
import ui.HUD;
import world.ResourceNode;
import world.World;
import entities.*;
import ui.BuildMenu;

import java.util.ArrayList;

import static com.raylib.Helpers.newColor;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.EntityManager.spawnedEnemies;
import static world.World.WORLD_HEIGHT;
import static world.World.WORLD_WIDTH;
import systems.WaveSystem;

public class GameState {
    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;
    final private WaveSystem waveSystem;

    public GameState() {
        TextureManager.init();

        // Create new instances
        player = new Player();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();
        waveSystem = new WaveSystem();
      
        // initialize
        Camera.init();
        ResourceNode.init();
        WeaponManager.init();
    }

    public void update() {
        float dt = GetFrameTime(); // get delta time (time since last frame)

        // Update
        player.update(dt);
        for (Enemy spawnedEnemy : spawnedEnemies) {
            spawnedEnemy.update(dt);
        }
        Camera.update(player.getPosition());
        buildSystem.update();
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
        buildMenu.drawUI();
        HUD.drawHUD();
        HUD.updateHUD();
        waveSystem.draw();
        // Misc

        // Draw mouse position
        Vector2 mousePos = GetMousePosition();
        GetMousePosition().close();
        DrawText("Mouse XY: " + (int) mousePos.x() + ", " + (int) mousePos.y(), 5, Main.SCREEN_HEIGHT - 25, 20, BLUE);

        // Draw player position
        DrawText("X: " + (int) Math.floor(player.getPosition().x()),
                5,
                5,
                20,
                BLUE);
        DrawText("Y: " + (int) Math.floor(player.getPosition().y()),
                5,
                25,
                20,
                BLUE);

        // Draw FPS
        DrawFPS(Main.SCREEN_WIDTH - 75, 5);

        waveSystem.drawDarknessOverlay();

        EndDrawing();
    }
}
