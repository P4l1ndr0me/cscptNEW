package core;

import systems.BuildSystem;
import ui.HUD;
import world.ResourceNode;
import world.World;
import entities.*;
import ui.BuildMenu;
import core.EntityManager;

import java.util.ArrayList;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static core.EntityManager.spawnedEnemies;
import static world.World.worldHeight;
import static world.World.worldWidth;

public class GameState {
    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;

    public static ArrayList<int[]> zombieSpawnPoint = new ArrayList<>();

    public GameState() {
        // Load all textures

        // Map-related
        TextureManager.loadTexture("background", "src/main/assets/images/map/bg.png");
        TextureManager.loadTexture("stone", "src/main/assets/images/map/stone.png");

        // Player-related
        TextureManager.loadTexture("mining", "src/main/assets/images/player/mining.png");
        TextureManager.loadTexture("playerNEW", "src/main/assets/images/player/playerSpriteNEW.png");

        // Building-related
        TextureManager.loadTexture("building1", "src/main/assets/images/buildings/building1.png");
        TextureManager.loadTexture("building2", "src/main/assets/images/buildings/building2.png");
        TextureManager.loadTexture("building3", "src/main/assets/images/buildings/building3.png");

        TextureManager.loadTexture("enemy1", "src/main/assets/images/sprites/ZOMBIE1.png");
        TextureManager.loadTexture("enemy2", "src/main/assets/images/sprites/redZombie.png");

        // create new instances
        player = new Player();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();

        for(int i =0;i<10;i++){
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

        for (int i =0;i<3;i++){

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


        // initialize camera
        Camera.init();
        ResourceNode.init();
    }

    public void update() {
        float dt = GetFrameTime(); // get delta time (time since last frame)

        // Update
        player.update(dt);
        for (int i =0;i<spawnedEnemies.size();i++){
            spawnedEnemies.get(i).update(dt);
        }
        Camera.update(player.getPosition());
        buildSystem.update();
    }

    public void draw() {
        BeginDrawing();
        ClearBackground(GRAY);

        BeginMode2D(Camera.camera);

        // Draw background and grid lines
        World.draw();

        // Draw entities
        EntityManager.DrawEntities();

        // Draw player
        player.draw();

        // Draw building preview
        buildSystem.drawPreview();

        // Hitboxes (debugging)
        for (Rectangle stoneRect : EntityManager.stoneRects) {
            DrawRectangleLinesEx(stoneRect, 1.0f, RED);
        }
        DrawRectangleLinesEx(Player.playerRec, 1.0f, RED);
        DrawRectangleLinesEx(Player.miningRec, 1.0f, RED);
        EndMode2D();

        // Draw UI & HUD
        buildMenu.drawUI();
        HUD.drawHUD();

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

        // Draw fps
        DrawFPS(Main.SCREEN_WIDTH - 75, 5);

        EndDrawing();
    }

    public int[] getSpawnPosition() {
        int playerX = worldWidth / 2;
        int playerY = worldHeight / 2;
        int safeRadius = 500;

        double angle = Math.random() * 2 * Math.PI;
        double maxDist = Math.min(worldWidth, worldHeight) / 2.0;
        double distance = safeRadius + Math.random() * (maxDist - safeRadius);

        int x = (int)(playerX + distance * Math.cos(angle));
        int y = (int)(playerY + distance * Math.sin(angle));

        // clamp to map bounds
        x = Math.max(0, Math.min(worldWidth - 1, x));
        y = Math.max(0, Math.min(worldHeight - 1, y));
        zombieSpawnPoint.add(new int[] {x, y});

        return new int[]{x, y};
    }
}
