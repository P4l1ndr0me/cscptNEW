package core;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;

public class Camera {
    public static Camera2D camera = new Camera2D();
    public static final float zoom = 1.0f;

    public static void init() {
        camera.offset(newVector2(Main.SCREEN_WIDTH / 2f, Main.SCREEN_HEIGHT / 2f));
        camera.zoom(zoom);
    }

    public static void update(Vector2 position) {
        camera.target(position);
    }
}
