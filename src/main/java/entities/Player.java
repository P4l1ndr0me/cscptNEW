package entities;

import static com.raylib.Raylib.*;

public class Player extends Entity {
    public Player(Vector2 position, float scale, float speed, Texture texture, int rows, int frames) {
        super(position, scale, speed, texture, rows, frames);
    }
}
