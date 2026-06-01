package entities;

import buildings.Building;
import core.EntityManager;
import core.TextureManager;
import core.WeaponManager;
import world.*;

import static com.raylib.Raylib.*;
import static com.raylib.Helpers.*;
import static com.raylib.Colors.*;

import java.util.ArrayList;

public class Player extends Entity {
    // Hitbox and resource rectangles
    public static Rectangle playerRec;
    public static Rectangle miningRec;

    // Player resources
    public static int numStone = 100000;
    public static int numGold = 100000;
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
    private final int miningRecWidth = 25;
    private final int miningRecHeight = 30;
    private final int miningRecOffset = 10;

    // Mining stats (upgradable)
    private static int miningDamage = 15;      // stones per hit
    private static float miningSpeed = 0.8f;   // seconds between hits

    // Mining animation
    private static Texture mining = TextureManager.getTexture("mining1");

    // Sword animation (tiered like mining)
    private static Texture swordTexture = null;

    // Weapon tiers
    private static int pickaxeTier = 1; // 1 = stone, 2 = iron, 3 = diamond
    private static int swordTier = 0;
    private static int bowTier = 0;

    // Pickaxe animation frames
    private int pickaxeFrame = 0;
    private final int pickaxeRows = 4;
    private final int pickaxeFrames = 3;
    private float pickaxeAnimTimer = 0f;
    private final float pickaxeFrameSpeed = 0.20f;
    private boolean pickaxeDown = false;
    private final float pickaxeOffset = 3 * scale;

    // Sword combat
    private boolean hasSwordEquipped = false;  // F to toggle between pickaxe and sword
    private float attackRadius = 50f;          // Radius for sword attack
    private float attackCooldown = 0.5f;       // Seconds between attacks
    private float attackTimer = 0f;
    private boolean isAttacking = false;
    private float attackAnimTimer = 0f;
    private final float attackAnimDuration = 0.2f;

    // Slash effect animation (handles attack visuals)
    private static Texture slashWest;
    private static Texture slashEast;
    private int slashFrame = 0;
    private float slashAnimTimer = 0f;
    private boolean isSlashing = false;
    private final int slashFrames = 5;
    private final float slashFrameSpeed = 0.05f; // 5 frames * 0.05 = 0.25 sec total

    // Debug
    private final boolean showDebugHitbox = true;


    // Updates sword attack input and cooldown
    private void updateSwordCombat(float dt) {
        // Update attack cooldown
        if (attackTimer > 0) {
            attackTimer -= dt;
        }

        // Update attack animation timer
        if (isAttacking) {
            attackAnimTimer += dt;
            if (attackAnimTimer >= attackAnimDuration) {
                isAttacking = false;
                attackAnimTimer = 0f;
            }
        }

        // Only allow attack if: Sword is equipped (F key), Player actually owns a sword, Attack is off cooldown, space bar pressed
        if (hasSwordEquipped && hasSword() && attackTimer <= 0 && IsKeyPressed(KEY_SPACE)) {
            performSwordAttack();
            attackTimer = attackCooldown;
            isAttacking = true;
            // Start slash effect
            isSlashing = true;
            slashFrame = 0;
            slashAnimTimer = 0f;
        }
    }

    // Performs sword attack, damaging enemies in front of player
    private void performSwordAttack() {
        int swordDamage = WeaponManager.getSwordDamage();
        if (swordDamage <= 0) return;

        Vector2 playerCenter = getPlayerCenter();
        int enemiesHit = 0;

        // lookX: 1 = facing east (right), -1 = facing west (left)
        int facing = lookX;

        // Check all spawned enemies
        for (int i = EntityManager.spawnedEnemies.size() - 1; i >= 0; i--) {
            Enemy enemy = EntityManager.spawnedEnemies.get(i);
            Vector2 enemyCenter = enemy.getHitCenter();

            // Vector from player to enemy
            float dx = enemyCenter.x() - playerCenter.x();
            float dy = enemyCenter.y() - playerCenter.y();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= attackRadius) {
                boolean inFront = false;

                if (facing == 1) { // facing east → enemies with dx > 0 (right side)
                    inFront = dx > 0;
                } else if (facing == -1) { // facing west → enemies with dx < 0 (left side)
                    inFront = dx < 0;
                }

                if (inFront) {
                    enemy.takeDamage(swordDamage);
                    enemiesHit++;

                    // Knockback away from player
                    Vector2 knockbackDir = newVector2(dx, dy);
                    if (Vector2Length(knockbackDir) > 0) {
                        knockbackDir = Vector2Normalize(knockbackDir);
                    }
                    enemy.applyKnockback(knockbackDir, 15f);
                }
            }
        }

