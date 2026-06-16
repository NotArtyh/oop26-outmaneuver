package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Missile base — seek steering standard.
 * Parametri caricati da missiles.json.
 */
public final class BasicMissile extends Missile {

    public BasicMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
    }

    @Override
    public String getMissileType() { return "basic"; }
}