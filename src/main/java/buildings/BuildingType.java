package buildings;

import static com.raylib.Raylib.*;

public class BuildingType {
    public Texture texture;
    public int cost;

    public BuildingType(Texture texture, int cost) {
        this.texture = texture;
        this.cost = cost;
    }
}