        if (enemiesHit > 0) {
            addDamagePopup(playerCenter, swordDamage, enemiesHit);
        }
    }

    // Returns the center position of the player (for attack radius)
    private Vector2 getPlayerCenter() {
        return newVector2(
                position.x(),
                position.y() - 10f  // Adjust to chest/head level
        );
    }

    // Shows floating damage text when hitting enemies
    private void addDamagePopup(Vector2 center, int damage, int hitCount) {
        String text;
        if (hitCount > 1) {
            text = hitCount + " enemies hit!";
        } else {
            text = damage + " damage!";
        }
        miningPopups.add(new MiningPopup(center, text));
    }

    // Floating text popups for mining gains
    private static class MiningPopup {
        Vector2 position;
        String text;
        float timer;

        MiningPopup(Vector2 pos, String text) {
            this.position = pos;
            this.text = text;
            this.timer = 1.0f;    // lifetime in seconds
        }

        void update(float dt) {
            timer -= dt;
            position.y(position.y() - 25 * dt); // float upward
        }

        boolean isAlive() {
            return timer > 0;
        }
    }

    private static ArrayList<MiningPopup> miningPopups = new ArrayList<>();

    // Adds a new floating text popup at the given position
    private static void addMiningPopup(Vector2 position, int amount) {
        miningPopups.add(new MiningPopup(position, "+" + amount));
    }

    // Updates all active popups and removes expired ones
    private void updatePopups(float dt) {
        // Loop backwards so we can remove while iterating
        for (int i = miningPopups.size() - 1; i >= 0; i--) {
            miningPopups.get(i).update(dt);
            if (!miningPopups.get(i).isAlive()) {
                miningPopups.remove(i); // Remove expired popup
            }
        }
    }

    // Draws all active floating text popups
    private void drawPopups() {
        for (MiningPopup popup : miningPopups) {
            Color color = newColor(66, 255, 87, 255); // Bright green
            DrawTextEx(
                    core.Main.pixelFont,
                    popup.text,
                    newVector2(popup.position.x(), popup.position.y()),
                    30,
                    0.5f,
                    color
            );
        }
    }

    // Player constructor - sets initial position and hitbox
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

        halfWidth = ((float) texture.width() / cols) * scale / 2;
        halfHeight = ((float) texture.height() / rows) * scale / 2;

        // Initialize player hitbox rectangle
        playerRec = newRectangle(
                position.x() - playerHitboxWidth / 2f,
                position.y() + playerHitboxOffsetY,
                playerHitboxWidth,
                playerHitboxHeight);

        // Initialize mining detection rectangle
        miningRec = newRectangle(
                position.x() + 10,
                position.y() - miningRecHeight / 2f,
                miningRecWidth,
                miningRecHeight
        );

        // Load slash effect textures
        slashWest = TextureManager.getTexture("attackWest");
        slashEast = TextureManager.getTexture("attackEast");

        // Load initial sword texture (if any)
        updateSwordTexture();
    }

    // Updates sword texture based on current tier (sword sprite is 3x2)
    private static void updateSwordTexture() {
        if (swordTier > 0) {
            Texture newTex = TextureManager.getTexture("sword" + swordTier);
            if (newTex != null) {
                swordTexture = newTex;
            }
        } else {
            swordTexture = null;
        }
    }

    public void update(float dt) {
        Vector2 moveDir = getMovementInput();

        updateToolInput();
        updateDirection(moveDir);

        updateSwordCombat(dt);
        updateSlashAnimation(dt);

        move(moveDir, dt);
        boundaryClamp();
        updatePlayerRect();

        pushOutOfStones();
        boundaryClamp();
        updatePlayerRect();

        updateMiningRect();
        updateMining(dt);
        updateAnimation(dt);
        updatePopups(dt);
    }

    // Draws the player with appropriate animation (walking or mining)
    public void draw() {
        if (hasPickaxeEquipped) {
            drawPickaxeAnimation();
        } else if (hasSwordEquipped) {
            drawSwordAnimation();
        } else {
            drawWalkingAnimation();
        }
        drawSlashEffect(); // Draw the slash effect on top of the player
        drawPopups();
    }

    // Gets WASD movement input and returns normalized direction vector
    private Vector2 getMovementInput() {
        float moveX = 0, moveY = 0;

        // Read keyboard input
        if (IsKeyDown(KEY_W)) moveY -= 1;
        if (IsKeyDown(KEY_S)) moveY += 1;
        if (IsKeyDown(KEY_A)) moveX -= 1;
        if (IsKeyDown(KEY_D)) moveX += 1;

        Vector2 moveDir = newVector2(moveX, moveY);

        // Normalize diagonal movement to maintain consistent speed
        if (Vector2Length(moveDir) != 0) {
            isMoving = true;
            return Vector2Normalize(moveDir);
        }

        isMoving = false;
        return moveDir;
    }

    // Handles tool equipping (R for pickaxe, F for sword)
    private void updateToolInput() {
        // R toggles pickaxe (mining mode)
        if (IsKeyPressed(KEY_R)) {
            hasPickaxeEquipped = !hasPickaxeEquipped;
            hasSwordEquipped = false;
            isAutoMining = false;
        }

        // F toggles sword (combat mode) – only if sword is owned
        if (IsKeyPressed(KEY_G) && hasSword()) {
            hasSwordEquipped = !hasSwordEquipped;
            hasPickaxeEquipped = false;
            isAutoMining = false;
            System.out.println("Sword equipped: " + hasSwordEquipped);
        }

        // Space toggles auto-mining when pickaxe is equipped
        if (hasPickaxeEquipped && IsKeyPressed(KEY_SPACE)) {
            isAutoMining = !isAutoMining;
        }
    }

    // Updates animation row based on movement direction
    private void updateDirection(Vector2 moveDir) {
        if (moveDir.x() < 0) {
            currentRow = 2;  // moving left
            lookX = -1;
        }
        if (moveDir.x() > 0) {
            currentRow = 1;  // moving right
            lookX = 1;
        }
        // Moving up/down keeps current row (no horizontal flip)
    }

    // Moves the player with collision detection (X then Y)
    private void move(Vector2 moveDir, float dt) {
        // Try X movement first
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

        // Then try Y movement
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

    // Checks if a rectangle collides with any placed building
    private boolean collidesWithBuildings(Rectangle rect) {
        for (Building building : EntityManager.placedBuildings) {
            if (CheckCollisionRecs(rect, building.getRect())) {
                return true; // Hit a building
            }
        }
        return false;
    }

    // Pushes player away from stone nodes to prevent walking through them
    private void pushOutOfStones() {
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            float radius = ResourceNode.STONE_RADIUS;

            // Find closest point on player rect to stone center
            float closestX = Math.max(playerRec.x(), Math.min(stoneCenter.x(), playerRec.x() + playerRec.width()));
            float closestY = Math.max(playerRec.y(), Math.min(stoneCenter.y(), playerRec.y() + playerRec.height()));

            float dx = closestX - stoneCenter.x();
            float dy = closestY - stoneCenter.y();

            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            // If colliding, push player away
            if (distance > 0 && distance < radius) {
                float overlap = radius - distance;

                // Minimum push threshold to prevent tiny movements
                if (Math.abs(dx) < 7f) {
                    if (dx < 0) dx = -7f;
                    else if (dx > 0) dx = 7f;
                    else dx = 7f;
                }

                if (Math.abs(dy) < 7f) {
                    if (dy < 0) dy = -7f;
                    else if (dy > 0) dy = 7f;
                    else dy = 7f;
                }

                float pushX = dx / distance * overlap;
                float pushY = dy / distance * overlap;

                applySafeStonePush(pushX, pushY);
                updatePlayerRect();
            }
        }
    }

    // Gradually applies stone push force to avoid clipping
    private void applySafeStonePush(float pushX, float pushY) {
        float originalX = position.x();
        float originalY = position.y();
        int steps = 10;

        // Try diagonal push first (full strength to weakest)
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float testX = originalX + pushX * scale;
            float testY = originalY + pushY * scale;

            Rectangle testRec = newRectangle(
                    testX - playerHitboxWidth / 2f,
                    testY + playerHitboxOffsetY,
                    playerHitboxWidth,
                    playerHitboxHeight
            );

            if (!collidesWithBuildings(testRec)) {
                position.x(Math.round(testX));
                position.y(Math.round(testY));
                updatePlayerRect();
                return;
            }
        }

        // Try X-only push if diagonal fails
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float testX = originalX + pushX * scale;

            Rectangle testRec = newRectangle(
                    testX - playerHitboxWidth / 2f,
                    originalY + playerHitboxOffsetY,
                    playerHitboxWidth,
                    playerHitboxHeight
            );

            if (!collidesWithBuildings(testRec)) {
                position.x(Math.round(testX));
                updatePlayerRect();
                return;
            }
        }

        // Try Y-only push as last resort
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float testY = originalY + pushY * scale;

            Rectangle testRec = newRectangle(
                    originalX - playerHitboxWidth / 2f,
                    testY + playerHitboxOffsetY,
                    playerHitboxWidth,
                    playerHitboxHeight
            );

            if (!collidesWithBuildings(testRec)) {
                position.y(Math.round(testY));
                updatePlayerRect();
                return;
            }
        }
        // If all pushes fail, don't move
    }

    // Keeps player within world boundaries
    private void boundaryClamp() {
        if (position.x() < halfWidth) position.x(halfWidth);
        if (position.y() < halfHeight) position.y(halfHeight);
        if (position.x() > World.WORLD_WIDTH - halfWidth) position.x(World.WORLD_WIDTH - halfWidth);
        if (position.y() > World.WORLD_HEIGHT - halfHeight) position.y(World.WORLD_HEIGHT - halfHeight);
    }

    // Updates player hitbox position to match current world position
    private void updatePlayerRect() {
        playerRec.x(position.x() - playerHitboxWidth / 2f);
        playerRec.y(position.y() + playerHitboxOffsetY);
    }

    // Updates mining rectangle position based on facing direction
    private void updateMiningRect() {
        if (lookX == 1) {
            miningRec.x(position.x() + miningRecOffset); // Right side
        } else {
            miningRec.x(position.x() - miningRecWidth - miningRecOffset); // Left side
        }
        miningRec.y(position.y() - miningRecHeight / 2f); // Center on player vertically
    }

    // Checks if mining rectangle overlaps any stone node
    private boolean isNearStone() {
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            if (CheckCollisionCircleRec(stoneCenter, ResourceNode.STONE_RADIUS, miningRec)) {
                return true; // Found a stone in range
            }
        }
        return false;
    }

    // Handles auto-mining logic and adds stone resources
    private void updateMining(float dt) {
        if (!hasPickaxeEquipped || !isAutoMining) {
            miningTimer = 0f;
            return;
        }

        if (isNearStone()) {
            miningTimer += dt;
            if (miningTimer >= miningSpeed) {
                miningTimer = 0f;
                numStone += miningDamage;
                // Show floating "+X" text above mining area
                addMiningPopup(newVector2(miningRec.x(), miningRec.y() - 30), miningDamage);
            }
        } else {
            miningTimer = 0f; // Reset timer when not near stone
        }
    }

    // Updates player and pickaxe animation frames
    private void updateAnimation(float dt) {
        if (isMoving) {
            frameTimer += dt;
            if (frameTimer >= frameSpeed) {
                frameTimer = 0f;
                if (hasPickaxeEquipped) {
                    pickaxeFrame = (pickaxeFrame + 1) % pickaxeFrames; // Cycle pickaxe frames
                } else {
                    currentCol = (currentCol + 1) % cols; // Cycle walking frames
                }
            }
        } else {
            // Idle - reset animations
            frameTimer = 0f;
            currentCol = 0;
            pickaxeFrame = 0;
        }

        // Animate pickaxe swinging when auto-mining
        if (isAutoMining) {
            pickaxeAnimTimer += dt;
            if (pickaxeAnimTimer >= pickaxeFrameSpeed) {
                pickaxeAnimTimer = 0f;
                pickaxeDown = !pickaxeDown; // Toggle up/down
            }
        } else {
            pickaxeDown = false;
            pickaxeAnimTimer = 0f;
        }
    }

    // Updates slash effect animation frames
    private void updateSlashAnimation(float dt) {
        if (!isSlashing) return;

        slashAnimTimer += dt;
        if (slashAnimTimer >= slashFrameSpeed) {
            slashAnimTimer = 0f;
            slashFrame++;
            if (slashFrame >= slashFrames) {
                isSlashing = false;
                slashFrame = 0;
            }
        }
    }

    // Draws player walking animation without pickaxe
    private void drawWalkingAnimation() {
        int frameWidth = texture.width() / cols;
        int frameHeight = texture.height() / rows;

        Rectangle source = new Rectangle()
                .x(currentCol * frameWidth)
                .y(currentRow * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        Rectangle dest = new Rectangle()
                .x((int) (position.x() - halfWidth))
                .y((int) (position.y() - halfHeight))
                .width(halfWidth * 2)
                .height(halfHeight * 2);

        DrawTexturePro(texture, source, dest, newVector2(0, 0), 0.0f, WHITE);
    }

    // Draws player with pickaxe mining animation
    private void drawPickaxeAnimation() {
        int frameWidth = mining.width() / pickaxeFrames;
        int frameHeight = mining.height() / pickaxeRows;

        // Determine which row based on facing direction and swing state
        int row;
        if (lookX == 1) {
            row = pickaxeDown ? 1 : 0;  // Right-facing: up/down rows
        } else {
            row = pickaxeDown ? 3 : 2;  // Left-facing: up/down rows
        }

        Rectangle source = new Rectangle()
                .x(pickaxeFrame * frameWidth)
                .y(row * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        float halfW = ((float) mining.width() / pickaxeFrames) * scale / 2;
        float halfH = ((float) mining.height() / pickaxeRows) * scale / 2;

        // Offset pickaxe position based on facing direction
        Rectangle dest = new Rectangle()
                .x((int) (position.x() - halfW + (lookX == 1 ? pickaxeOffset : -pickaxeOffset)))
                .y((int) (position.y() - halfH))
                .width(halfW * 2)
                .height(halfH * 2);

        DrawTexturePro(mining, source, dest, newVector2(0, 0), 0.0f, WHITE);
    }

    // Draws player with sword walking animation using tiered sword textures (3x2 sprite sheet)
    private void drawSwordAnimation() {
        if (swordTexture == null) return;

        // Sword texture is 3 columns, 2 rows
        int frameWidth = swordTexture.width() / 3;
        int frameHeight = swordTexture.height() / 2;

        // Row 0 = walking east (right), Row 1 = walking west (left)
        int row = (lookX == 1) ? 0 : 1;

        int frame = currentCol;

        Rectangle source = new Rectangle()
                .x(frame * frameWidth)
                .y(row * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        // Compute destination based on sword frame size (not player texture)
        float halfW = frameWidth * scale / 2f;
        float halfH = frameHeight * scale / 2f;

        // Offset sword position based on facing direction
        Rectangle dest = new Rectangle()
                .x((int) (position.x() - halfW + (lookX == 1 ? 3 : -3)))
                .y((int) (position.y() - halfH))
                .width(halfW * 2)
                .height(halfH * 2);

        DrawTexturePro(swordTexture, source, dest, newVector2(0, 0), 0.0f, WHITE);
    }

    // Draws slash effect animation when attacking
    private void drawSlashEffect() {
        if (!isSlashing) return;

        // Choose the correct texture and frame order
        Texture tex;
        int frameIndex;

        if (lookX == 1) { // facing east → use east texture (frames from right to left)
            tex = slashEast;
            // Reverse frame index because the sheet goes right to left
            frameIndex = (slashFrames - 1) - slashFrame;
        } else { // facing west → use west texture (frames left to right, normal order)
            tex = slashWest;
            frameIndex = slashFrame;
        }

        if (tex == null) return;

        int frameWidth = tex.width() / slashFrames;
        int frameHeight = tex.height();

        Rectangle source = new Rectangle()
                .x(frameIndex * frameWidth)
                .y(0)
                .width(frameWidth)
                .height(frameHeight);

        // Position the slash slightly in front of the player
        float offsetX;
        if (lookX == 1) {
            offsetX = halfWidth + 10;          // in front of right side
        } else {
            offsetX = -halfWidth - frameWidth * scale - 10; // in front of left side
        }
        float offsetY = -halfHeight - 10;      // above player's head

        Rectangle dest = new Rectangle()
                .x((int) (position.x() + offsetX))
                .y((int) (position.y() + offsetY))
                .width(frameWidth * scale)
                .height(frameHeight * scale);

        DrawTexturePro(tex, source, dest, newVector2(0, 0), 0.0f, WHITE);
    }

    // Getters for weapon tiers
    public static int getPickaxeTier() { return pickaxeTier; }
    public static int getSwordTier() { return swordTier; }
    public static int getBowTier() { return bowTier; }

    // Upgrades pickaxe with weapon stats and updates mining speed/animation
    public static void upgradePickaxe(Weapon weapon) {
        pickaxeTier = weapon.getTier();
        miningDamage = weapon.getDamage();
        // Faster mining = lower cooldown (divide base by attack speed)
        float baseCooldown = 0.8f;
        miningSpeed = baseCooldown / weapon.getAttackSpeed();
        // Load the new mining animation sprite sheet for this tier
        Texture newTex = TextureManager.getTexture("mining" + pickaxeTier);
        if (newTex != null) {
            mining = newTex;
        }
    }

    // Upgrades sword tier - sets tier and updates sword texture
    public static void upgradeSword(Weapon weapon) {
        swordTier = weapon.getTier();
        updateSwordTexture();
    }

    // Upgrades bow tier
    public static void upgradeBow(Weapon weapon) {
        if (bowTier < 3) bowTier++;
    }

    // Returns current mining texture
    public static Texture getCurrentMiningTexture() {
        return mining;
    }

    // Returns true if player has purchased at least a tier 1 sword
    private boolean hasSword() {
        return swordTier > 0;
    }

    // Resets player to starting state for game restart
    public void reset() {
        position = newVector2(World.WORLD_WIDTH / 2f, World.WORLD_HEIGHT / 2f);
        numStone = 0;
        numGold = 0;
        health = 100;
        pickaxeTier = 1;
        miningDamage = 15;
        miningSpeed = 0.8f;
        mining = TextureManager.getTexture("mining1");
        swordTier = 0;
        bowTier = 0;
        updateSwordTexture();
        hasPickaxeEquipped = false;
        isAutoMining = false;
        miningTimer = 0f;
        pickaxeFrame = 0;
        pickaxeDown = false;
        pickaxeAnimTimer = 0f;
        currentCol = 0;
        currentRow = 1;
        lookX = 1;
        isMoving = false;
        frameTimer = 0f;
        miningPopups.clear();
        isSlashing = false;
        slashFrame = 0;
        slashAnimTimer = 0f;
        updatePlayerRect();
        updateMiningRect();
    }
}