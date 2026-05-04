package core;

import java.util.ArrayList;

import buildings.Building;
import com.raylib.Raylib;
import entities.Enemy;
import ui.BuildMenu;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;


public class EntityManager {
    public static ArrayList<Building> placedBuildings = new ArrayList<>();
    public static ArrayList<Enemy> spawnedEnemies = new ArrayList<>();


    public static void DrawEntities() {
        // Draw buildings
        for (Building building : placedBuildings) {
            building.draw(BuildMenu.buildingTextures);
        }

        for (Enemy enemy : spawnedEnemies){
            enemy.drawWalk();
        }
    }

    public static void spawnDemZombies(float x, float y, float scale, float spd, Texture texture, int row, int col){
        Enemy enemy = new Enemy(newVector2(x, y), scale, spd, texture, row, col);
        spawnedEnemies.add(enemy);
    }
}
