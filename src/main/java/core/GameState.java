package core;

import systems.BuildSystem;
import world.World;
import entities.*;
import ui.BuildMenu;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class GameState {

    RenderTexture target;

    final private Player player;
    final private World world;
    final private InputHandler playerInput;
    final private BuildMenu buildMenu;
    final private BuildSystem buildSystem;

    public GameState() {
        target = LoadRenderTexture(Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);

        // Load all textures
        TextureManager.loadTexture("player", "src/main/assets/images/playerSpriteNEW.png");
        TextureManager.loadTexture("background", "src/main/assets/images/bgNew.png");
        TextureManager.loadTexture("stone", "src/main/assets/images/stone.png");

        TextureManager.loadTexture("building1", "src/main/assets/images/buildings/building1.png");
        TextureManager.loadTexture("building2", "src/main/assets/images/buildings/building2.png");
        TextureManager.loadTexture("building3", "src/main/assets/images/buildings/building3.png");

        // create new instances
        player = new Player();
        world = new World();
        playerInput = new InputHandler();
        buildMenu = new BuildMenu();
        buildSystem = new BuildSystem();

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
        BeginTextureMode(target);
            ClearBackground(GRAY);

            BeginMode2D(Camera.camera);

            // draw background and grid lines
            world.draw();

            // draw pregenerated stone
            world.drawStone();

            // draw entities
            EntityManager.DrawEntities();

            // draw building preview
            buildSystem.draw();

            // draw player
            player.drawWalk();

            EndMode2D();

            // draw UI & HUD
            buildMenu.drawUI();

            // misc
            playerInput.drawMouseCoord();
            DrawText("X: " + (int) Math.floor(player.getPosition().x()),
                    10,
                    10,
                    20,
                    BLUE);
            DrawText("Y: " + (int) Math.floor(player.getPosition().y()),
                    10,
                    30,
                    20,
                    BLUE);

        EndTextureMode();

        BeginDrawing();
            ClearBackground(GRAY);
            Rectangle source = newRectangle(0, 0, (float)target.texture().width(), (float)-target.texture().height());
            Rectangle dest = newRectangle(0, 0, Main.SCREEN_WIDTH, Main.SCREEN_HEIGHT);
            DrawTexturePro(target.texture(), source, dest, newVector2(0, 0), 0.0f, WHITE);
        EndDrawing();
    }
}
