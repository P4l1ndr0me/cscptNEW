package entities;

import buildings.Building;
import core.EntityManager;
import core.TextureManager;
import world.*;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

public class Player extends Entity {
    public static Rectangle playerRec;
    public static Rectangle miningRec;
    public static int numStone = 1000;
    public static int numGold = 1000;
    public static int health = 100;

    // Size & movement
    private final float halfWidth;
    private final float halfHeight;
    private boolean isMoving = false;
    private int lookX; // 1 = facing right, -1 = facing left
    private final float playerHitboxWidth = 20;
    private final float playerHitboxHeight = 16;
    private final float playerHitboxOffsetY = 12;

    // Mining
    private boolean hasPickaxeEquipped = false; // R to toggle
    private boolean isAutoMining = false;       // SPACE to toggle
    private float miningTimer = 0f;
    private final float miningCooldown = 0.8f;
    private final int miningAmount = 20;
    private final int miningRecWidth = 25;
    private final int miningRecHeight = 30;
    private final int miningRecOffset = 10;

    // Mining animation
    private final Texture mining = TextureManager.getTexture("mining");
    private int pickaxeFrame = 0;
    private final int pickaxeRows = 4;
    private final int pickaxeFrames = 3;
    private float pickaxeAnimTimer = 0f;
    private final float pickaxeFrameSpeed = 0.20f;
    private boolean pickaxeDown = false;
    private final float pickaxeOffset = 3 * scale;

    public Player() {
        super(
                newVector2(World.WORLD_WIDTH / 2f, World.WORLD_HEIGHT / 2f),
                2.0f,
                250.0f,
                TextureManager.getTexture("playerNEW"),
                3,
                3);

        frameSpeed = 0.15f;

        // Player spawns looking to the right (first frame = idle)
        currentRow = 1;
        lookX = 1;

        halfWidth = ((float) texture.width() / frames) * scale / 2;
        halfHeight = ((float) texture.height() / rows) * scale / 2;

        playerRec = newRectangle(
                position.x() - playerHitboxWidth / 2f,
                position.y() + playerHitboxOffsetY,
                playerHitboxWidth,
                playerHitboxHeight);

        miningRec = newRectangle(
                position.x() + 10,
                position.y() - miningRecHeight / 2f,
                miningRecWidth,
                miningRecHeight
        );
    }

    public void update(float dt) {
        Vector2 moveDir = getMovementInput();

        updateToolInput();

        updateDirection(moveDir);
        move(moveDir, dt);
        boundaryClamp();

        updatePlayerRect();
        pushOutOfStones();
        updateMiningRect();

        updateMining(dt);
        updateAnimation(dt);
    }

    // draw walking animations for player
    public void draw() {
        if (hasPickaxeEquipped) {
            drawPickaxeAnimation();
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
            lookX = -1;
        }
        if (moveDir.x() > 0) {
            currentRow = 1;  // moving right
            lookX = 1;
        }
    }

    private void move(Vector2 moveDir, float dt) {
        float nextX = position.x() + speed * moveDir.x() * dt;
        float nextY = position.y();

        Rectangle nextRecX = newRectangle(
                nextX - playerHitboxWidth / 2f,
                nextY + playerHitboxOffsetY,
                playerHitboxWidth,
                playerHitboxHeight
        );

        if (!collidesWithBuildings(nextRecX)) {
            position.x(Math.round(nextX));
        }

        nextX = position.x();
        nextY = position.y() + speed * moveDir.y() * dt;

        Rectangle nextRecY = newRectangle(
                nextX - playerHitboxWidth / 2f,
                nextY + playerHitboxOffsetY,
                playerHitboxWidth,
                playerHitboxHeight
        );

        if (!collidesWithBuildings(nextRecY)) {
            position.y(Math.round(nextY));
        }
    }

