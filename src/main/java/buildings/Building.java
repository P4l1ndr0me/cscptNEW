package buildings;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newRectangle;

public class Building {
    public static int size = 64; // size of every building (64 pixels x 64 pixels)

    public Vector2 position; // Stores top left corner position, not center
    public BuildingType type;
    public int health;

    public Building(Vector2 position, BuildingType type) {
        this.position = position;
        this.type = type;
        this.health = type.maxHealth;
    }

    public void update(float dt) {}



    public void draw() {
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);
    }

    public Rectangle getRect() {
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
