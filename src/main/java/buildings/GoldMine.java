package buildings;

import entities.Player;

import static com.raylib.Raylib.*;

public class GoldMine extends Building {
    private final float goldCooldown = 0.1f;
    private float goldTimer = 0f;
    private final int goldAmount = 2;

    public GoldMine(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public void update(float dt) {
        goldTimer += dt;
        if (goldTimer >= goldCooldown) {
            goldTimer = 0f;
            Player.numGold += goldAmount;
        }
    }
}
