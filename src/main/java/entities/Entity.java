package entities;

import static com.raylib.Raylib.*;

public class Entity {
    Vector2 position;
    float scale;
    float speed;
    Texture texture;
    int rows;
    int cols;
    int health;

    int currentRow;
    int currentCol;
    float frameTimer = 0;
    float frameSpeed;

    public Entity(Vector2 position, float scale, float speed, Texture texture, int rows, int cols) {
        this.position = position;
        this.scale = scale;
        this.speed = speed;
        this.texture = texture;
        this.rows = rows;
        this.cols = cols;
    }

    public Vector2 getPosition() {
        return position;
    }
}
