package buildings;

import entities.Player;

import static com.raylib.Raylib.*;

public class GoldMine extends Building {
    // Gold mines generate gold over time after being placed
    private final float goldCooldown = 0.15f; // Time between each gold generation
    private float goldTimer = 0f; // Tracks time since last gold generation
    private final int goldAmount = 2; // Amount of gold generated each cooldown

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
}
