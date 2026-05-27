package hitboxes;

import com.raylib.Colors;
import com.raylib.Raylib;
import com.raylib.Raylib.Color;

import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class enemyHitclass {
    public float x;
    public float y;
    public float radius;

    // Constructor to create the object instance
    public enemyHitclass(float x, float y, float radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    public Vector2 getCenter() {
        return newVector2(x, y);
    }
}