package buildings;

import core.EntityManager;
import core.TextureManager;
import entities.Enemy;
import entities.TowerBullet;

import static com.raylib.Colors.RED;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newRectangle;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class ArrowTower extends Building {
    public ArrowTower(Vector2 position, BuildingType type) {
        super(position, type);
    }

    private float rotation = 0;

    // How far the arrow can detect enemies
    private final float range = 350f;

    private float attackTimer = 0f;
    private final float attackCooldown = 0.45f;
    private final float knockbackStrength = 3f;

    public void update(float dt) {
        Enemy target = getNearestEnemyInRange();

        if (target != null) {
            aimAtEnemy(target);

            attackTimer +=dt;

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

        Vector2 origin = newVector2(30, 29);

        DrawTexturePro(headTex, source, dest, origin, rotation, WHITE);

        //DrawCircleLines((int)(position.x() + size / 2f), (int)(position.y() + size / 2f), range, RED);
    }

    private Enemy getNearestEnemyInRange() {
        Enemy nearestEnemy = null;
        float closestDistance = range;

        // Center of the cannon tower
        float arrowCenterX = position.x() + size / 2f;
        float arrowCenterY = position.y() + size / 2f;

        for (Enemy enemy : EntityManager.spawnedEnemies) {
            Vector2 enemyPos = enemy.getPosition();

            float dx = enemyPos.x() - arrowCenterX;
            float dy = enemyPos.y() - arrowCenterY;

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
        float arrowCenterX = position.x() + size / 2f;
        float arrowCenterY = position.y() + size / 2f;

        // Enemy position
        float enemyX = enemy.getPosition().x();
        float enemyY = enemy.getPosition().y();

        // Difference between enemy and cannon
        float dx = enemyX - arrowCenterX;
        float dy = enemyY - arrowCenterY;

        // atan2 gives angle in radians, then we convert to degrees
        rotation = (float) Math.toDegrees(Math.atan2(dy, dx)) + 0;
    }

    public int getUpgradeStoneCost() {
        return switch (level) {
            case 1 -> 120;
            case 2 -> 240;
            default -> 0;
        };
    }

    private void shoot(Enemy target) {
        float arrowCenterX = position.x() + size / 2f;
        float arrowCenterY = position.y() + size / 2f;

        float angleRad = (float) Math.toRadians(rotation);

        float barrelOffset = 32f;

        Vector2 bulletStart = newVector2(
                arrowCenterX + (float) Math.cos(angleRad) * barrelOffset,
                arrowCenterY + (float) Math.sin(angleRad) * barrelOffset
        );

        EntityManager.towerBullets.add(
                new TowerBullet(
                        bulletStart,
                        target,
                        damage,
                        knockbackStrength,
                        "Arrow Bullet",
                        550f,
                        1.0f,
                        0f
                )
        );
    }

    public int getUpgradeGoldCost() {
        return switch (level) {
            case 1 -> 80;
            case 2 -> 200;
            default -> 0;
        };
    }
}
