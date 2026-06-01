package core;

import java.util.ArrayList;
import buildings.Building;
import entities.Enemy;
import entities.TowerBullet;
import world.ResourceNode;

import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class EntityManager {
    public static ArrayList<Enemy> spawnedEnemies = new ArrayList<>();
    public static ArrayList<Building> placedBuildings = new ArrayList<>();
    public static ArrayList<TowerBullet> towerBullets = new ArrayList<>();
    public static ArrayList<Vector2> stoneCenters = new ArrayList<>();

    private static boolean isPaused = false;

    public static void setPaused(boolean paused) {
        isPaused = paused;
    }

    public static void drawEntities() {
        for (Building building : placedBuildings) {
            building.draw();
        }
        for (Vector2 stoneCenter : stoneCenters) {
            DrawTextureEx(
                    TextureManager.getTexture("stone"),
                    newVector2(stoneCenter.x() - ResourceNode.STONE_RADIUS, stoneCenter.y() - ResourceNode.STONE_RADIUS),
                    0.0f,
                    1.0f,
                    WHITE
            );
        }
        for (Enemy enemy : spawnedEnemies){
            enemy.draw();
        }
        for (TowerBullet bullet : towerBullets) {
            bullet.draw();
        }
    }

    public static void updateEntities(float dt) {
        // Don't update entities when paused
        if (isPaused) return;

        for (Building building : EntityManager.placedBuildings) {
            building.update(dt);
        }
        for (int i = spawnedEnemies.size() - 1; i >= 0; i--) {
            Enemy enemy = spawnedEnemies.get(i);
            enemy.update(dt);
            if (enemy.isDead()) {
                spawnedEnemies.remove(i);
            }
        }
        for (int i = towerBullets.size() - 1; i >= 0; i--) {
            TowerBullet bullet = towerBullets.get(i);
            bullet.update(dt);
            if (!bullet.isActive()) {
                towerBullets.remove(i);
            }
        }
    }

    public static void reset() {
        spawnedEnemies.clear();
        placedBuildings.clear();
        towerBullets.clear();
        stoneCenters.clear();
        isPaused = false;  // Reset pause state
    }
}