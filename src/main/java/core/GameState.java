package core;

import systems.BuildSystem;
import world.World;
import entities.*;
import ui.BuildMenu;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class GameState {

    final private Player player;
    final private World world;
    final private InputHandler playerInput;
    final private BuildMenu buildMenu;
    final private BuildSystem buildSystem;

    public GameState() {
        // Load all textures
        TextureManager.loadTexture("player", "src/main/assets/images/player.png");
        TextureManager.loadTexture("background", "src/main/assets/images/bgNew.png");
        TextureManager.loadTexture("stone", "src/main/assets/images/stone.png");

        TextureManager.loadTexture("building1", "src/main/assets/images/buildings/building1.png");
        TextureManager.loadTexture("building2", "src/main/assets/images/buildings/building2.png");
        TextureManager.loadTexture("building3", "src/main/assets/images/buildings/building3.png");

        player = new Player(newVector2(World.worldWidth / 2f, World.worldHeight / 2f),
                2.0f,
                200.0f,
                3,
                3);
        world = new World();
        playerInput = new InputHandler();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time

        // update
        player.update(dt);
        Camera.update(player.getPosition());
        buildSystem.getClickedBuilding();
        buildSystem.update();
    }

    public void draw() {
        ClearBackground(GRAY);

        BeginMode2D(Camera.camera);

        // draw background and grid lines
        world.draw();

        // draw pregenerated stone
        world.drawStone();

        // draw building preview
        buildSystem.drawPreview();

        // draw entities
        EntityManager.DrawEntities();

        // draw player
        player.drawWalk();

        EndMode2D();

        // draw UI & HUD
        buildMenu.drawUI();

        // misc
        playerInput.drawMouseCoord();
        DrawText(Math.floor(player.getPosition().x()) + ", " + Math.floor(player.getPosition().y()),
                10,
                10,
                20,
                BLUE);
    }
}
