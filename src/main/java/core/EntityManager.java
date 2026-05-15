package core;

import java.util.ArrayList;

import buildings.Building;
import entities.Enemy;
import world.ResourceNode;

import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class EntityManager {
    public static ArrayList<Enemy> spawnedEnemies = new ArrayList<>();
    public static ArrayList<Building> placedBuildings = new ArrayList<>(); // store info of every placed building
    public static ArrayList<Vector2> stoneCenters = new ArrayList<>(); // store center pos of every stone

    public static void DrawEntities() {
        // Draw buildings
        for (Building building : placedBuildings) {
            building.draw();
        }

        // Draw stone
        for (Vector2 stoneCenter : stoneCenters) {
            DrawTextureEx(
                    TextureManager.getTexture("stone"),
                    newVector2(stoneCenter.x() - ResourceNode.stoneRadius, stoneCenter.y() - ResourceNode.stoneRadius),
                    0.0f,
                    1.0f,
                    WHITE
            );
        }

        for (Enemy enemy : spawnedEnemies){
            enemy.drawWalk();
        }
    }

    public static void spawnZombie(float x, float y, float scale, float spd, Texture texture, int row, int col){
        Enemy enemy = new Enemy(newVector2(x, y), scale, spd, texture, row, col);
        spawnedEnemies.add(enemy);
    }

}
