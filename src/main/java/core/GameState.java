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
    final private World world;
    final private InputHandler playerInput;
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

        // create new instances
        player = new Player();
        world = new World();
        playerInput = new InputHandler();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();
        ResourceNode resourceNode = new ResourceNode();

        // initialize camera
        Camera.init();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time

        // update
        player.update(dt);
        Camera.update(player.getPosition());
        buildSystem.update();
    }

    public void draw() {
        BeginDrawing();
        ClearBackground(GRAY);

        BeginMode2D(Camera.camera);

        // draw background and grid lines
        world.draw();

        // draw entities
        EntityManager.DrawEntities();

        // draw player
        player.drawWalk();

        // draw building preview
        buildSystem.drawPreview();

        EndMode2D();

        // draw UI & HUD
        buildMenu.drawUI();
        HUD.drawUI();

        // misc
        playerInput.drawMouseCoord();
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
        DrawFPS(Main.SCREEN_WIDTH - 75, 5);
        EndDrawing();
    }
}
