package core;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;

public class Camera {
    private final Camera2D camera2D;

    public Camera(Vector2 startPosition) {
        camera2D = new Camera2D();
        camera2D.zoom(1.0f);
        camera2D.offset(newVector2(Main.screen_width / 2f, Main.screen_height / 2f));
        camera2D.target(startPosition);
    }

    public void followTarget(Vector2 targetPosition) {
        // lerp camera for smooth effect (in progress)
//        float lerpFactor = 0.1f;
//
//        camera2D.target().x(camera2D.target().x() + (targetPosition.x() - camera2D.target().x()) * lerpFactor);
//        camera2D.target().y(camera2D.target().y() + (targetPosition.y() - camera2D.target().y()) * lerpFactor
//        );

        camera2D.target(targetPosition);
    }

    public void beginDraw() {
        BeginMode2D(camera2D);
    }

    public void endDraw() {
        EndMode2D();
    }
}
