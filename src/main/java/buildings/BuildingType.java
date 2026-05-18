package buildings;

import static com.raylib.Raylib.*;

public class BuildingType {
    // BuildingType holds the data for a specific type of building (i.e. arrow tower)
    public String name;
    public Texture texture;

    // Resources needed to place this building
    public int stoneCost;
    public int goldCost;

    // Placement and health data for this type of building
    public int maxPlacements;
    public int maxHealth;

    public BuildingType(String name, Texture texture, int stoneCost, int goldCost, int maxPlacements, int maxHealth) {
        this.name = name;
        this.texture = texture;
        this.stoneCost = stoneCost;
        this.goldCost = goldCost;
        this.maxPlacements = maxPlacements;
        this.maxHealth = maxHealth;
    }
}
