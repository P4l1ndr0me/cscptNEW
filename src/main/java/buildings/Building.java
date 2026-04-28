package buildings;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class Building {
    Vector2 position;
    int type;

    public Building(Vector2 position, int type) {
        this.position = position;
        this.type = type;
    }

    public void draw(Texture[] textures) {
        DrawTextureEx(textures[type], position, 0, 1.0f, WHITE);
    }
}
