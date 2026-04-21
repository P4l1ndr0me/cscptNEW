package core;

import world.World;
import entities.*;

import static com.raylib.Colors.WHITE;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;

public class GameState {

    private Camera camera;
    private Player player;

    Texture skin, background;

    public GameState() {
        // load textures
        skin = LoadTexture("src/main/assets/images/proportionalSprite1.png");
        background = LoadTexture("src/main/assets/images/background.png");

        camera = new Camera(newVector2(World.worldWidth / 2, World.worldHeight / 2));
        player = new Player(newVector2(World.worldWidth / 2, World.worldHeight / 2), 4.0f, 150.0f, skin, 3, 3);
    }

    public void update() {
        float dt = GetFrameTime(); // delta time
        player.update(dt);
        camera.followTarget(player.getPosition());
    }

    public void draw() {
        camera.beginDraw();
            DrawTextureEx(background, newVector2(0, 0), 0, World.worldWidth/background.width(), WHITE);
            player.drawWalk();
        camera.endDraw();
    }

    public void unload() {
        UnloadTexture(skin);
        UnloadTexture(background);
    }
}
