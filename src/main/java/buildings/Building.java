package buildings;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newRectangle;

public class Building {
    public static final int size = 64; // size of every building (64 pixels x 64 pixels)

    public Vector2 position; // Stores top left corner position, not center
    public BuildingType type; // Stores the data for this building
    public int health; // Current health of this specific placed building

    public Building(Vector2 position, BuildingType type) {
        this.position = position;
        this.type = type;
        this.health = type.maxHealth; // Each placed building starts with its type's max health
    }

    public void update(float dt) {

    }

    public void draw() {
        // Draw building at its top-left position
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);
    }

    public Rectangle getRect() {
        // Return building hitbox (i.e. used for collision checks)
        return newRectangle(
                position.x(),
                position.y(),
                size,
                size
        );
    }

    public void takeDamage(int amount) {
        health -= amount;
    }
}
