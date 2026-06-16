package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Missile base — seek steering standard.
 * Parametri caricati da missiles.json.
 */
public final class BasicMissile extends Missile {

    public BasicMissile(final double x, final double y, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
    }

    @Override
    public String getMissileType() { return "basic"; }
}