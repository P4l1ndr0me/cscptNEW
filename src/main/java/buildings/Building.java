package buildings;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.*;

public class Building {
    public Vector2 position; // top left corner, not center
    public int type;

    public Building(Vector2 position, int type) {
        this.position = position;
        this.type = type;
    }

    public void draw(Texture[] textures) {
        DrawTextureEx(textures[type], position, 0, 1.0f, WHITE);
    }
}
