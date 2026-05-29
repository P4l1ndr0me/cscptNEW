package entities;

import buildings.Building;
import core.EntityManager;
import systems.BuildSystem;
import world.World;

import static com.raylib.Helpers.*;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class Enemy extends Entity {

    // Hitbox
    private final float hitRadius = 12f;
    private float hitOffsetX;
    private float hitOffsetY;

    // Movement
    private final float separationRadius = 24f;
    private final float separationStrength = 0.35f;

    // Animation
    private final float animSpeed = 0.20f;

    // Combat
    private int damage = 10;
    private int health = 100;
    private final float attackRange = 2f;
    private float attackCooldown = 1.0f;
    private float attackTimer = 0f;
    private Building targetBuilding = null;

    // Debug
    private final boolean showDebugHitbox = true;

    public Enemy(Vector2 position, float scale, float speed, Texture texture, int rows, int frames) {
        super(position, scale, speed, texture, rows, frames);

        currentFrame = 1;
        currentRow = 0;
    }

    public void update(float dt) {
        targetBuilding = getBuildingInAttackRange();

        if (targetBuilding != null) {
            attackBuilding(dt);
            updateAnimation(dt, newVector2(0, 0));
            //return;
        }

        Building target = getTargetBuilding();

        if (target == null) {
            updateAnimation(dt, newVector2(0, 0));
            return;
        }

        Vector2 moveDir = getDirectionToTarget(target);

        Vector2 separation = getSeparationForce();
        moveDir.x(moveDir.x() + separation.x() * separationStrength);
        moveDir.y(moveDir.y() + separation.y() * separationStrength);

        if (Vector2Length(moveDir) > 0) {
            moveDir = Vector2Normalize(moveDir);
        }

        move(moveDir, dt);
        boundaryClamp();
        updateAnimation(dt, moveDir);
    }

    private Building getBuildingInAttackRange() {
        Vector2 center = getHitCenter();

        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionCircleRec(
                    center,
                    hitRadius + attackRange,
                    building.getRect()
            )) {
                return building;
            }
        }

        return null;
    }

    private void attackBuilding(float dt) {
        if (targetBuilding == null) {
            return;
        }

        attackTimer += dt;

        if (attackTimer >= attackCooldown) {
            attackTimer = 0f;

            targetBuilding.health -= damage;

            if (targetBuilding.health <= 0) {
                destroyBuilding(targetBuilding);
                targetBuilding = null;
            }
        }
    }

    private void destroyBuilding(Building building) {
        EntityManager.placedBuildings.remove(building);
    }

    private Building getTargetBuilding() {
        // Main zombie target should be the Gold Stash
        Building goldStash = BuildSystem.getGoldStash();

        if (goldStash != null) {
            return goldStash;
        }

        return null;
    }

    private Vector2 getDirectionToTarget(Building target) {
        float targetCenterX = target.position.x() + Building.size / 2f;
        float targetCenterY = target.position.y() + Building.size / 2f;

        float dx = targetCenterX - position.x();
        float dy = targetCenterY - position.y();

        Vector2 direction = newVector2(dx, dy);

        if (Vector2Length(direction) > 0) {
            direction = Vector2Normalize(direction);
        }

        return direction;
    }

    private Vector2 getSeparationForce() {
        float sepX = 0;
        float sepY = 0;
        int count = 0;

        for (Enemy other : EntityManager.spawnedEnemies) {
            if (other == this) {
                continue;
            }

            float dx = position.x() - other.position.x();
            float dy = position.y() - other.position.y();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 0.01f && distance < separationRadius) {
                float strength = (separationRadius - distance) / separationRadius;

                sepX += (dx / distance) * strength;
                sepY += (dy / distance) * strength;
                count++;
            }
        }

        if (count > 0) {
            sepX /= count;
            sepY /= count;
        }

        Vector2 separation = newVector2(sepX, sepY);

        if (Vector2Length(separation) > 0) {
            separation = Vector2Normalize(separation);
        }

        return separation;
    }

    private void move(Vector2 moveDir, float dt) {
        if (Vector2Length(moveDir) == 0) {
            return;
        }

        // Try X movement first
        float nextX = position.x() + moveDir.x() * speed * dt;

        if (!collidesWithBuilding(nextX, position.y())) {
            position.x(nextX);
        }

        // Then try Y movement
        float nextY = position.y() + moveDir.y() * speed * dt;

        if (!collidesWithBuilding(position.x(), nextY)) {
            position.y(nextY);
        }
    }

    private boolean collidesWithBuilding(float testX, float testY) {
        Vector2 testCenter = newVector2(testX + hitOffsetX, testY + hitOffsetY);

        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionCircleRec(testCenter, hitRadius, building.getRect())) {
                return true;
            }
        }

        return false;
    }

    private void boundaryClamp() {
        float frameWidth = texture.width() / (float) frames;
        float frameHeight = texture.height() / (float) rows;

        float halfW = frameWidth * scale / 2f;
        float halfH = frameHeight * scale / 2f;

        if (position.x() < halfW) {
            position.x(halfW);
        }

        if (position.y() < halfH) {
            position.y(halfH);
        }

        if (position.x() > World.WORLD_WIDTH - halfW) {
            position.x(World.WORLD_WIDTH - halfW);
        }

        if (position.y() > World.WORLD_HEIGHT - halfH) {
            position.y(World.WORLD_HEIGHT - halfH);
        }
    }

    private void updateAnimation(float dt, Vector2 moveDir) {
        if (Vector2Length(moveDir) == 0) {
            frameTimer = 0;
            return;
        }

        // Choose animation column based on direction
        if (Math.abs(moveDir.x()) > Math.abs(moveDir.y())) {
            if (moveDir.x() < 0) {
                currentFrame = 0; // left
                hitOffsetX = 7f;
                hitOffsetY = 9f;
            } else {
                currentFrame = 2; // right
                hitOffsetX = -6f;
                hitOffsetY = 9f;
            }
        } else {
            currentFrame = 1; // up/down
            hitOffsetX = 1f;
            hitOffsetY = 9f;
        }

        // Cycle rows for walking animation
        frameTimer += dt;

        if (frameTimer >= animSpeed) {
            frameTimer = 0;
            currentRow = (currentRow + 1) % rows;
        }
    }

    public void draw() {
        int frameWidth = texture.width() / frames;
        int frameHeight = texture.height() / rows;

        Rectangle source = newRectangle(
                currentFrame * frameWidth,
                currentRow * frameHeight,
                frameWidth,
                frameHeight
        );

        Rectangle dest = newRectangle(
                position.x() - frameWidth * scale / 2f,
                position.y() - frameHeight * scale / 2f,
                frameWidth * scale,
                frameHeight * scale
        );

        DrawTexturePro(
                texture,
                source,
                dest,
                newVector2(0, 0),
                0.0f,
                WHITE
        );

        if (showDebugHitbox) {
            DrawCircleLines(
                    (int) (position.x() + hitOffsetX),
                    (int) (position.y() + hitOffsetY),
                    hitRadius,
                    RED
            );
        }
    }

    public void takeDamage(int amount) {
        health -= amount;
    }

    public boolean isDead() {
        return health <= 0;
    }

    public void applyKnockback(Vector2 direction, float strength) {
        if (Vector2Length(direction) == 0) {
            return;
        }

        direction = Vector2Normalize(direction);

        float nextX = position.x() + direction.x() * strength;
        float nextY = position.y() + direction.y() * strength;

        if (!collidesWithBuilding(nextX, position.y())) {
            position.x(nextX);
        }

        if (!collidesWithBuilding(position.x(), nextY)) {
            position.y(nextY);
        }

        boundaryClamp();
    }

    public Vector2 getHitCenter() {
        return newVector2(position.x() + hitOffsetX, position.y() + hitOffsetY);
    }

    public float getHitRadius() {
        return hitRadius;
    }
}