package buildings;

import static com.raylib.Raylib.*;

public class GoldStash extends Building {
    // Goldstash is the main base building
    // If this building is destroyed, the player loses the game
    public GoldStash(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public void upgrade() {
        if (!canUpgrade()) {
            return;
        }

        level++;

        maxHealth *= 2;
        health = maxHealth;
    }

    public boolean canUpgrade() {
        return level < maxLevel;
    }

    public int getUpgradeGoldCost() {
        return switch (level) {
            case 1 -> 2000;
            case 2 -> 5000;
            default -> 0;
        };
    }
}
