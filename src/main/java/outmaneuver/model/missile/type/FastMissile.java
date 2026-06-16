package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Veloce e agile.
 */
public final class FastMissile extends Missile {

    public FastMissile(final double x, final double y, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
    }

    @Override
    public String getMissileType() { return "fast"; }
}