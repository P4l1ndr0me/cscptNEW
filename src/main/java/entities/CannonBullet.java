package entities;

import core.EntityManager;
import core.TextureManager;

import static com.raylib.Colors.WHITE;
import static com.raylib.Helpers.newRectangle;
import static com.raylib.Helpers.newVector2;
import static com.raylib.Raylib.*;

public class CannonBullet {
    private Vector2 position;
    private Vector2 velocity;
    private Enemy target;

    private final Texture texture = TextureManager.getTexture("Cannon Bullet");

    private final float speed = 300f;
    private final float scale = 1.0f;
    private final float hitRadius = 0f;

    private final int damage;
    private final float knockbackStrength;

    private boolean active = true;
    private float rotation;

    public CannonBullet(Vector2 startPosition, Enemy target, int damage, float knockbackStrength) {
        this.position = newVector2(startPosition.x(), startPosition.y());
        this.target = target;
        this.damage = damage;
        this.knockbackStrength = knockbackStrength;

        updateVelocity();
    }

    public void update(float dt) {
        if (!active || target == null || target.isDead()) {
            active = false;
            return;
        }

        updateVelocity();

        position.x(position.x() + velocity.x() * speed * dt);
        position.y(position.y() + velocity.y() * speed * dt);

        checkHit();
    }

    private void updateVelocity() {
        Vector2 targetCenter = target.getHitCenter();

        float dx = targetCenter.x() - position.x();
        float dy = targetCenter.y() - position.y();

        Vector2 direction = newVector2(dx, dy);

        if (Vector2Length(direction) > 0) {
            direction = Vector2Normalize(direction);
        }

        velocity = direction;

        rotation = (float) Math.toDegrees(Math.atan2(direction.y(), direction.x()));
    }

    private void checkHit() {
        Vector2 targetCenter = target.getHitCenter();

        float distance = Vector2Distance(position, targetCenter);

        if (distance <= hitRadius + target.getHitRadius()) {
            target.takeDamage(damage);
            target.applyKnockback(velocity, knockbackStrength);

            if (target.isDead()) {
                EntityManager.spawnedEnemies.remove(target);
            }

            active = false;
        }
    }

    public void draw() {
        if (!active) {
            return;
        }

        Rectangle source = newRectangle(
                0,
                0,
                texture.width(),
                texture.height()
        );

        Rectangle dest = newRectangle(
                position.x(),
                position.y(),
                texture.width() * scale,
                texture.height() * scale
        );

        Vector2 origin = newVector2(
                texture.width() * scale / 2f,
                texture.height() * scale / 2f
        );

        DrawTexturePro(
                texture,
                source,
                dest,
                origin,
                rotation,
                WHITE
        );
    }

    public boolean isActive() {
        return active;
    }
}
