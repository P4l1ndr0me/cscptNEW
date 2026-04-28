package core;

import java.util.ArrayList;

import buildings.Building;
import ui.BuildMenu;

import static com.raylib.Raylib.*;

public class EntityManager {
    public static ArrayList<Building> placedBuildings = new ArrayList<>();

    public static void DrawEntities() {
        // Draw buildings
        for (Building building : placedBuildings) {
            building.draw(BuildMenu.buildingTextures);
        }
    }
}
