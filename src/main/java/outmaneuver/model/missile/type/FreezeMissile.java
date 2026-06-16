package outmaneuver.model.missile.type;

import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import java.util.List;

/*
 * Quando collide congela tutti i missili nel raggio.
 */
public final class FreezeMissile extends Missile {

    private final double freezeRadius;
    private final double freezeDuration;

    public FreezeMissile(final double x, final double y, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
        this.freezeRadius   = data.freezeRadius();
        this.freezeDuration = data.freezeDuration();
    }

    public void triggerFreeze(final List<IMissile> others) {
        for (final IMissile other : others) {
            if (!other.isAlive() || other.equals(this)) continue;
            final double dx = other.getWorldX() - getWorldX();
            final double dy = other.getWorldY() - getWorldY();
            if (dx * dx + dy * dy < freezeRadius * freezeRadius) {
                other.freeze(freezeDuration);
            }
        }
        destroy();
    }

    public double getFreezeRadius() { return freezeRadius; }

    @Override
    public String getMissileType() { return "freeze"; }
}