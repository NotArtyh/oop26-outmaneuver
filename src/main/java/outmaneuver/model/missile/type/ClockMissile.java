package outmaneuver.model.missile.type;

import java.util.List;

import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Quando collide rallenta tutti i missili attivi.
 */
public final class ClockMissile extends Missile {

    private final double slowFactor;
    private final double slowDuration;

    public ClockMissile(final double x, final double y, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
        this.slowFactor   = data.slowFactor();
        this.slowDuration = data.slowDuration();
    }

    public void triggerSlow(final List<IMissile> others) {
        for (final IMissile other : others) {
            if (!other.isAlive() || other.equals(this)) continue;
            other.slowDown(slowFactor, slowDuration);
        }
        destroy();
    }

    public double getSlowFactor()   { return slowFactor; }
    public double getSlowDuration() { return slowDuration; }

    @Override
    public String getMissileType() { return "clock"; }
}