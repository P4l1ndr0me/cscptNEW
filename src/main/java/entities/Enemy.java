package entities;

import com.raylib.Raylib;
import world.World;
import buildings.Building;
import core.EntityManager;

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
        onsetX = false;
        onsetY = false;

        float[] target = determineClosestBuilding(position.x(), position.y());

        if (target == null) {
            // No buildings to target, maybe wander randomly
            return;
        }

//        float randomOffsetX = (float) (Math.random() * 100 - 50); // -50 to +50 pixels
//        float randomOffsetY = (float) (Math.random() * 100 - 50);
//        float targetX = target[0] + randomOffsetX;
//        float targetY = target[1] + randomOffsetY;

        if (position.x() < target[0] +1  && position.x() > target[0] -1 ) onsetX = true;
        if (position.y() < target[1] +1 && position.y() > target[1] -1) onsetY = true;


        // Movement input
        if (position.y() > target[1] && !onsetY) {
            moveY -= 1;
        }
        if (position.y() < target[1] && !onsetY) {
            moveY += 1;
        }
        if (position.x() > target[0] && !onsetX) {
            moveX -= 1;
        }
        if (position.x() < target[0] && !onsetX) {
            moveX += 1;
        }


        // Set row of sprite sheet based on direction
        if (moveY < 0) currentFrame = 1;  // up
        if (moveY > 0) currentFrame = 1;  // down
        if (moveX < 0) currentFrame = 0;  // left
        if (moveX > 0) currentFrame = 2;  // right

        if (moveX == 0 && moveY == 0) {
            currentFrame = 1;
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
        if (position.x() > World.WORLD_WIDTH - halfWidth) {
            position.x(World.WORLD_WIDTH - halfWidth);
        }
        if (position.y() > World.WORLD_HEIGHT - halfHeight) {
            position.y(World.WORLD_HEIGHT - halfHeight);
        }

        if (moving) {
            frameTimer += dt;
            if (frameTimer >= slowspd) {
                frameTimer = 0;
                currentRow = (currentRow + 1) % rows;
            }
        } else {
            currentRow = 0;
        }
    }

    public float[] determineClosestBuilding(float curx, float cury){
        Building closestBuilding = null;
        float closestDistance = Float.MAX_VALUE;

        for (Building building: EntityManager.placedBuildings){
            float buildingX = building.position.x();
            float buildingY = building.position.y();

            // Calculate distance between enemy and building
            float dx = buildingX - curx;
            float dy = buildingY - cury;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestBuilding = building;
            }
        }
        // Return the position of the closest building, or null if no buildings exist
        if (closestBuilding != null) {
            return new float[]{ closestBuilding.position.x(), closestBuilding.position.y()};
        }

        return null; // No buildings found
    }
}