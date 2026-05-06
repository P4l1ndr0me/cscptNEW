package core;

import systems.BuildSystem;
import ui.HUD;
import world.ResourceNode;
import world.World;
import entities.*;
import ui.BuildMenu;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class GameState {
    final private Player player;
    final private BuildSystem buildSystem;
    final private BuildMenu buildMenu;

    public GameState() {
        // Load all textures
        TextureManager.loadTexture("player", "src/main/assets/images/playerSpriteNEW.png");
        TextureManager.loadTexture("background", "src/main/assets/images/bgNew.png");
        TextureManager.loadTexture("stone", "src/main/assets/images/stoneNEW.png");

        TextureManager.loadTexture("building1", "src/main/assets/images/buildings/building1.png");
        TextureManager.loadTexture("building2", "src/main/assets/images/buildings/building2.png");
        TextureManager.loadTexture("building3", "src/main/assets/images/buildings/building3.png");

        // Create new instances
        player = new Player();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();

        // Initialize camera and resource node
        Camera.init();
        ResourceNode.init();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time

        // Update
        player.update(dt);
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
        player.drawWalk();

        // Draw building preview
        buildSystem.drawPreview();

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
}
