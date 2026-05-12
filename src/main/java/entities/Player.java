package entities;

import core.EntityManager;
import core.TextureManager;
import world.World;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class Player extends Entity {
    public static Rectangle playerRec;
    public static Rectangle miningRec;
    public static int numStone = 0;

    // Size & movement
    private final float halfWidth;
    private final float halfHeight;
    private boolean isMoving = false;
    private int lookX; // 1 = right, -1 = left

    // Mining
    private boolean isMining = false;
    private float miningTimer = 0f;
    private final float miningCooldown = 0.5f;
    private final int miningAmount = 20;
    private final int miningRecWidth = 25;
    private final int miningRecHeight = 30;

    // Mining animation
    private Texture mining;
    private int miningFrame = 0;
    private float miningAnimTimer = 0f;
    private final float miningFrameSpeed = 0.12f;
    private int row;


    public Player() {
        super(
                newVector2(World.worldWidth / 2f, World.worldHeight / 2f),
                2.0f,
                250.0f,
                TextureManager.getTexture("playerNEW"),
                3,
                3);

        frameSpeed = 0.15f;
        currentRow = 1;
        lookX = 1;

        halfWidth = (texture.width() / (float) frames) * scale / 2;
        halfHeight = (texture.height() / (float) rows) * scale / 2;

        playerRec = newRectangle(
                position.x() - halfWidth,
                position.y() - halfHeight,
                halfWidth * 2,
                halfHeight * 2);

        miningRec = newRectangle(
                position.x() + 10,
                position.y() - miningRecHeight / 2f,
                miningRecWidth,
                miningRecHeight
        );
    }

    public void update(float dt) {
        Vector2 moveDir = getMovementInput();

        updateDirection(moveDir);
        move(moveDir, dt);
        boundaryClamp();

        updatePlayerRect();
        updateMiningRect();

        updatePlayerAnimation(dt);
        updateMining(dt);
        updateMiningAnimation(dt);
    }

    // draw walking animations for player
    public void draw() {
        if (isMining) {
            drawMiningAnimation();
        } else {
            drawWalkingAnimation();
        }
    }

    private Vector2 getMovementInput() {
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

        Vector2 moveDir = newVector2(moveX, moveY);

        // Make sure going diagonally doesn't increase speed (by normalizing the movement vector)
        if (Vector2Length(moveDir) != 0) {
            isMoving = true;
            return Vector2Normalize(moveDir);
        }

        isMoving = false;
        return moveDir;
    }

    private void updateDirection(Vector2 moveDir) {
        // Set row of sprite sheet based on player direction
//        if (moveY < 0) currentRow = 0;  // moving up
//        if (moveY > 0) currentRow = 1;  // moving down
        if (moveDir.x() < 0) {
            currentRow = 2;  // moving left
            row = 1;
            lookX = -1;
        }
        if (moveDir.x() > 0) {
            currentRow = 1;  // moving right
            row = 0;
            lookX = 1;
        }
    }

    private void move(Vector2 moveDir, float dt) {
        position.x(Math.round(position.x() + speed * moveDir.x() * dt));
        position.y(Math.round(position.y() + speed * moveDir.y() * dt));
    }

    private void boundaryClamp() {
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

    private void updatePlayerRect() {
        playerRec.x(position.x() - halfWidth);
        playerRec.y(position.y() - halfHeight);
    }

    private void updateMiningRect() {
        if (lookX == 1) { // looking right
            miningRec.x(position.x() + 10);
        } else { // looking left
            miningRec.x(position.x() - 10 - miningRecWidth);
        }
        miningRec.y(position.y() - miningRecHeight / 2f);
    }

    private boolean isNearStone() {
        for (Rectangle stoneRect : EntityManager.stoneRects) {
            if (CheckCollisionRecs(miningRec, stoneRect)) {
                return true;
            }
        }
        return false;
    }

    public void updateMining(float dt) {
        if (IsKeyDown(KEY_SPACE) && isNearStone()) {
            isMining = true;
            miningTimer += dt;

            if (miningTimer >= miningCooldown) {
                miningTimer = 0f;
                numStone += miningAmount;
            }
        } else {
            isMining = false;
            miningTimer = 0f;
        }
    }

    public void updatePlayerAnimation(float dt) {
        if (isMoving) {
            frameTimer += dt;

            if (frameTimer >= frameSpeed) {
                frameTimer = 0;
                currentFrame = (currentFrame + 1) % frames;
            }
        } else {
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

    private void drawWalkingAnimation() {
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

        Vector2 origin = newVector2(0, 0);

        DrawTexturePro(texture, source, dest, origin, 0.0f, WHITE);
    }

    private void drawMiningAnimation() {
        Texture mining = TextureManager.getTexture("mining");
        int numRows = 2;
        int numFrames = 2;

        int frameWidth = mining.width() / numFrames;
        int frameHeight = mining.height() / numRows;

        Rectangle source = new Rectangle()
                .x(miningFrame * frameWidth)
                .y(row * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        float halfW = (mining.width() / numFrames) * scale / 2;
        float halfH = (mining.height() / numRows) * scale / 2;

        Rectangle dest = new Rectangle()
                .x((int) (position.x() - halfW + (lookX == 1 ? 6 : -6))) // add/subtract 6 for pickaxe offset
                .y((int) (position.y() - halfH))
                .width(halfW * 2)
                .height(halfH * 2);

        Vector2 origin = newVector2(0, 0);

        DrawTexturePro(mining, source, dest, origin, 0.0f, WHITE);
    }
}
