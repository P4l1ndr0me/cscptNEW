package core;

import world.World;
import entities.*;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Colors.*;

public class GameState {

    final private Player player;
    final private World world;

    public GameState() {
        player = new Player(newVector2(World.worldWidth / 2f, World.worldHeight / 2f), 2.0f, 600.0f, 3, 3);
        world = new World();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time
        player.update(dt);
        Camera.update(player.getPosition());
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

        // draw player
        player.drawWalk();

        EndMode2D();

        // misc
        DrawText(GetFPS() + " FPS", 10, 690, 20, RED);
        DrawText(Math.floor(player.getPosition().x()) + ", " + Math.floor(player.getPosition().y()), 10, 10, 20, RED);
    }

    public void unload() {
        player.unload();
        world.unload();
    }
}
