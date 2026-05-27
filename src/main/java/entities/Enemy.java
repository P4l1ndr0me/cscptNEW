package entities;

import com.raylib.Raylib;
import world.World;
import buildings.Building;
import core.EntityManager;
import world.ResourceNode;
import hitboxes.enemyHitclass;

import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class Enemy extends Entity {

    // Hitbox - circular
    private final float hitRadius = 12;
    private enemyHitclass hitCircle;

    // Target offset for spreading out
    private float[] personalOffset = null;

    // Small separation force for zombies
    private final float SEPARATION_RADIUS = 20f;
    private final float SEPARATION_STRENGTH = 0.3f;

    // Stone avoidance
    private float stuckTimer = 0;
    private float[] rerouteDirection = null;
    private final float REROUTE_DURATION = 1.5f;

    // Animation
    private final float animSpeed = 0.20f;

    public Enemy(Raylib.Vector2 position, float scale, float speed, Raylib.Texture texture, int rows, int frames) {
        super(position, scale, speed, texture, rows, frames);
        hitCircle = new enemyHitclass(position.x(), position.y() + 12, hitRadius);
    }

    public void update(float dt) {
        updateHitCircle();

        // Find target building with random offset
        float[] target = determineClosestBuilding(position.x(), position.y());
        if (target == null) return;

        // Check if stuck on stone
        if (isStuckOnStone()) {
            stuckTimer += dt;
            if (stuckTimer >= REROUTE_DURATION) {
                stuckTimer = 0;
                rerouteDirection = calculateRerouteDirection(target);
            }
        } else {
            stuckTimer = 0;
            rerouteDirection = null;
        }

        // Calculate movement direction
        float moveX = 0, moveY = 0;

        if (rerouteDirection != null) {
            // Use reroute direction
            moveX = rerouteDirection[0];
            moveY = rerouteDirection[1];
        } else {
            // Normal movement toward target
            if (position.x() < target[0]) moveX = 1;
            if (position.x() > target[0]) moveX = -1;
            if (position.y() < target[1]) moveY = 1;
            if (position.y() > target[1]) moveY = -1;
        }

        // Add tiny separation force from other zombies
        float[] separation = calculateSmallSeparation();
        moveX += separation[0] * SEPARATION_STRENGTH;
        moveY += separation[1] * SEPARATION_STRENGTH;

        // Normalize diagonal movement
        float len = (float) Math.sqrt(moveX * moveX + moveY * moveY);
        if (len > 0) {
            moveX /= len;
            moveY /= len;
        }

        Raylib.Vector2 moveDir = newVector2(moveX, moveY);

        move(moveDir, dt);
        boundaryClamp();
        updateHitCircle();
        updateAnimation(dt, moveDir);
    }

    private boolean isStuckOnStone() {
        // Check if a stone is directly in front of the zombie
        float checkDistance = 25f;

        // Check in the direction the zombie is trying to go
        for (Raylib.Vector2 stoneCenter : EntityManager.stoneCenters) {
            float dx = stoneCenter.x() - position.x();
            float dy = stoneCenter.y() - position.y();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            // If stone is very close and directly blocking path
            if (dist < checkDistance && dist > 0) {
                return true;
            }
        }

        return false;
    }

    private float[] calculateRerouteDirection(float[] target) {
        // Try different directions to go around the stone
        float[][] possibleDirs = {
                {1, 0},   // right
                {-1, 0},  // left
                {0, 1},   // down
                {0, -1},  // up
                {1, 1},   // down-right
                {-1, 1},  // down-left
                {1, -1},  // up-right
                {-1, -1}  // up-left
        };

        // Find direction that doesn't have a stone blocking
        for (float[] dir : possibleDirs) {
            float checkX = position.x() + dir[0] * 30;
            float checkY = position.y() + dir[1] * 30;
            boolean blocked = false;

            for (Raylib.Vector2 stoneCenter : EntityManager.stoneCenters) {
                float dx = checkX - stoneCenter.x();
                float dy = checkY - stoneCenter.y();
                float dist = (float) Math.sqrt(dx * dx + dy * dy);

                if (dist < ResourceNode.stoneRadius + hitRadius + 10) {
                    blocked = true;
                    break;
                }
            }

            if (!blocked) {
                return dir;
            }
        }

        // If all directions blocked, just go around randomly
        return new float[]{(float)(Math.random() - 0.5), (float)(Math.random() - 0.5)};
    }

    private float[] calculateSmallSeparation() {
        float sepX = 0, sepY = 0;
        int count = 0;

        for (Enemy other : EntityManager.spawnedEnemies) {
            if (other == this) continue;

            float dx = position.x() - other.position.x();
            float dy = position.y() - other.position.y();
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < SEPARATION_RADIUS && dist > 0.01f) {
                float strength = (SEPARATION_RADIUS - dist) / SEPARATION_RADIUS;
                sepX += (dx / dist) * strength;
                sepY += (dy / dist) * strength;
                count++;
            }
        }

        if (count > 0) {
            sepX /= count;
            sepY /= count;

            float len = (float) Math.sqrt(sepX * sepX + sepY * sepY);
            if (len > 0) {
                sepX /= len;
                sepY /= len;
            }
        }

        return new float[]{sepX, sepY};
    }

    private void move(Raylib.Vector2 moveDir, float dt) {
        // --- X movement ---
        float nextX = position.x() + speed * moveDir.x() * dt;

        enemyHitclass nextHitX = new enemyHitclass(nextX, hitCircle.y, hitRadius);

        if (!collidesWithEverything(nextHitX)) {
            position.x(nextX);
        }

        // --- Y movement ---
        float nextY = position.y() + speed * moveDir.y() * dt;

        enemyHitclass nextHitY = new enemyHitclass(hitCircle.x, nextY + 12, hitRadius);

        if (!collidesWithEverything(nextHitY)) {
            position.y(nextY);
        }
    }

    // Check collisions (stones are obstacles)
    private boolean collidesWithEverything(enemyHitclass circle) {
        // Buildings
        for (Building b : EntityManager.placedBuildings) {
            if (CheckCollisionCircleRec(newVector2(circle.x, circle.y), circle.radius, b.getRect())) {
                return true;
            }
        }

        // Player
        if (Player.playerRec != null && CheckCollisionCircleRec(newVector2(circle.x, circle.y), circle.radius, Player.playerRec)) {
            return true;
        }

        // Stone nodes - NOW THEY BLOCK MOVEMENT
        for (Raylib.Vector2 stoneCenter : EntityManager.stoneCenters) {
            float dx = stoneCenter.x() - circle.x;
            float dy = stoneCenter.y() - circle.y;
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance < ResourceNode.stoneRadius + circle.radius) {
                return true;
            }
        }

        return false;
    }

    private void updateHitCircle() {
        hitCircle.x = position.x();
        hitCircle.y = position.y() + 12;
        hitCircle.radius = hitRadius;
    }

    private void updateAnimation(float dt, Raylib.Vector2 moveDir) {
        if (Vector2Length(moveDir) == 0) {
            currentRow = 0;
            frameTimer = 0;
            return;
        }

        // Direction
        if (moveDir.x() < 0) currentFrame = 0; // left
        if (moveDir.x() > 0) currentFrame = 2; // right
        if (moveDir.y() != 0) currentFrame = 1; // up/down share row

        // Frame cycling
        frameTimer += dt;
        if (frameTimer >= animSpeed) {
            frameTimer = 0;
            currentRow = (currentRow + 1) % rows;
        }
    }

    private void boundaryClamp() {
        float halfW = (texture.width() / (float) frames) * scale / 2;
        float halfH = (texture.height() / (float) rows) * scale / 2;

        if (position.x() < halfW) position.x(halfW);
        if (position.y() < halfH) position.y(halfH);
        if (position.x() > World.WORLD_WIDTH - halfW) position.x(World.WORLD_WIDTH - halfW);
        if (position.y() > World.WORLD_HEIGHT - halfH) position.y(World.WORLD_HEIGHT - halfH);
    }

    public float[] determineClosestBuilding(float curx, float cury) {
        Building closest = null;
        float closestDist = Float.MAX_VALUE;

        for (Building b : EntityManager.placedBuildings) {
            float dx = b.position.x() - curx;
            float dy = b.position.y() - cury;
            float dist = (float) Math.sqrt(dx * dx + dy * dy);

            if (dist < closestDist) {
                closestDist = dist;
                closest = b;
            }
        }

        if (closest != null) {
            // Generate persistent offset once per enemy
            if (personalOffset == null) {
                float randomX = (float)(Math.random() * 80 - 40);
                float randomY = (float)(Math.random() * 80 - 40);
                personalOffset = new float[]{randomX, randomY};
            }

            return new float[]{
                    closest.position.x() + 32 + personalOffset[0],
                    closest.position.y() + 32.0f + personalOffset[1]
            };
        }

        return null;
    }

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

        DrawTexturePro(texture, source, dest, newVector2(0, 0), 0.0f, WHITE);

        // Draw hitbox
        DrawCircleLines(
                (int) hitCircle.x,
                (int) hitCircle.y,
                (int) hitCircle.radius,
                RED
        );

        // Draw reroute debug info
        if (rerouteDirection != null) {
            DrawLine(
                    (int) position.x(),
                    (int) position.y(),
                    (int)(position.x() + rerouteDirection[0] * 30),
                    (int)(position.y() + rerouteDirection[1] * 30),
                    YELLOW
            );
        }
    }
}