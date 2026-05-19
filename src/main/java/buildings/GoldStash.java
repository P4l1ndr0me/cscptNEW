package buildings;

import static com.raylib.Raylib.*;

public class GoldStash extends Building {
    // Goldstash is the main base building
    // If this building is destroyed, the player loses the game
    public GoldStash(Vector2 position, BuildingType type) {
        super(position, type);
    }
}
