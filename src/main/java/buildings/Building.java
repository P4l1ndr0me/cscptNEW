package buildings;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class Building {
    public Vector2 position; // top left corner, not center
    public BuildingType type;
    public static int size = 64;

    public Building(Vector2 position, BuildingType type) {
        this.position = position;
        this.type = type;
    }

    public void draw() {
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);
    }
}
