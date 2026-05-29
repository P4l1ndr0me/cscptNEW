package buildings;

import core.TextureManager;
import core.EntityManager;
import entities.Enemy;
import entities.TowerBullet;

import static com.raylib.Colors.RED;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.*;

public class CannonTower extends Building {
    private float rotation = 0;

    // How far the cannon can detect enemies
    private final float range = 250f;

    private float attackTimer = 0f;
    private final float attackCooldown = 1.2f;
    private final float knockbackStrength = 10f;

    public CannonTower(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public void update(float dt) {
        Enemy target = getNearestEnemyInRange();

        if (target != null) {
            aimAtEnemy(target);

            attackTimer += dt;

            if (attackTimer >= attackCooldown) {
                attackTimer = 0f;
                shoot(target);
            }
        } else {
            attackTimer = 0f;
        }
    }

    public void draw() {
        Texture headTex = TextureManager.getTexture(type.name + " " + level);

        // Draw base of cannon first
        DrawTextureEx(type.baseTexture, position, 0, 1.0f, WHITE);

        // Draw head of cannon next
        Rectangle source = newRectangle(0, 0, headTex.width(), headTex.height());

        Rectangle dest = newRectangle(position.x() + 32, position.y() + 32, headTex.width(), headTex.height());

        Vector2 origin = newVector2(23, 23);

        DrawTexturePro(headTex, source, dest, origin, rotation, WHITE);

//        DrawCircleLines((int)(position.x() + size / 2f), (int)(position.y() + size / 2f), range, RED);
    }

    private Enemy getNearestEnemyInRange() {
        Enemy nearestEnemy = null;
        float closestDistance = range;

        // Center of the cannon tower
        float cannonCenterX = position.x() + size / 2f;
        float cannonCenterY = position.y() + size / 2f;

        for (Enemy enemy : EntityManager.spawnedEnemies) {
            Vector2 enemyPos = enemy.getPosition();

            float dx = enemyPos.x() - cannonCenterX;
            float dy = enemyPos.y() - cannonCenterY;

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= closestDistance) {
                closestDistance = distance;
                nearestEnemy = enemy;
            }
        }

        return nearestEnemy;
    }

    private void aimAtEnemy(Enemy enemy) {
        // Center of cannon
        float cannonCenterX = position.x() + size / 2f;
        float cannonCenterY = position.y() + size / 2f;

        // Enemy position
        float enemyX = enemy.getPosition().x();
        float enemyY = enemy.getPosition().y();

        // Difference between enemy and cannon
        float dx = enemyX - cannonCenterX;
        float dy = enemyY - cannonCenterY;

        // atan2 gives angle in radians, then we convert to degrees
        rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) + 0;
    }

    private void shoot(Enemy target) {
        float cannonCenterX = position.x() + size / 2f;
        float cannonCenterY = position.y() + size / 2f;

        float angleRad = (float) Math.toRadians(rotation);

        float barrelOffset = 35f;

        Vector2 bulletStart = newVector2(
                cannonCenterX + (float) Math.cos(angleRad) * barrelOffset,
                cannonCenterY + (float) Math.sin(angleRad) * barrelOffset
        );

        EntityManager.towerBullets.add(
                new TowerBullet(
                        bulletStart,
                        target,
                        damage,
                        knockbackStrength,
                        "Cannon Bullet",
                        300f,
                        1.0f,
                        0f
                )
        );
    }

    public int getUpgradeStoneCost() {
        return switch (level) {
            case 1 -> 150;
            case 2 -> 300;
            default -> 0;
        };
    }

    public int getUpgradeGoldCost() {
        return switch (level) {
            case 1 -> 100;
            case 2 -> 250;
            default -> 0;
        };
    }
}
