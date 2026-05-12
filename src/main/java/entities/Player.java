package entities;

import core.EntityManager;
import core.TextureManager;
import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class Player extends Entity {
    public static Rectangle playerRec;
    public static int numStone = 0;

    // Movement
    private final float halfWidth;
    private final float halfHeight;
    private boolean isMoving = false;

    // Mining
    private boolean isMining = false;
    private float miningTimer = 0f;
    private final float miningCooldown = 0.5f;
    private final int miningAmount = 20;
    public static Rectangle miningRec;

    // Mining animation
    private int miningFrame = 0;
    private float miningAnimTimer = 0f;
    private final float miningFrameSpeed = 0.12f;


    public Player() {
        super(newVector2(World.worldWidth / 2f, World.worldHeight / 2f), 2.0f, 250.0f, TextureManager.getTexture("player"), 3, 3);

        halfWidth = (texture.width() / (float) frames) * scale / 2;
        halfHeight = (texture.height() / (float) rows) * scale / 2;

        playerRec = newRectangle(position.x() - halfWidth, position.y() - halfHeight, halfWidth * 2, halfHeight * 2);
        miningRec = newRectangle(
                position.x() + 10,
                position.y() - 30 / 2f,
                25,
                30
        );
    }

    // draw walking animations for player
    public void draw() {
        if (!isMining) {
            int frameWidth = texture.width() / frames;
            int frameHeight = texture.height() / rows;
            Rectangle source = new Rectangle()
                    .x(currentFrame * frameWidth)
                    .y(currentRow * frameHeight)
                    .width(frameWidth)
                    .height(frameHeight);

            Rectangle dest = new Rectangle()
                    .x((int) (position.x() - halfWidth))
                    .y((int) (position.y() - halfHeight))
                    .width(halfWidth * 2)
                    .height(halfHeight * 2);

            Vector2 origin = new Vector2().x(0).y(0);

            DrawTexturePro(texture, source, dest, origin, 0.0f, WHITE);
        }
        else {
            Texture current;
            if (miningFrame == 0) {
                current = TextureManager.getTexture("rightMine1");
            } else {
                current = TextureManager.getTexture("rightMine2");
            }

            DrawTextureEx(
                    current,
                    newVector2(position.x() - halfWidth, position.y() - halfHeight),
                    0.0f,
                    scale,
                    WHITE
            );
        }
    }

    public void boundaryClamp() {
        // Make sure player does not go out of bounds
        if (position.x() < halfWidth) {
            position.x(halfWidth);
        }
        if (position.y() < halfHeight) {
            position.y(halfHeight);
        }
        if (position.x() > World.worldWidth - halfWidth) {
            position.x(World.worldWidth - halfWidth);
        }
        if (position.y() > World.worldHeight - halfHeight) {
            position.y(World.worldHeight - halfHeight);
        }
    }

    public boolean isNearStone() {
        for (Rectangle stoneRect : EntityManager.stoneRects) {
            if (CheckCollisionRecs(miningRec, stoneRect)) {
                return true;
            }
        }
        return false;
    }

    public void mine(float dt) {
        if (IsKeyDown(KEY_SPACE) && isNearStone()) {
            isMining = true;
            miningTimer += dt;

            if (miningTimer >= miningCooldown) {
                miningTimer = 0f;
                numStone += miningAmount;
            }
        }
        else {
            isMining = false;
            miningTimer = 0f;
        }
    }

    public void updateMiningRect() {
        miningRec.x(position.x() + 10);
        miningRec.y(position.y() - 30 / 2f);
    }

    public void updatePlayerAnimation(float dt) {
        if (isMoving) {
            frameTimer += dt;
            if (frameTimer >= frameSpeed) {
                frameTimer = 0;
                currentFrame = (currentFrame + 1) % frames;
            }
        }
        else {
            currentFrame = 0;
            frameTimer = 0;
        }
    }

    public void updateMiningAnimation(float dt) {
        if (isMining) {
            miningAnimTimer += dt;

            if (miningAnimTimer >= miningFrameSpeed) {
                miningAnimTimer = 0f;
                miningFrame = (miningFrame + 1) % 2;
            }
        } else {
            miningFrame = 0;
            miningAnimTimer = 0f;
        }
    }

    public void update(float dt) {
        float moveX = 0, moveY = 0;

        // Movement input
        if (IsKeyDown(KEY_W)) {
            moveY -= 1;
        }
        if (IsKeyDown(KEY_S)) {
            moveY += 1;
        }
        if (IsKeyDown(KEY_A)) {
            moveX -= 1;
        }
        if (IsKeyDown(KEY_D)) {
            moveX += 1;
        }

        // Set row of sprite sheet based on direction
        if (moveY < 0) currentRow = 0;  // up
        if (moveY > 0) currentRow = 1;  // down
        if (moveX < 0) currentRow = 2;  // left
        if (moveX > 0) currentRow = 1;  // right

        if (moveX == 0 && moveY == 0) {
            currentRow = 1;
        }

        // Make sure going diagonally doesn't increase speed
        Vector2 moveDir = newVector2(moveX, moveY);
        if (Vector2Length(moveDir) != 0) {
            moveDir = Vector2Normalize(moveDir);
            isMoving = true;
        }
        else {
            isMoving = false;
        }

        position.x(Math.round(position.x() + speed * moveDir.x() * dt));
        position.y(Math.round(position.y() + speed * moveDir.y() * dt));

        // update playerRec
        playerRec.x(position.x() - halfWidth);
        playerRec.y(position.y() - halfHeight);

        // update miningRec
        updateMiningRect();

        boundaryClamp();

        updatePlayerAnimation(dt);

        mine(dt);
        updateMiningAnimation(dt);
    }
}
