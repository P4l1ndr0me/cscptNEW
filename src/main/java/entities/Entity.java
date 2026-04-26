package entities;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static com.raylib.Helpers.newVector2;

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
    float frameSpeed = 0.12f;

    public Entity(Vector2 position, float scale, float speed, Texture texture, int rows, int frames) {
        this.position = position;
        this.scale = scale;
        this.speed = speed;
        this.texture = texture;
        this.rows = rows;
        this.frames = frames;
    }

    public void draw() {
        float width = texture.width() * scale;
        float height = texture.height() * scale;
        Vector2 drawPos = newVector2(position.x() - (width / 2), position.y() - (height / 2));

        DrawTextureEx(texture, drawPos, rotation, scale, WHITE);
    }

    public Vector2 getPosition() {
        return position;
    }

    public void unload() {

    }
}
