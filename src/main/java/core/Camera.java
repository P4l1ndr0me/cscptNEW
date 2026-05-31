package core;

import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

public class Camera {
    public static Camera2D camera = new Camera2D();
    public static final float zoom = 1.0f;

    public static void init() {
        camera.offset(newVector2(Main.SCREEN_WIDTH / 2f, Main.SCREEN_HEIGHT / 2f));
        camera.zoom(zoom);
    }

    public static void reset() {
        // Reset camera target to origin (or where player spawns)
        // Since player spawns at WORLD_WIDTH/2, WORLD_HEIGHT/2, we set target to that
        camera.target(newVector2(World.WORLD_WIDTH / 2f, World.WORLD_HEIGHT / 2f));

        // Keep offset and zoom as they are (they don't change during gameplay)
        camera.offset(newVector2(Main.SCREEN_WIDTH / 2f, Main.SCREEN_HEIGHT / 2f));
        camera.zoom(zoom);
    }

    public static void update(Vector2 position) {
        camera.target(position);
    }
}
