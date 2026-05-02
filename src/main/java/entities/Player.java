package entities;

import core.TextureManager;
import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class Player extends Entity {
    public Player() {
        super(newVector2(World.worldWidth / 2f, World.worldHeight / 2f), 2.0f, 250.0f, TextureManager.getTexture("player"), 3, 3);
    }

    // draw walking animations for player sprite assuming 3x3 sprite
    public void drawWalk() {
        int frameWidth = texture.width() / frames;
        int frameHeight = texture.height() / rows;
        Rectangle source = new Rectangle()
                .x(currentFrame * frameWidth)
                .y(currentRow * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        Rectangle dest = new Rectangle()
                .x((int) (position.x() - (frameWidth * scale) / 2))
                .y((int) (position.y() - (frameHeight * scale) / 2))
                .width(frameWidth * scale)
                .height(frameHeight * scale);

        Vector2 origin = new Vector2().x(0).y(0);

        DrawTexturePro(texture, source, dest, origin, 0.0f, WHITE);
    }

    public void update(float dt) {
        boolean moving = false;
        float moveX = 0, moveY = 0;

        // Movement input
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

        // Set row of sprite sheet based on direction
        if (moveY < 0) currentRow = 0;  // up
        if (moveY > 0) currentRow = 1;  // down
        if (moveX < 0) currentRow = 2;  // left
        if (moveX > 0) currentRow = 1;  // right

        if (moveX == 0 && moveY == 0) {
            currentRow = 1;
        }

        //make sure going diagonally doesn't increase speed
        Vector2 moveDir = newVector2(moveX, moveY);
        if (Vector2Length(moveDir) != 0) {
            moveDir = Vector2Normalize(moveDir);
            moving = true;
        }

        position.x(Math.round(position.x() + speed * moveDir.x() * dt));
        position.y(Math.round(position.y() + speed * moveDir.y() * dt));

//        position.x(position.x() + speed * moveDir.x() * dt);
//        position.y(position.y() + speed * moveDir.y() * dt);

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
                currentFrame = (currentFrame + 1) % frames;
            }
        } else {
            currentFrame = 0;
        }
    }
}
