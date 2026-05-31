package core;

import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

public class Camera {
    // Main 2D camera used to follow the player around the world
    public static Camera2D camera = new Camera2D();

    // Camera zoom level.
    // 1.0f means normal scale.
    public static final float ZOOM = 1.0f;

    public static void init() {
        // Keep the camera centered on the screen.
        // This makes the camera target appear in the middle of the window.
        camera.offset(newVector2(
                Main.SCREEN_WIDTH / 2f,
                Main.SCREEN_HEIGHT / 2f
        ));

        // Set the camera zoom level.
        camera.zoom(ZOOM);
    }

    public static void update(Vector2 position) {
        // Follow the given position
        camera.target(position);
    }

    public static void reset() {
        // Reset the camera target to the player's spawn position (center of world)
        camera.target(newVector2(World.WORLD_WIDTH / 2f, World.WORLD_HEIGHT / 2f));
    }
}
