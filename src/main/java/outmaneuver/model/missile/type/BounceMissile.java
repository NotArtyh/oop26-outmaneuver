package outmaneuver.model.missile.type;

import java.util.Random;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Rimbalza sui bordi dello schermo con direzione casuale.
 */
public final class BounceMissile extends Missile {

    private static final int BOUNCE_MARGIN = 10;

    private final int screenW;
    private final int screenH;

    public BounceMissile(final double x, final double y,
                         final int screenW, final int screenH,
                         final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
        this.screenW = screenW;
        this.screenH = screenH;
        final double angle = new Random().nextDouble() * Math.PI * 2;
        setVelocity(Math.cos(angle) * data.speed(), Math.sin(angle) * data.speed());
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    public void checkBounce(final Plane plane) {
        final double cx = plane.getPosition().getX();
        final double cy = plane.getPosition().getY();
        final double sx = getWorldX() - cx;
        final double sy = getWorldY() - cy;

        if (sx < -screenW / 2.0 + BOUNCE_MARGIN) {
            setVelocity(Math.abs(getVx()), getVy());
        } else if (sx > screenW / 2.0 - BOUNCE_MARGIN) {
            setVelocity(-Math.abs(getVx()), getVy());
        }

        if (sy < -screenH / 2.0 + BOUNCE_MARGIN) {
            setVelocity(getVx(), Math.abs(getVy()));
        } else if (sy > screenH / 2.0 - BOUNCE_MARGIN) {
            setVelocity(getVx(), -Math.abs(getVy()));
        }
    }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane,
                                          final int screenW, final int screenH) {
        return false;
    }

    @Override
    public String getMissileType() { return "bounce"; }
}