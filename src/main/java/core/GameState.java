package core;

import world.World;
import static com.raylib.Colors.RAYWHITE;
import static com.raylib.Raylib.*;

public class GameState {

    private Camera camera;
    private World world;
    float playerX = World.worldWidth / 2;
    float playerY = World.worldHeight / 2;

    public GameState() {
        world = new World();
        camera = new Camera(playerX, playerY);
    }

    public void update(float dt) {
        float speed = 300f*dt;
        if (IsKeyDown(KEY_W)) {
            playerY -= speed;
            camera.followTarget(playerX, playerY);
        }
        if (IsKeyDown(KEY_S)) {
            playerY += speed;
            camera.followTarget(playerX, playerY);
        }
        if (IsKeyDown(KEY_A)) {
            playerX -= speed;
            camera.followTarget(playerX, playerY);
        }
        if (IsKeyDown(KEY_D)) {
            playerX += speed;
            camera.followTarget(playerX, playerY);
        }
    }

    public void draw() {
        camera.beginDraw();
            world.drawGrid();
        camera.endDraw();
    }
}
