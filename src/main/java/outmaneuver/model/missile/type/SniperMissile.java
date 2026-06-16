package outmaneuver.model.missile.type;

import java.awt.Dimension;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Direzione fissa al momento dello spawn.
 * Se esce dallo schermo viene eliminato, non rediretto.
 */
public final class SniperMissile extends Missile {

    public SniperMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane, final Dimension screenSize) {
        return destroyIfOffScreen(plane, screenSize);
    }

    @Override
    public String getMissileType() { return "sniper"; }
}
