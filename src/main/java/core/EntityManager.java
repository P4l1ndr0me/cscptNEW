package core;

import java.util.ArrayList;

import buildings.Building;
import ui.BuildMenu;
import world.World;

import static com.raylib.Raylib.*;

public class EntityManager {
    public static ArrayList<Building> placedBuildings = new ArrayList<>();
    public static ArrayList<Vector2> stonePosition = new ArrayList<>();

    public static void DrawEntities() {
        // Draw buildings
        for (Building building : placedBuildings) {
            building.draw(BuildMenu.buildingTextures);
        }

        // Draw stone
        for (Vector2 position : stonePosition) {
            World.drawStone(position);
        }
    }
}
