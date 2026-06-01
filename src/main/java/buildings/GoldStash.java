package buildings;

import static com.raylib.Raylib.*;

public class GoldStash extends Building {
    // Goldstash is the main base building
    // If this building is destroyed, the player loses the game

    public GoldStash(Vector2 position, BuildingType type) {
        super(position, type);
    }

    // Upgrades the gold stash to the next level
    public void upgrade() {
        if (!canUpgrade()) {
            return;
        }

        level++;

        maxHealth *= 2;
        health = maxHealth;
    }

    // Checks if the gold stash can be upgraded further
    public boolean canUpgrade() {
        return level < maxLevel;
    }

    // Returns the gold cost for the next upgrade based on current level
    public int getUpgradeGoldCost() {
        return switch (level) {
            case 1 -> 2000;
            case 2 -> 5000;
            default -> 0;
        };
    }

    // Checks if the gold stash has been destroyed
    public boolean checkDead(){
        return health == 0;
    }

    // Handles damage taken and triggers game over when destroyed
    @Override
    public void takeDamage(int damage) {
        super.takeDamage(damage);
        if (health <= 0) {
            core.GameState.setState(core.GameState.State.GAME_OVER);
        }
    }
}