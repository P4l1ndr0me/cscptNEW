package buildings;

import core.TextureManager;

import static com.raylib.Raylib.*;
import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.*;

public class CannonTower extends Building {
    private final Texture cannonHeadTexture = TextureManager.getTexture("Cannon Head");;

    private float rotation = 90;

    public CannonTower(Vector2 position, BuildingType type) {
        super(position, type);
    }

    public void update(float dt) {
        //rotation += 60 * dt; // temporary test rotation
    }

    public void draw() {
        // Draw base of cannon first
        DrawTextureEx(type.texture, position, 0, 1.0f, WHITE);

        // Draw head of cannon nextg

        Rectangle source = newRectangle(0, 0, cannonHeadTexture.width(), cannonHeadTexture.height());

        Rectangle dest = newRectangle(position.x() + 32, position.y() + 32, 46, 78);

        Vector2 origin = newVector2(23, 55);

        DrawTexturePro(cannonHeadTexture, source, dest, origin, rotation, WHITE);
    }
}
