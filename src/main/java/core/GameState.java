package core;

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

    public GameState() {
        player = new Player(newVector2(World.worldWidth / 2f, World.worldHeight / 2f),
                2.0f,
                200.0f,
                3,
                3);
        world = new World();
        playerInput = new InputHandler();
        buildMenu = new BuildMenu();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time

        // update
        player.update(dt);
        Camera.update(player.getPosition());
        buildMenu.getClickedBuilding();
        buildMenu.update();
    }

    public void draw() {
        ClearBackground(GRAY);

        BeginMode2D(Camera.camera);

        // draw background
        world.drawBg();

        // draw grid lines
        world.drawGrid();

        // draw pregenerated stone
        world.drawStone();

        // draw preview
        buildMenu.drawPreview();

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

    public void unload() {
        player.unload();
        world.unload();
        buildMenu.unload();
    }
}
