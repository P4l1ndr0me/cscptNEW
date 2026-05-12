package core;

import java.util.ArrayList;

import buildings.Building;
import world.ResourceNode;

import static com.raylib.Colors.RED;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class EntityManager {
    public static ArrayList<Building> placedBuildings = new ArrayList<>(); // store info of every placed building
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
    }
}
