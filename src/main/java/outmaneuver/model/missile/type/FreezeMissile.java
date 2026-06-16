package outmaneuver.model.missile.type;

import java.util.List;

import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Quando collide congela tutti i missili nel raggio.
 */
public final class FreezeMissile extends Missile {

    private final MissileData data;

    public FreezeMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
        this.data = data;
    }

    @Override
    public void onCollision(final List<IMissile> activeMissiles) {
        for (final IMissile other : activeMissiles) {
            if (!other.isAlive() || other.equals(this)) continue;
            final Vector2 delta = other.getHitbox().getCenter()
                    .add(getHitbox().getCenter().scale(-1));
            if (delta.magnitude() < data.freezeRadius()) {
                other.freeze(data.freezeDuration());
            }
        }
        destroy();
    }

    @Override
    protected double getFreezeRadius() { return data.freezeRadius(); }

    @Override
    public String getMissileType() { return "freeze"; }
}