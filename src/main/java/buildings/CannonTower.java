package buildings;

import core.TextureManager;
import core.EntityManager;
import entities.Enemy;

import static com.raylib.Colors.RED;
import static com.raylib.Raylib.*;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.*;

public class CannonTower extends Building {
    private final Texture cannonHeadTexture = TextureManager.getTexture("Cannon Head");

    private float rotation = 0;

    // How far the cannon can detect enemies
    private final float range = 250f;

    public CannonTower(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public void update(float dt) {
        Enemy target = getNearestEnemyInRange();

        if (target != null) {
            aimAtEnemy(target);
        }
    }

    public void draw() {
        // Draw base of cannon first
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);

        // Draw head of cannon next
        Rectangle source = newRectangle(0, 0, cannonHeadTexture.width(), cannonHeadTexture.height());

        Rectangle dest = newRectangle(position.x() + 32, position.y() + 32, cannonHeadTexture.width(), cannonHeadTexture.height());

        Vector2 origin = newVector2(23, 23);

        DrawTexturePro(cannonHeadTexture, source, dest, origin, rotation, WHITE);

        DrawCircleLines((int)(position.x() + size / 2f), (int)(position.y() + size / 2f), range, RED);
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
}
