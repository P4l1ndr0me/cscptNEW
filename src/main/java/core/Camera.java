package core;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;

public class Camera {
    public static Camera2D camera = new Camera2D();
    static float zoom = 1.0f;

    public static void update(Vector2 position) {
        // lerp camera for smooth effect (in progress)
//        float lerpFactor = 0.1f;
//
//        camera2D.target().x(camera2D.target().x() + (targetPosition.x() - camera2D.target().x()) * lerpFactor);
//        camera2D.target().y(camera2D.target().y() + (targetPosition.y() - camera2D.target().y()) * lerpFactor
//        );

        camera.offset(newVector2(Main.screen_width / 2f, Main.screen_height / 2f));

        camera.target(position);

        float wheel = GetMouseWheelMove();
        if (wheel != 0) {
            zoom += wheel * 0.02f;
            if (zoom < 0.5f) zoom = 0.5f; // clamp min
            if (zoom > 2.0f) zoom = 2.0f; // clamp max
        }
        camera.zoom(zoom);
    }
}
