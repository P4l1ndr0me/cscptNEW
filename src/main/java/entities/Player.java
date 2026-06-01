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
    public static int numStone = 0;
    public static int numGold = 0;
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

    // Mining animation (changes with pickaxe tier)
    private static Texture mining = TextureManager.getTexture("mining1");

    // Sword animation (tiered like mining)
    private static Texture swordTexture = null;

    // Weapon tiers (0 = not owned)
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

    // Sword combat (manual attack with cooldown)
    private boolean hasSwordEquipped = false;  // G to toggle
    private float attackRadius = 50f;
    private float attackCooldown = 0.3f;       // low cooldown for spamming
    private float attackTimer = 0f;             // seconds until next attack allowed
    private boolean isAttacking = false;
    private float attackAnimTimer = 0f;
    private final float attackAnimDuration = 0.2f;

    // Slash effect animation (plays on top of player when attacking)
    private static Texture slashWest;
    private static Texture slashEast;
    private int slashFrame = 0;
    private float slashAnimTimer = 0f;
    private boolean isSlashing = false;
    private final int slashFrames = 5;
    private final float slashFrameSpeed = 0.05f; // 5 frames * 0.05 = 0.25 sec total

    // Debug
    private final boolean showDebugHitbox = true;

    // --- Sword combat (manual attack on SPACE press) ---
    private void updateSwordCombat(float dt) {
        // Decrease attack cooldown timer
        if (attackTimer > 0) {
            attackTimer -= dt;
            if (attackTimer < 0) attackTimer = 0;
        }

        // Attack animation timer
        if (isAttacking) {
            attackAnimTimer += dt;
            if (attackAnimTimer >= attackAnimDuration) {
                isAttacking = false;
                attackAnimTimer = 0f;
            }
        }

        // Perform attack when SPACE is pressed and cooldown is ready
        if (hasSwordEquipped && hasSword() && attackTimer <= 0 && IsKeyPressed(KEY_SPACE)) {
            performSwordAttack();
            attackTimer = attackCooldown;  // reset cooldown
            isAttacking = true;
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
        int facing = lookX;

        // Check all spawned enemies
        for (int i = EntityManager.spawnedEnemies.size() - 1; i >= 0; i--) {
            Enemy enemy = EntityManager.spawnedEnemies.get(i);
            Vector2 enemyCenter = enemy.getHitCenter();

            float dx = enemyCenter.x() - playerCenter.x();
            float dy = enemyCenter.y() - playerCenter.y();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);

            if (distance <= attackRadius) {
                boolean inFront = false;
                if (facing == 1) inFront = dx > 0;      // facing right → enemies to the right
                else if (facing == -1) inFront = dx < 0; // facing left → enemies to the left

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
        return newVector2(position.x(), position.y() - 10f);
    }

    // Shows floating damage text when hitting enemies
    private void addDamagePopup(Vector2 center, int damage, int hitCount) {
        String text = (hitCount > 1) ? hitCount + " enemies hit!" : damage + " damage!";
        miningPopups.add(new MiningPopup(center, text));
    }

    // --- Floating text popups (mining gains & damage numbers) ---
    private static class MiningPopup {
        Vector2 position;
        String text;
        float timer;
        MiningPopup(Vector2 pos, String text) {
            this.position = pos;
            this.text = text;
            this.timer = 1.0f;
        }
        void update(float dt) {
            timer -= dt;
            position.y(position.y() - 25 * dt); // float upward
        }
        boolean isAlive() { return timer > 0; }
    }

    private static ArrayList<MiningPopup> miningPopups = new ArrayList<>();

    private static void addMiningPopup(Vector2 position, int amount) {
        miningPopups.add(new MiningPopup(position, "+" + amount));
    }

    private void updatePopups(float dt) {
        for (int i = miningPopups.size() - 1; i >= 0; i--) {
            miningPopups.get(i).update(dt);
            if (!miningPopups.get(i).isAlive()) miningPopups.remove(i);
        }
    }

    private void drawPopups() {
        for (MiningPopup popup : miningPopups) {
            Color color = newColor(66, 255, 87, 255); // bright green
            DrawTextEx(core.Main.pixelFont, popup.text,
                    newVector2(popup.position.x(), popup.position.y()), 30, 0.5f, color);
        }
    }

    // Player constructor
    public Player() {
        super(newVector2(World.WORLD_WIDTH / 2f, World.WORLD_HEIGHT / 2f),
                2.0f, 250.0f, TextureManager.getTexture("playerNEW"), 3, 3);
        frameSpeed = 0.15f;
        currentRow = 1;
        lookX = 1;
        halfWidth = ((float) texture.width() / cols) * scale / 2;
        halfHeight = ((float) texture.height() / rows) * scale / 2;
        playerRec = newRectangle(position.x() - playerHitboxWidth / 2f,
                position.y() + playerHitboxOffsetY,
                playerHitboxWidth, playerHitboxHeight);
        miningRec = newRectangle(position.x() + 10, position.y() - miningRecHeight / 2f,
                miningRecWidth, miningRecHeight);
        slashWest = TextureManager.getTexture("attackWest");
        slashEast = TextureManager.getTexture("attackEast");
        updateSwordTexture();
    }

    // Updates sword texture based on current tier (sword sprite is 3x2)
    private static void updateSwordTexture() {
        if (swordTier > 0) {
            Texture newTex = TextureManager.getTexture("sword" + swordTier);
            if (newTex != null) swordTexture = newTex;
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

    public void draw() {
        if (hasPickaxeEquipped) drawPickaxeAnimation();
        else if (hasSwordEquipped) drawSwordAnimation();
        else drawWalkingAnimation();
        drawSlashEffect(); // always on top
        drawPopups();
    }

    // Gets WASD input, returns normalized direction vector
    private Vector2 getMovementInput() {
        float moveX = 0, moveY = 0;
        if (IsKeyDown(KEY_W)) moveY -= 1;
        if (IsKeyDown(KEY_S)) moveY += 1;
        if (IsKeyDown(KEY_A)) moveX -= 1;
        if (IsKeyDown(KEY_D)) moveX += 1;
        Vector2 moveDir = newVector2(moveX, moveY);
        if (Vector2Length(moveDir) != 0) {
            isMoving = true;
            return Vector2Normalize(moveDir);
        }
        isMoving = false;
        return moveDir;
    }

    // Handles tool switching (R = pickaxe, G = sword)
    private void updateToolInput() {
        if (IsKeyPressed(KEY_R)) {
            hasPickaxeEquipped = !hasPickaxeEquipped;
            hasSwordEquipped = false;
            isAutoMining = false;
        }
        if (IsKeyPressed(KEY_G) && hasSword()) {
            hasSwordEquipped = !hasSwordEquipped;
            hasPickaxeEquipped = false;
            isAutoMining = false;
        }
        // Space toggles auto-mining when pickaxe is equipped
        if (hasPickaxeEquipped && IsKeyPressed(KEY_SPACE)) {
            isAutoMining = !isAutoMining;
        }
        // Sword attacks are handled in updateSwordCombat (space press, no toggle)
    }

    // Changes animation row based on movement direction
    private void updateDirection(Vector2 moveDir) {
        if (moveDir.x() < 0) { currentRow = 2; lookX = -1; }
        if (moveDir.x() > 0) { currentRow = 1; lookX = 1; }
    }

    // Movement with per‑axis collision (allows sliding along walls)
    private void move(Vector2 moveDir, float dt) {
        float nextX = position.x() + speed * moveDir.x() * dt;
        Rectangle nextRecX = newRectangle(nextX - playerHitboxWidth / 2f,
                position.y() + playerHitboxOffsetY,
                playerHitboxWidth, playerHitboxHeight);
        if (!collidesWithBuildings(nextRecX)) position.x(Math.round(nextX));

        float nextY = position.y() + speed * moveDir.y() * dt;
        Rectangle nextRecY = newRectangle(position.x() - playerHitboxWidth / 2f,
                nextY + playerHitboxOffsetY,
                playerHitboxWidth, playerHitboxHeight);
        if (!collidesWithBuildings(nextRecY)) position.y(Math.round(nextY));
    }

    // Checks collision with any placed building
    private boolean collidesWithBuildings(Rectangle rect) {
        for (Building building : EntityManager.placedBuildings)
            if (CheckCollisionRecs(rect, building.getRect())) return true;
        return false;
    }

    // Pushes player away from stone nodes to prevent walking through them
    private void pushOutOfStones() {
        for (Vector2 stoneCenter : EntityManager.stoneCenters) {
            float radius = ResourceNode.STONE_RADIUS;
            float closestX = Math.max(playerRec.x(), Math.min(stoneCenter.x(), playerRec.x() + playerRec.width()));
            float closestY = Math.max(playerRec.y(), Math.min(stoneCenter.y(), playerRec.y() + playerRec.height()));
            float dx = closestX - stoneCenter.x();
            float dy = closestY - stoneCenter.y();
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (distance > 0 && distance < radius) {
                float overlap = radius - distance;
                if (Math.abs(dx) < 7f) dx = dx < 0 ? -7f : (dx > 0 ? 7f : 7f);
                if (Math.abs(dy) < 7f) dy = dy < 0 ? -7f : (dy > 0 ? 7f : 7f);
                float pushX = dx / distance * overlap;
                float pushY = dy / distance * overlap;
                applySafeStonePush(pushX, pushY);
                updatePlayerRect();
            }
        }
    }

    // Gradually applies stone push force to avoid clipping
    private void applySafeStonePush(float pushX, float pushY) {
        float origX = position.x(), origY = position.y();
        int steps = 10;
        // Try diagonal push first
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float testX = origX + pushX * scale;
            float testY = origY + pushY * scale;
            Rectangle testRec = newRectangle(testX - playerHitboxWidth / 2f,
                    testY + playerHitboxOffsetY,
                    playerHitboxWidth, playerHitboxHeight);
            if (!collidesWithBuildings(testRec)) {
                position.x(Math.round(testX));
                position.y(Math.round(testY));
                updatePlayerRect();
                return;
            }
        }
        // Try X-only push
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float testX = origX + pushX * scale;
            Rectangle testRec = newRectangle(testX - playerHitboxWidth / 2f,
                    origY + playerHitboxOffsetY,
                    playerHitboxWidth, playerHitboxHeight);
            if (!collidesWithBuildings(testRec)) {
                position.x(Math.round(testX));
                updatePlayerRect();
                return;
            }
        }
        // Try Y-only push
        for (int i = steps; i >= 1; i--) {
            float scale = i / (float) steps;
            float testY = origY + pushY * scale;
            Rectangle testRec = newRectangle(origX - playerHitboxWidth / 2f,
                    testY + playerHitboxOffsetY,
                    playerHitboxWidth, playerHitboxHeight);
            if (!collidesWithBuildings(testRec)) {
                position.y(Math.round(testY));
                updatePlayerRect();
                return;
            }
        }
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
        if (lookX == 1) miningRec.x(position.x() + miningRecOffset);
        else miningRec.x(position.x() - miningRecWidth - miningRecOffset);
        miningRec.y(position.y() - miningRecHeight / 2f);
    }

    // Checks if mining rectangle overlaps any stone node
    private boolean isNearStone() {
        for (Vector2 stoneCenter : EntityManager.stoneCenters)
            if (CheckCollisionCircleRec(stoneCenter, ResourceNode.STONE_RADIUS, miningRec)) return true;
        return false;
    }

    // Handles auto-mining logic (increment timer, add stone when threshold reached)
    private void updateMining(float dt) {
        if (!hasPickaxeEquipped || !isAutoMining) { miningTimer = 0f; return; }
        if (isNearStone()) {
            miningTimer += dt;
            if (miningTimer >= miningSpeed) {
                miningTimer = 0f;
                numStone += miningDamage;
                addMiningPopup(newVector2(miningRec.x(), miningRec.y() - 30), miningDamage);
            }
        } else {
            miningTimer = 0f;
        }
    }

    // Updates walking / pickaxe / idle animations
    private void updateAnimation(float dt) {
        if (isMoving) {
            frameTimer += dt;
            if (frameTimer >= frameSpeed) {
                frameTimer = 0f;
                if (hasPickaxeEquipped) pickaxeFrame = (pickaxeFrame + 1) % pickaxeFrames;
                else currentCol = (currentCol + 1) % cols;
            }
        } else {
            frameTimer = 0f;
            currentCol = 0;
            pickaxeFrame = 0;
        }
        // Pickaxe swing when auto-mining
        if (isAutoMining) {
            pickaxeAnimTimer += dt;
            if (pickaxeAnimTimer >= pickaxeFrameSpeed) {
                pickaxeAnimTimer = 0f;
                pickaxeDown = !pickaxeDown;
            }
        } else {
            pickaxeDown = false;
            pickaxeAnimTimer = 0f;
        }
    }

    // Updates slash effect animation frames (5‑frame sprite)
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

    // Draws player walking animation (no tool)
    private void drawWalkingAnimation() {
        int fw = texture.width() / cols, fh = texture.height() / rows;
        Rectangle src = new Rectangle().x(currentCol * fw).y(currentRow * fh).width(fw).height(fh);
        Rectangle dst = new Rectangle().x((int)(position.x() - halfWidth)).y((int)(position.y() - halfHeight))
                .width(halfWidth * 2).height(halfHeight * 2);
        DrawTexturePro(texture, src, dst, newVector2(0,0), 0, WHITE);
    }

    // Draws player with pickaxe mining animation
    private void drawPickaxeAnimation() {
        int fw = mining.width() / pickaxeFrames, fh = mining.height() / pickaxeRows;
        int row = (lookX == 1) ? (pickaxeDown ? 1 : 0) : (pickaxeDown ? 3 : 2);
        Rectangle src = new Rectangle().x(pickaxeFrame * fw).y(row * fh).width(fw).height(fh);
        float halfW = ((float) mining.width() / pickaxeFrames) * scale / 2;
        float halfH = ((float) mining.height() / pickaxeRows) * scale / 2;
        float offsetX = (lookX == 1) ? pickaxeOffset : -pickaxeOffset;
        Rectangle dst = new Rectangle().x((int)(position.x() - halfW + offsetX)).y((int)(position.y() - halfH))
                .width(halfW * 2).height(halfH * 2);
        DrawTexturePro(mining, src, dst, newVector2(0,0), 0, WHITE);
    }

    // Draws player with sword walking animation (3x2 sprite)
    private void drawSwordAnimation() {
        if (swordTexture == null) return;
        int frameWidth = swordTexture.width() / 3;
        int frameHeight = swordTexture.height() / 2;
        int row = (lookX == 1) ? 0 : 1;
        int frame = currentCol;

        Rectangle source = new Rectangle()
                .x(frame * frameWidth)
                .y(row * frameHeight)
                .width(frameWidth)
                .height(frameHeight);

        float halfW = frameWidth * scale / 2f;
        float halfH = frameHeight * scale / 2f;
        float offsetX = (lookX == 1) ? 3 : -3;

        Rectangle dest = new Rectangle()
                .x((int) (position.x() - halfW + offsetX))
                .y((int) (position.y() - halfH))
                .width(halfW * 2)
                .height(halfH * 2);

        DrawTexturePro(swordTexture, source, dest, newVector2(0, 0), 0.0f, WHITE);
    }

    // Draws slash effect overlay when attacking
    private void drawSlashEffect() {
        if (!isSlashing) return;
        Texture tex;
        int frameIndex;
        if (lookX == 1) {
            tex = slashEast;
            frameIndex = (slashFrames - 1) - slashFrame; // east sheet plays reversed
        } else {
            tex = slashWest;
            frameIndex = slashFrame; // west sheet plays normally
        }
        if (tex == null) return;
        int fw = tex.width() / slashFrames, fh = tex.height();
        Rectangle src = new Rectangle().x(frameIndex * fw).y(0).width(fw).height(fh);
        float offsetX = (lookX == 1) ? halfWidth + 10 : -halfWidth - fw * scale - 10;
        float offsetY = -halfHeight - 10;
        Rectangle dst = new Rectangle().x((int)(position.x() + offsetX)).y((int)(position.y() + offsetY))
                .width(fw * scale).height(fh * scale);
        DrawTexturePro(tex, src, dst, newVector2(0,0), 0, WHITE);
    }

    // Getters for weapon tiers
    public static int getPickaxeTier() { return pickaxeTier; }
    public static int getSwordTier() { return swordTier; }
    public static int getBowTier() { return bowTier; }

    // Upgrades pickaxe with weapon stats and updates mining speed/animation
    public static void upgradePickaxe(Weapon weapon) {
        pickaxeTier = weapon.getTier();
        miningDamage = weapon.getDamage();
        float baseCooldown = 0.8f;
        miningSpeed = baseCooldown / weapon.getAttackSpeed();
        Texture newTex = TextureManager.getTexture("mining" + pickaxeTier);
        if (newTex != null) mining = newTex;
    }

    // Upgrades sword tier - sets tier and updates sword texture
    public static void upgradeSword(Weapon weapon) {
        swordTier = weapon.getTier();
        updateSwordTexture();
    }

    // Upgrades bow tier (placeholder)
    public static void upgradeBow(Weapon weapon) {
        if (bowTier < 3) bowTier++;
    }

    public static Texture getCurrentMiningTexture() { return mining; }

    // Returns true if player has purchased at least a tier 1 sword
    private boolean hasSword() { return swordTier > 0; }

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
        hasSwordEquipped = false;
        hasPickaxeEquipped = false;
        isAutoMining = false;
        attackTimer = 0f;
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