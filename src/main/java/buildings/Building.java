package buildings;

import systems.BuildSystem;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newRectangle;

public class Building {
    public static final int size = 64; // size of every building (64 pixels x 64 pixels)

    public Vector2 position; // Stores top left corner position, not center
    public BuildingType type; // Stores the data for this building

    public int health; // Current health of this specific placed building
    public int maxHealth;

    public int level;
    public int maxLevel;

    public int damage;

    public Building(Vector2 position, BuildingType type) {
        this.position = position;
        this.type = type;

        this.maxHealth = type.baseMaxHealth; // Each placed building starts with its type's max health
        this.health = maxHealth;

        this.level = 1;
        this.maxLevel = 3;

        this.damage = type.baseDamage;
    }

    public void update(float dt) {

    }

    public void draw() {
        // Draw building at its top-left position
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);
    }

    public boolean canUpgrade() {
        Building goldStash = BuildSystem.getGoldStash();

        if (goldStash == null) {
            return false;
        }

        return level < maxLevel & level < BuildSystem.getGoldStash().level;
    }

    public int getUpgradeStoneCost() {
        return 0;
    }

    public int getUpgradeGoldCost() {
        return 0;
    }

    public void upgrade() {
        if (!canUpgrade()) {
            return;
        }

        level++;

        maxHealth += 50;
        health = maxHealth;

        damage += 10;
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
}
