package entities;

import com.raylib.Raylib;
import world.World;

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

    public Entity (Vector2 position, float scale, float speed, Texture texture, int rows, int frames) {
        this.position = position;
        this.scale = scale;
        this.speed = speed;
        this.texture = texture;
        this.rows = rows;
        this.frames = frames;
    }

    public void draw () {
        float width = texture.width() * scale;
        float height = texture.height() * scale;
        Vector2 drawPos = newVector2(position.x() - (width/2), position.y() - (height/2));

        DrawTextureEx(texture, drawPos, rotation, scale, WHITE);
    }

    // draw walking animations for player sprite assuming 3x3 sprite
    public void drawWalk(){
        float frameWidth = (float) texture.width() / frames;
        float frameHeight = (float) texture.height() / rows;
        Raylib.Rectangle source = new Raylib.Rectangle()
                .x(currentFrame * frameWidth)
                .y(currentRow * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        Rectangle dest = new Rectangle()
                .x(position.x() - (frameWidth * scale) / 2)
                .y(position.y() - (frameHeight * scale) / 2)
                .width(frameWidth * scale)
                .height(frameHeight * scale);

        Vector2 origin = new Vector2().x(0).y(0);

        DrawTexturePro(texture, source, dest, origin, 0.0f, WHITE);

    }

    public void update (float dt) {
        boolean moving = false;
        float moveX = 0, moveY = 0;

        if (IsKeyDown(KEY_W)) {
            moveY -= 1;
        }
        if (IsKeyDown(KEY_S)) {
            moveY += 1;
        }
        if (IsKeyDown(KEY_A)) {
            moveX -= 1;
        }
        if (IsKeyDown(KEY_D)) {
            moveX += 1;
        }

        if (moveY < 0) currentRow = 0;       // up
        else if (moveY > 0) currentRow = 2;  // down
        else if (moveX < 0) currentRow = 2;  // left
        else if (moveX > 0) currentRow = 1;  // right


        //make sure going diagonally doesn't increase speed
        Vector2 moveDir = newVector2(moveX, moveY);
        if (Vector2Length(moveDir) != 0) {
            moveDir = Vector2Normalize(moveDir);
            moving = true;
        }

        position.x(position.x() + speed * moveDir.x() *dt);
        position.y(position.y() + speed * moveDir.y() * dt);

        // Make sure player does not go out of bounds
        float halfWidth = (texture.width() / (float) frames) * scale / 2;
        float halfHeight = (texture.height() / (float) rows) * scale / 2;

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

        if (moving) {
            frameTimer += dt;

            if (frameTimer >= frameSpeed) {
                frameTimer = 0;
                currentFrame++;

                if (currentFrame >= frames) {
                    currentFrame = 0;
                }
            }
        } else {
            currentFrame = 0;
        }
    }

    public Vector2 getPosition () {
        return position;
    }
}
