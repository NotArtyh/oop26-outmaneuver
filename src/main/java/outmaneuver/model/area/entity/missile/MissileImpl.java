package outmaneuver.model.area.entity.missile;

import java.util.ArrayList;
import java.util.List;

import java.awt.Dimension;

import outmaneuver.model.area.collision.CollisionLayer;
import outmaneuver.model.area.collision.Hitbox;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;
import outmaneuver.view.MissileRenderData;

public abstract class MissileImpl implements Missile {

    // --- POSIZIONE E MOVIMENTO ---
    private Vector2 position;
    private Vector2 velocity;

    // --- PARAMETRI BASE ---
    protected final double speed;
    protected final double maxTurnAngle;
    protected final double hitboxRadius;
    private final double maxLifetime;

    // --- REDIRECT ---
    private final double predictionTime;
    private final int outOfBoundsMargin;

    // --- STATO ---
    private boolean alive;
    private double lifetime;

    // --- FREEZE ---
    private boolean frozen      = false;
    private double  freezeTimer = 0;

    // --- SLOW ---
    private boolean slowed     = false;
    private double  slowTimer  = 0;
    private double  slowFactor = 1.0;

    protected MissileImpl(final Vector2 spawnPos,
                          final double speed, final double maxTurnAngle,
                          final double hitboxRadius, final double lifetime,
                          final double predictionTime, final int outOfBoundsMargin) {
        this.position           = spawnPos;
        this.velocity           = Vector2.ZERO;
        this.speed              = speed;
        this.maxTurnAngle       = maxTurnAngle;
        this.hitboxRadius       = hitboxRadius;
        this.lifetime           = lifetime;
        this.maxLifetime        = lifetime;
        this.predictionTime     = predictionTime;
        this.outOfBoundsMargin  = outOfBoundsMargin;
        this.alive              = true;
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        steer(plane.getPosition());
        move(dt);
    }

    protected final boolean shouldSkipUpdate(final double dt) {
        if (lifetime >= 0) {
            lifetime -= dt;
            if (lifetime <= 0) {
                destroy();
                return true;
            }
        }
        if (!alive) return true;

        if (frozen) {
            freezeTimer -= dt;
            if (freezeTimer <= 0) frozen = false;
            return true;
        }

        if (slowed) {
            slowTimer -= dt;
            if (slowTimer <= 0) {
                slowed     = false;
                slowFactor = 1.0;
            }
        }

        return false;
    }

    protected final void move(final double dt) {
        final double factor = slowed ? slowFactor : 1.0;
        position = position.add(velocity.scale(dt * factor));
    }

    protected void steer(final Vector2 target) {
        final double desiredAngle = target.add(position.scale(-1)).angle();
        final double currentAngle = velocity.angle();
        final double diff         = normalizeAngle(desiredAngle - currentAngle);
        final double turn         = Math.max(-maxTurnAngle, Math.min(maxTurnAngle, diff));
        velocity = Vector2.fromAngle(currentAngle + turn).scale(speed);
    }

    protected final double normalizeAngle(double a) {
        while (a >  Math.PI) a -= 2 * Math.PI;
        while (a < -Math.PI) a += 2 * Math.PI;
        return a;
    }

    @Override
    public void setInitialDirection(final Vector2 target) {
        velocity = Vector2.fromAngle(target.add(position.scale(-1)).angle()).scale(speed);
    }

    public void setVelocity(final double vx, final double vy) {
        this.velocity = new Vector2(vx, vy);
    }

    public void setVelocity(final Vector2 vel) {
        this.velocity = vel;
    }

    @Override
    public Vector2 getPosition() {
        return position;
    }

    @Override
    public void setPosition(final Vector2 pos) {
        this.position = pos;
    }

    @Override
    public void update(final long deltaMs) { }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane, final Dimension screenSize) {
        final Vector2 delta = position.add(plane.getPosition().scale(-1));
        final boolean outX = Math.abs(delta.getX()) > screenSize.width  / 2.0 + outOfBoundsMargin;
        final boolean outY = Math.abs(delta.getY()) > screenSize.height / 2.0 + outOfBoundsMargin;

        if (outX || outY) {
            final Vector2 planeVel = Vector2.fromAngle(plane.getDirection())
                    .scale(plane.getEffectiveSpeed());
            final Vector2 predicted = plane.getPosition().add(planeVel.scale(predictionTime));
            setInitialDirection(predicted);
            return true;
        }
        return false;
    }

    public int getOutOfBoundsMargin() { return outOfBoundsMargin; }

    @Override
    public void onCollision(final List<Missile> activeMissiles) { destroy(); }

    @Override
    public void checkBounce(final Vector2 planePos, final Dimension screenSize) { }

    protected double getFreezeRadius() { return 0; }

    @Override
    public List<Missile> getSpawnOnInit() { return new ArrayList<>(); }

    @Override
    public void destroy()    { this.alive = false; }

    @Override
    public boolean isAlive() { return alive; }

    @Override
    public void freeze(final double duration) {
        this.frozen      = true;
        this.freezeTimer = duration;
    }

    public boolean isFrozen() { return frozen; }

    @Override
    public void slowDown(final double factor, final double duration) {
        this.slowed     = true;
        this.slowFactor = factor;
        this.slowTimer  = duration;
    }

    public boolean isSlowed() { return slowed; }

    @Override
    public boolean collidesWith(final Plane plane) {
        final double dist = plane.getPosition().add(position.scale(-1)).magnitude();
        return dist < hitboxRadius + plane.getStats().getHitboxRadius();
    }

    protected final boolean destroyIfOffScreen(final Plane plane, final Dimension screenSize) {
        if (isOffScreen(plane, screenSize)) {
            destroy();
        }
        return false;
    }

    public boolean isOffScreen(final Plane plane, final Dimension screenSize) {
        final Vector2 delta = position.add(plane.getPosition().scale(-1));
        return Math.abs(delta.getX()) > screenSize.width  / 2.0 + outOfBoundsMargin
            || Math.abs(delta.getY()) > screenSize.height / 2.0 + outOfBoundsMargin;
    }

    @Override
    public Hitbox getHitbox() {
        return new Hitbox(position, hitboxRadius);
    }

    @Override
    public CollisionLayer getCollisionLayer() {
        return CollisionLayer.MISSILE;
    }

    public double getWorldX()       { return position.getX(); }
    public double getWorldY()       { return position.getY(); }
    public Vector2 getVelocity()    { return velocity; }
    public double getVx()           { return velocity.getX(); }
    public double getVy()           { return velocity.getY(); }
    public double getSpeed()        { return speed; }
    @Override
    public double getHitboxRadius() { return hitboxRadius; }

    protected double getMaxLifetime() { return maxLifetime; }

    @Override
    public boolean isGhostVisible() { return true; }

    @Override
    public MissileRenderData getRenderData() {
        return new MissileRenderData(
                position.getX(), position.getY(),
                velocity.getX(), velocity.getY(),
                hitboxRadius,
                maxLifetime > 0 ? lifetime / maxLifetime : -1,
                getMissileType(),
                isGhostVisible(),
                getFreezeRadius());
    }

    @Override
    public abstract String getMissileType();
}
