package entities;

import static com.raylib.Raylib.*;

public class Entity {
    Vector2 position;
    float rotation, scale;
    float speed;
    Texture texture;
    int rows;
    int frames;
    int health;

    int currentFrame;
    int currentRow;
    float frameTimer;
    float frameSpeed;

    public Entity(Vector2 position, float scale, float speed, Texture texture, int rows, int frames) {
        this.position = position;
        this.scale = scale;
        this.speed = speed;
        this.texture = texture;
        this.rows = rows;
        this.frames = frames;
    }

    public Vector2 getPosition() {
        return position;
    }
}
