package buildings;

import static com.raylib.Raylib.*;

public class BuildingType {
    // BuildingType holds the data for a specific type of building (i.e. arrow tower)
    public String name;
    public Texture baseTexture;

    // Resources needed to place this building
    public int stoneCost;
    public int goldCost;

    // Placement and health data for this type of building
    public int maxPlacements;
    public int baseMaxHealth;

    public int baseDamage;

    public BuildingType(String name, Texture baseTexture, int stoneCost, int goldCost, int maxPlacements, int baseMaxHealth, int baseDamage) {
        this.name = name;
        this.baseTexture = baseTexture;
        this.stoneCost = stoneCost;
        this.goldCost = goldCost;
        this.maxPlacements = maxPlacements;
        this.baseMaxHealth = baseMaxHealth;
        this.baseDamage = baseDamage;
    }
}
