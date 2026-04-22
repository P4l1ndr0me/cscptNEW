package core;

import world.World;
import entities.*;

import static com.raylib.Colors.WHITE;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;

public class GameState {

    private Camera camera;
    private Player player;
    private World world;

    Texture skin;

    public GameState() {
        // load textures
        skin = LoadTexture("src/main/assets/images/proportionalSprite1.png");

        camera = new Camera(newVector2(World.worldWidth / 2, World.worldHeight / 2));
        player = new Player(newVector2(World.worldWidth / 2, World.worldHeight / 2), 4.0f, 150.0f, skin, 3, 3);
        world = new World();
    }

    public void update() {
        float dt = GetFrameTime(); // delta time
        player.update(dt);
        camera.followTarget(player.getPosition());
    }

    public void draw() {
        camera.beginDraw();
            world.drawWorld();
            player.drawWalk();
        camera.endDraw();
    }

    public void unload() {
        UnloadTexture(skin);
        world.unload();
    }
}
