package buildings;

import core.TextureManager;
import systems.BuildSystem;

import static com.raylib.Colors.*;
import static com.raylib.Raylib.*;
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
        Texture tex = TextureManager.getTexture(type.name + " " + level);

        // Draw building at its top-left position
        DrawTextureEx(tex, position, 0, 1.0f, WHITE);

        drawHealthBar();
    }

    protected void drawHealthBar() {
        if (health >= maxHealth) {
            return;
        }

        float barWidth = 48;
        float barHeight = 6;

        // Inside the 64x64 building, near the bottom
        float barX = position.x() + (size - barWidth) / 2f;
        float barY = position.y() + size - barHeight - 5;

        float healthPercent = getHealthPercent();

        // Optional dark background behind the bar
        DrawRectangle(
                (int) barX,
                (int) barY,
                (int) barWidth,
                (int) barHeight,
                DARKGRAY
        );

        // Actual health amount
        DrawRectangle(
                (int) barX,
                (int) barY,
                (int) (barWidth * healthPercent),
                (int) barHeight,
                GREEN
        );

        // Border
        DrawRectangleLines(
                (int) barX,
                (int) barY,
                (int) barWidth,
                (int) barHeight,
                BLACK
        );
    }

    public boolean canUpgrade() {
        Building goldStash = BuildSystem.getGoldStash();

        if (goldStash == null) {
            return false;
        }

        return level < maxLevel && level < goldStash.level;
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

    public void takeDamage(int amount) {
        health -= amount;

        if (health < 0) {
            health = 0;
        }
    }

    public boolean isDamaged() {
        return health < maxHealth;
    }

    public boolean isDestroyed() {
        return health <= 0;
    }

    public float getHealthPercent() {
        return (float) health / maxHealth;
    }
}
