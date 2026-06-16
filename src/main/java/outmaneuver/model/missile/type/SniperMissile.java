package outmaneuver.model.missile.type;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Direzione fissa al momento dello spawn.
 * Se esce dallo schermo viene eliminato, non rediretto.
 */
public final class SniperMissile extends Missile {

    public SniperMissile(final double x, final double y,
                         final Plane plane, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
        final double angle = Math.atan2(
                plane.getPosition().getY() - y,
                plane.getPosition().getX() - x);
        setVelocity(Math.cos(angle) * data.speed(), Math.sin(angle) * data.speed());
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane,
                                          final int screenW, final int screenH) {
        if (isOffScreen(plane, screenW, screenH)) {
            destroy();
        }
        return false;
    }

    @Override
    public String getMissileType() { return "sniper"; }
}