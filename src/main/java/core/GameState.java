package core;

import world.World;
import entities.Player;
import static com.raylib.Raylib.*;

public class GameState {

    private Camera camera;
    private World world;
    private Player player;

    private final float HALF_WIDTH = GetScreenWidth() / 2;
    private final float HALF_HEIGHT = GetScreenHeight() / 2;

    float playerWorldX = World.worldWidth / 2;
    float playerWorldY = World.worldHeight / 2;

    Texture playerSprite = new Texture();

    public GameState() {
        world = new World();
        playerSprite = LoadTexture("src/main/Resources/Sprite1/spriteSheet.png");
        player = new Player(playerSprite, 3, 3, HALF_WIDTH, HALF_HEIGHT, 150f);
        camera = new Camera(playerWorldX, playerWorldY);
    }

    public void update(float dt) {
        float speed = 350f;
        if (IsKeyDown(KEY_W)) {
            playerWorldY -= speed * dt;
            camera.followTarget(playerWorldX, playerWorldY);
        }
        if (IsKeyDown(KEY_S)) {
            playerWorldY += speed * dt;
            camera.followTarget(playerWorldX, playerWorldY);
        }
        if (IsKeyDown(KEY_A)) {
            playerWorldX -= speed * dt;
            camera.followTarget(playerWorldX, playerWorldY);
        }
        if (IsKeyDown(KEY_D)) {
            playerWorldX += speed * dt;
            camera.followTarget(playerWorldX, playerWorldY);
        }

        // clamping
        playerWorldX = Math.max(HALF_WIDTH, Math.min(World.worldWidth - HALF_WIDTH, playerWorldX));
        playerWorldY = Math.max(HALF_HEIGHT, Math.min(World.worldHeight - HALF_HEIGHT, playerWorldY));
    }

    public void draw() {
        camera.beginDraw();
            world.drawGrid();
        camera.endDraw();
    }
}
