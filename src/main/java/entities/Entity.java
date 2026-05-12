package entities;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class Entity {
    Vector2 position;
    float rotation, scale;
    float speed;
    Texture texture;
    int rows;
    int frames;

    int currentFrame;
    int currentRow;
    float frameTimer;
    float frameSpeed = 0.15f;

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
