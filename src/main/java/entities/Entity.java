package entities;

import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;
import static com.raylib.Helpers.newVector2;

public class Entity {
    Vector2 position;
    float rotation, scale;
    float speed;
    Texture texture;

    public Entity (Vector2 position, float scale, float speed, Texture texture) {
        this.position = position;
        this.scale = scale;
        this.speed = speed;
        this.texture = texture;
    }

    public void draw () {
        float width = texture.width() * scale;
        float height = texture.height() * scale;
        Vector2 drawPos = newVector2(position.x() - (width/2), position.y() - (height/2));

        DrawTextureEx(texture, drawPos, rotation, scale, WHITE);
    }

    public void update (float dt) {
        float halfWidth = (texture.width() * scale) / 2;
        float halfHeight = (texture.height() * scale) / 2;

        float moveX = (IsKeyDown(KEY_D) ? 1.0f : 0.0f) - (IsKeyDown(KEY_A) ? 1.0f : 0.0f);
        float moveY = (IsKeyDown(KEY_S) ? 1.0f : 0.0f) - (IsKeyDown(KEY_W) ? 1.0f : 0.0f);
        Vector2 moveDir = newVector2(moveX, moveY);

        // make sure going diagonally doesn't increase speed
        if (Vector2Length(moveDir) > 0) {
            moveDir = Vector2Normalize(moveDir);
        }

        position.x(position.x() + speed * moveDir.x() *dt);
        position.y(position.y() + speed * moveDir.y() * dt);
//        if (IsKeyDown(KEY_W)) {
//            position.y(position.y() - speed * dt);
//        }
//        if (IsKeyDown(KEY_S)) {
//            position.y(position.y() + speed * dt);
//        }
//        if (IsKeyDown(KEY_A)) {
//            position.x(position.x() - speed * dt);
//        }
//        if (IsKeyDown(KEY_D)) {
//            position.x(position.x() + speed * dt);
//        }

        // Make sure player does not go out of bounds
        if (position.x() < halfWidth) {
            position.x(halfWidth);
        }
        if (position.y() < halfHeight) {
            position.y(halfHeight);
        }
        if (position.x() > World.worldWidth - halfWidth) {
            position.x(World.worldWidth - halfWidth);
        }
        if (position.y() > World.worldHeight - halfHeight) {
            position.y(World.worldHeight - halfHeight);
        }
    }

    public Vector2 getPosition () {
        return position;
    }
}
