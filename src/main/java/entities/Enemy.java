package entities;

import com.raylib.Raylib;
import world.World;

import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static com.raylib.Raylib.Vector2Normalize;

public class Enemy extends Entity{
    public Enemy(Raylib.Vector2 position, float scale, float speed, Raylib.Texture texture, int rows, int frames) {
        super(position, scale, speed, texture, rows, frames);
    }

    public static boolean onsetY = false;
    public static boolean onsetX = false;
    public final float slowspd = 0.34f;

    public void drawWalk() {
        int frameWidth = texture.width() / frames;
        int frameHeight = texture.height() / rows;
        Rectangle source = new Rectangle()
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

    public void update(float dt) {
        boolean moving = false;
        float moveX = 0, moveY = 0;
        boolean onsetX = false;
        boolean onsetY = false;

        if (position.x() < World.worldWidth / 2f +1  && position.x() > World.worldWidth / 2f-1 ) onsetX = true;
        if (position.y() < World.worldHeight / 2f +1 && position.y() > World.worldHeight / 2f -1) onsetY = true;


        // Movement input
        if (position.y() > World.worldHeight / 2f && onsetY == false) {
            moveY -= 1;
        }
        if (position.y() < World.worldHeight / 2f && onsetY == false ) {
            moveY += 1;
        }
        if (position.x() > World.worldWidth / 2f && onsetX == false) {
            moveX -= 1;
        }
        if (position.x() < World.worldWidth / 2f && onsetX == false) {
            moveX += 1;
        }


        // Set row of sprite sheet based on direction
        if (moveY < 0) currentRow = 0;  // up
        if (moveY > 0) currentRow = 0;  // down
        if (moveX < 0) currentRow = 1;  // left
        if (moveX > 0) currentRow = 2;  // right

        if (moveX == 0 && moveY == 0) {
            currentRow = 0;
        }

        //make sure going diagonally doesn't increase speed
        Vector2 moveDir = newVector2(moveX, moveY);
        if (Vector2Length(moveDir) != 0) {
            moveDir = Vector2Normalize(moveDir);
            moving = true;
        }

        position.x(position.x() + speed * moveDir.x() * dt);
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
            if (frameTimer >= slowspd) {
                frameTimer = 0;
                currentFrame = (currentFrame + 1) % frames;
            }
        } else {
            currentFrame = 0;
        }
    }
}