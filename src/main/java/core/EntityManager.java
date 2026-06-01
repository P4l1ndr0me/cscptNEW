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
    // Stores all active enemies, placed buildings, and stone resource positions
    public static ArrayList<Enemy> spawnedEnemies = new ArrayList<>();
    public static ArrayList<Building> placedBuildings = new ArrayList<>();
    public static ArrayList<TowerBullet> towerBullets = new ArrayList<>();
    public static ArrayList<Vector2> stoneCenters = new ArrayList<>();

    // Draws all entities in the game world
    public static void drawEntities() {
        // Draw all placed buildings
        for (Building building : placedBuildings) {
            building.draw();
        }

        // Draw all stone nodes
        for (Vector2 stoneCenter : stoneCenters) {
            DrawTextureEx(
                    TextureManager.getTexture("stone"),
                    newVector2(stoneCenter.x() - ResourceNode.STONE_RADIUS, stoneCenter.y() - ResourceNode.STONE_RADIUS),
                    0.0f,
                    1.0f,
                    WHITE
            );
        }

        // Draw all active enemies
        for (Enemy enemy : spawnedEnemies){
            enemy.draw();
        }

        // Draw all tower bullets
        for (TowerBullet bullet : towerBullets) {
            bullet.draw();
        }
    }

    // Updates all entities and removes dead/inactive ones
    public static void updateEntities(float dt) {
        // Update every building
        for (Building building : EntityManager.placedBuildings) {
            building.update(dt);
        }

        // Update enemies and remove dead ones (iterate backwards for safe removal)
        for (int i = spawnedEnemies.size() - 1; i >= 0; i--) {
            Enemy enemy = spawnedEnemies.get(i);

            enemy.update(dt);

            if (enemy.isDead()) {
                spawnedEnemies.remove(i);
            }
        }

        // Update bullets and remove inactive ones (iterate backwards for safe removal)
        for (int i = towerBullets.size() - 1; i >= 0; i--) {
            TowerBullet bullet = towerBullets.get(i);

            bullet.update(dt);

            if (!bullet.isActive()) {
                towerBullets.remove(i);
            }
        }
    }

    // Clears all entity lists when resetting the game
    public static void reset() {
        // Clear all dynamic lists
        spawnedEnemies.clear();
        placedBuildings.clear();
        towerBullets.clear();
        stoneCenters.clear();
    }
}