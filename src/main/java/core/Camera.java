package core;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.newVector2;

public class Camera {
    private static final float SCREEN_WIDTH = 1280f;
    private static final float SCREEN_HEIGHT = 720f;

    private Camera2D camera2D;

    public Camera(float startX, float startY) {
        camera2D = new Camera2D();
        camera2D.zoom(1.0f);
        camera2D.offset(newVector2(SCREEN_WIDTH / 2, SCREEN_HEIGHT / 2));
        camera2D.target(newVector2(startX, startY));
    }

    public void followTarget(float targetX, float targetY) {
        camera2D.target(newVector2(targetX, targetY));
    }

    public void beginDraw() {
        BeginMode2D(camera2D);
    }

    public void endDraw() {
        EndMode2D();
    }
}
