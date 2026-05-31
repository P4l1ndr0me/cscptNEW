package entities;

import buildings.Building;
import core.EntityManager;
import systems.BuildSystem;
import world.ResourceNode;
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
    private final float separationRadius = 30f;
    private final float separationStrength = 0.45f;

    // Animation
    private final float animSpeed = 0.40f;
    private int animationFrame = 0;
    private boolean isAttacking = false;
    private boolean wasAttacking = false;

    // Combat
    private int damage;
    private final float attackRange = 5f;
    private float attackCooldown = 1.0f;
    private float attackTimer = 0f;
    private Building targetBuilding = null;
    private int goldDrop;

    // Debug
    private final boolean showDebugHitbox = true;

    public Enemy(
            Vector2 position,
            float scale,
            float speed,
            Texture texture,
            int rows,
            int cols,
            int health,
            int damage,
            int goldDrop
    ) {
        super(position, scale, speed, texture, rows, cols);

        this.health = health;
        this.damage = damage;
        this.goldDrop = goldDrop;

        currentCol = 0;
        currentRow = 0;
    }

    public void update(float dt) {
        targetBuilding = getBuildingInAttackRange();

        isAttacking = targetBuilding != null;

        if (isAttacking) {
            attackBuilding(dt);
        } else {
            attackTimer = attackCooldown;
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

        move(moveDir, dt);

        pushOutOfStones();
        pushAwayFromPlayer();

        boundaryClamp();
        updateAnimation(dt, moveDir);
    }

    private void pushOutOfStones() {
        Vector2 center = getHitCenter();

        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            float combinedRadius = hitRadius + ResourceNode.stoneRadius;

            float dx = center.x() - stoneCenter.x();
            float dy = center.y() - stoneCenter.y();

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 0.01f && distance < combinedRadius) {
                float overlap = combinedRadius - distance;

                float pushX = dx / distance * overlap;
                float pushY = dy / distance * overlap;

                applySafePush(pushX, pushY);

            }
        }
    }

    private void pushAwayFromPlayer() {
        Vector2 center = getHitCenter();

        Rectangle playerRect = Player.playerRec;

        float closestX = Math.max(
                playerRect.x(),
                Math.min(center.x(), playerRect.x() + playerRect.width())
        );

        float closestY = Math.max(
                playerRect.y(),
                Math.min(center.y(), playerRect.y() + playerRect.height())
        );

        float dx = center.x() - closestX;
        float dy = center.y() - closestY;

        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > 0.01f && distance < hitRadius) {
            float overlap = hitRadius - distance;

            // Player only slightly pushes zombie
            float playerPushStrength = 0.50f;

            float pushX = dx / distance * overlap * playerPushStrength;
            float pushY = dy / distance * overlap * playerPushStrength;

            applySafePush(pushX, pushY);
        }
    }

    private void applySafePush(float pushX, float pushY) {
        float originalX = position.x();
        float originalY = position.y();

        int steps = 10;

        // Try full diagonal push first
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;

            float testX = originalX + pushX * scale;
            float testY = originalY + pushY * scale;

            if (!collidesWithObstacle(testX, testY)) {
                position.x(testX);
                position.y(testY);
                return;
            }
        }

        // Try X-only push
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;

            float testX = originalX + pushX * scale;

            if (!collidesWithObstacle(testX, originalY)) {
                position.x(testX);
                return;
            }
        }

        // Try Y-only push
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;

            float testY = originalY + pushY * scale;

            if (!collidesWithObstacle(originalX, testY)) {
                position.y(testY);
                return;
            }
        }
    }

    private boolean collidesWithObstacle(float testX, float testY) {
        return collidesWithBuilding(testX, testY) || collidesWithStone(testX, testY);
    }

    private boolean collidesWithStone(float testX, float testY) {
        Vector2 testCenter = newVector2(testX + hitOffsetX, testY + hitOffsetY);

        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            float combinedRadius = hitRadius + ResourceNode.stoneRadius;

            if (Vector2Distance(testCenter, stoneCenter) < combinedRadius) {
                return true;
            }
        }

        return false;
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

            targetBuilding.takeDamage(damage);

            if (targetBuilding.isDestroyed()) {
                destroyBuilding(targetBuilding);
                targetBuilding = null;
            }
        }
    }

    private void destroyBuilding(Building building) {
        BuildSystem.destroyBuilding(building);
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
        float frameWidth = texture.width() / (float) cols;
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
        boolean moving = Vector2Length(moveDir) > 0;
        boolean shouldAnimate = moving || isAttacking;

        if (isAttacking != wasAttacking) {
            animationFrame = 0;
            frameTimer = 0;
            wasAttacking = isAttacking;
        }

        if (moving) {
            if (Math.abs(moveDir.x()) > Math.abs(moveDir.y())) {
                if (moveDir.x() < 0) {
                    currentCol = 0; // left
                    hitOffsetX = 7f;
                    hitOffsetY = 9f;
                } else {
                    currentCol = 2; // right
                    hitOffsetX = -6f;
                    hitOffsetY = 9f;
                }
            } else {
                currentCol = 1; // up/down
                hitOffsetX = 1f;
                hitOffsetY = 9f;
            }
        }

        if (shouldAnimate) {
            frameTimer += dt;

            if (frameTimer >= animSpeed) {
                frameTimer = 0;
                animationFrame = (animationFrame + 1) % 2;
            }
        } else {
            frameTimer = 0;
            animationFrame = 0;
        }

        if (isAttacking) {
            currentRow = 2 + animationFrame;
        } else {
            currentRow = animationFrame;
        }
    }

    public void draw() {
        int frameWidth = texture.width() / cols;
        int frameHeight = texture.height() / rows;

        Rectangle source = newRectangle(
                currentCol * frameWidth,
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

        if (health <= 0) {
            health = 0;
            Player.numGold += goldDrop;
        }
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