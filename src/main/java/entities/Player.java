package entities;

import static com.raylib.Raylib.*;

public class Player {
    float x;
    float y;

    Texture spriteSheet;

    float spd;
    int frame; // animation frame
    int maxFrames; // max amount of frames per row in spreadsheet
    int rows; // rows in spreadsheet
    float frameTime = 1.0f; // speed of animation idk what is a good vaule
    int direction; //which way they are goign
    boolean moving = false;

    int framWidth;
    int frameHeight;

    public Player (Texture sheet, int rows, int frames, float x, float y, float spd){
        this.spriteSheet = sheet;
        this.rows = rows;
        this.frame = frames;
        this.x = x;
        this.y = y;
        this.spd = spd;
    }

    /*
     * As WASD is pressed, change animation accoringly
     *
     */
    public void update() {
        moving = false;
        if (IsKeyDown(KEY_W)) {
            y -= spd; // go upwards with that speed
            moving = true;
            direction = 1; // we will keep it as 1/2 for up and down as we only 2 animation types

        }
        if (IsKeyDown(KEY_S)) {
            y += spd; // go upwards with that speed
            moving = true;
            direction = 2;
        }
        if (IsKeyDown(KEY_A)) {
            x -= spd; // go upwards with that speed
            moving = true;
            direction = 0;
        }
        if (IsKeyDown(KEY_D)) {
            x += spd; // go upwards with that speed
            moving = true;
            direction = 0;
        }
    }
}
