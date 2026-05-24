package core;

import systems.BuildSystem;
import ui.HUD;
import world.ResourceNode;
import world.World;
import entities.*;
import ui.BuildMenu;

import java.util.ArrayList;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.EntityManager.spawnedEnemies;
import static world.World.WORLD_HEIGHT;
import static world.World.WORLD_WIDTH;

public class GameState {
    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;

    public static ArrayList<int[]> zombieSpawnPoint = new ArrayList<>();

    public GameState() {
        TextureManager.init();

        // Create new instances
        player = new Player();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();

        for (int i = 0; i < 10; i++) {
            int[] pos = getSpawnPosition();
            EntityManager.spawnZombie
                    (pos[0],
                            pos[1],
                            2.0f,
                            50.0f,
                            TextureManager.getTexture("enemy1"),
                            3,
                            3);
        }

        for (int i = 0; i < 3; i++) {

            int[] pos = getSpawnPosition();

            EntityManager.spawnZombie
                    (pos[0],
                            pos[1],
                            2.0f,
                            75.0f,
                            TextureManager.getTexture("enemy2"),
                            3,
                            3);
        }
      
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
        HUD.updateHUD();

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

        EndDrawing();
    }

    public int[] getSpawnPosition() {
        int playerX = WORLD_WIDTH / 2;
        int playerY = WORLD_HEIGHT / 2;
        int safeRadius = 500;

        double angle = Math.random() * 2 * Math.PI;
        double maxDist = Math.min(WORLD_WIDTH, WORLD_HEIGHT) / 2.0;
        double distance = safeRadius + Math.random() * (maxDist - safeRadius);

        int x = (int) (playerX + distance * Math.cos(angle));
        int y = (int) (playerY + distance * Math.sin(angle));

        // clamp to map bounds
        x = Math.max(0, Math.min(WORLD_WIDTH - 1, x));
        y = Math.max(0, Math.min(WORLD_HEIGHT - 1, y));
        zombieSpawnPoint.add(new int[]{x, y});

        return new int[]{x, y};
    }
}
