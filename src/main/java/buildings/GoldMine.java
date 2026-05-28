package buildings;

import entities.Player;

import static com.raylib.Raylib.*;

public class GoldMine extends Building {
    // Gold mines generate gold over time after being placed
    private final float goldCooldown = 1; // Time between each gold generation
    private float goldTimer = 0f; // Tracks time since last gold generation
    private int goldAmount = 2; // Amount of gold generated each cooldown

    public GoldMine(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public void update(float dt) {
        // Increase timer using delta time
        goldTimer += dt;

        // Add gold once the cooldown is reached
        if (goldTimer >= goldCooldown) {
            goldTimer = 0f;
            Player.numGold += goldAmount;
        }
    }

    public void upgrade() {
        if (!canUpgrade()) {
            return;
        }

        level++;

        goldAmount *= 2;

        maxHealth += 50;
        health = maxHealth;
    }

    public int getUpgradeStoneCost() {
        return switch (level) {
            case 1 -> 100;
            case 2 -> 200;
            default -> 0;
        };
    }

    public int getUpgradeGoldCost() {
        return switch (level) {
            case 1 -> 50;
            case 2 -> 150;
            default -> 0;
        };
    }
}
