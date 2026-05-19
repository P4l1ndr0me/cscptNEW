package core;

import java.util.ArrayList;

import buildings.Building;
import entities.Enemy;
import ui.BuildMenu;
import world.ResourceNode;

import static com.raylib.Colors.RED;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;


public class EntityManager {
    public static ArrayList<Building> placedBuildings = new ArrayList<>();
    public static ArrayList<Enemy> spawnedEnemies = new ArrayList<>();
    public static ArrayList<Rectangle> stoneRects = new ArrayList<>(); // store center pos of every stone

    public static void DrawEntities() {
        // Draw buildings
        for (Building building : placedBuildings) {
            building.draw();
        }

        // Draw stone
        for (Rectangle stoneRect : stoneRects) {
            DrawTextureEx(
                    TextureManager.getTexture("stone"),
                    newVector2(stoneRect.x(), stoneRect.y()),
                    0.0f,
                    ResourceNode.stoneScale,
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