    private boolean collidesWithBuildings(Rectangle rect) {
        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionRecs(rect, building.getRect())) {
                return true;
            }
        }
        return false;
    }

    private void pushOutOfStones() {
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            float radius = ResourceNode.stoneRadius;

            float closestX = Math.max(playerRec.x(), Math.min(stoneCenter.x(), playerRec.x() + playerRec.width()));
            float closestY = Math.max(playerRec.y(), Math.min(stoneCenter.y(), playerRec.y() + playerRec.height()));

            float dx = closestX - stoneCenter.x();
            float dy = closestY - stoneCenter.y();

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance > 0 && distance < radius) {
                float overlap = radius - distance;

                position.x(Math.round(position.x() + dx / distance * overlap));
                position.y(Math.round(position.y() + dy / distance * overlap));

                updatePlayerRect();
            }
        }
    }

    private void boundaryClamp() {
        // Make sure player does not go out of bounds
        if (position.x() < halfWidth) {
            position.x(halfWidth);
        }
        if (position.y() < halfHeight) {
            position.y(halfHeight);
        }
        if (position.x() > World.WORLD_WIDTH - halfWidth) {
            position.x(World.WORLD_WIDTH - halfWidth);
        }
        if (position.y() > World.WORLD_HEIGHT - halfHeight) {
            position.y(World.WORLD_HEIGHT - halfHeight);
        }
    }

    private void updatePlayerRect() {
        playerRec.x(position.x() - playerHitboxWidth / 2f);
        playerRec.y(position.y() + playerHitboxOffsetY);
    }

    private void updateMiningRect() {
        if (lookX == 1) { // looking right
            miningRec.x(position.x() + miningRecOffset);
        } else { // looking left
            miningRec.x(position.x() - miningRecWidth - miningRecOffset);
        }
        miningRec.y(position.y() - miningRecHeight / 2f);
    }

    private boolean isNearStone() {
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            if (CheckCollisionCircleRec(stoneCenter, ResourceNode.stoneRadius, miningRec)) {
                return true;
            }
        }
        return false;
    }

    private void updateMining(float dt) {
        if (!hasPickaxeEquipped || !isAutoMining) {
            miningTimer = 0f;
            return;
        }

        if (isNearStone()) {
            miningTimer += dt;

            if (miningTimer >= miningCooldown) {
                miningTimer = 0f;
                numStone += miningAmount;
            }
        } else {
            miningTimer = 0f;
        }
    }

    private void updateToolInput() {
        if (IsKeyPressed(KEY_R)) {
            hasPickaxeEquipped = !hasPickaxeEquipped;
            isAutoMining = false;
        }

        if (hasPickaxeEquipped && IsKeyPressed(KEY_SPACE)) {
            isAutoMining = !isAutoMining;
        }
    }

    private void updateAnimation(float dt) {
        if (isMoving) {
            frameTimer += dt;

            if (frameTimer >= frameSpeed) {
                frameTimer = 0f;

                if (hasPickaxeEquipped) {
                    pickaxeFrame = (pickaxeFrame + 1) % pickaxeFrames;
                } else {
                    currentFrame = (currentFrame + 1) % frames;
                }
            }
        } else {
            frameTimer = 0f;
            currentFrame = 0;
            pickaxeFrame = 0;
        }

        if (isAutoMining) {
            pickaxeAnimTimer += dt;
            if (pickaxeAnimTimer >= pickaxeFrameSpeed) {
                pickaxeAnimTimer = 0f;
                pickaxeDown = !pickaxeDown;
            }
        } else {
            pickaxeDown = false; // default to pickaxe in up position
            pickaxeAnimTimer = 0f;
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

    private void drawPickaxeAnimation() {
        int frameWidth = mining.width() / pickaxeFrames;
        int frameHeight = mining.height() / pickaxeRows;

        int row;

        if (lookX == 1) {
            row = pickaxeDown ? 1 : 0;
        } else {
            row = pickaxeDown ? 3 : 2;
        }

        Rectangle source = new Rectangle()
                .x(pickaxeFrame * frameWidth)
                .y(row * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        float halfW = ((float) mining.width() / pickaxeFrames) * scale / 2;
        float halfH = ((float) mining.height() / pickaxeRows) * scale / 2;

        Rectangle dest = new Rectangle()
                .x((int) (position.x() - halfW + (lookX == 1 ? pickaxeOffset : -pickaxeOffset))) // add/subtract pickaxe offset
                .y((int) (position.y() - halfH))
                .width(halfW * 2)
                .height(halfH * 2);

        Vector2 origin = newVector2(0, 0);

        DrawTexturePro(mining, source, dest, origin, 0.0f, WHITE);
    }
}
