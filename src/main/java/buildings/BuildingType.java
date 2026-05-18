package buildings;

import static com.raylib.Raylib.*;

public class BuildingType {
    public String name;
    public Texture texture;
    public int stoneCost;
    public int goldCost;
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
