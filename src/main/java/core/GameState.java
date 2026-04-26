package core;

import world.World;
import entities.*;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Colors.*;

public class GameState {

    private Camera camera;
    private Player player;
    private World world;

    public GameState() {
        camera = new Camera(newVector2(World.worldWidth / 2, World.worldHeight / 2));
        player = new Player(newVector2(World.worldWidth / 2, World.worldHeight / 2), 2.0f, 300.0f, 3, 3);
        world = new World();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time
        player.update(dt);
        camera.followTarget(player.getPosition());
    }

    public void draw() {
        camera.beginDraw();
        world.drawBg();
        world.drawGrid(player.getPosition());
        player.drawWalk();
        camera.endDraw();
        DrawText(String.valueOf(GetFPS()) + " FPS", 10, 690, 20, RED);
        DrawText(String.valueOf(Math.floor(player.getPosition().x()) + ", " + Math.floor(player.getPosition().y())), 10, 10, 20, RED);
    }

    public void unload() {
        player.unload();
        world.unload();
    }
}
