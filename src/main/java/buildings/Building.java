package buildings;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class Building {
    public Vector2 position; // Stores top left corner position, not center
    public BuildingType type;
    public static int size = 64; // size of every building (64 pixels x 64 pixels)

    public Building(Vector2 position, BuildingType type) {
        this.position = position;
        this.type = type;
    }

    public void draw() {
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);
    }
}
