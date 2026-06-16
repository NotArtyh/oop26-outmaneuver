package outmaneuver.model.missile.type;

import java.awt.Dimension;
import java.util.Random;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Rimbalza sui bordi dello schermo con direzione casuale.
 */
public final class BounceMissile extends Missile {

    private final int bounceMargin;

    public BounceMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
        this.bounceMargin = (int) data.bounceMargin();
        setVelocity(Vector2.fromAngle(new Random().nextDouble() * Math.PI * 2).scale(data.speed()));
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    @Override
    public void checkBounce(final Vector2 planePos, final Dimension screenSize) {
        final Vector2 relative = getPosition().add(planePos.scale(-1));
        final Vector2 vel      = getVelocity();

        if (relative.getX() < -screenSize.width / 2.0 + bounceMargin) {
            setVelocity(new Vector2(Math.abs(vel.getX()),  vel.getY()));
        } else if (relative.getX() > screenSize.width / 2.0 - bounceMargin) {
            setVelocity(new Vector2(-Math.abs(vel.getX()), vel.getY()));
        }

        final Vector2 vel2 = getVelocity();
        if (relative.getY() < -screenSize.height / 2.0 + bounceMargin) {
            setVelocity(new Vector2(vel2.getX(),  Math.abs(vel2.getY())));
        } else if (relative.getY() > screenSize.height / 2.0 - bounceMargin) {
            setVelocity(new Vector2(vel2.getX(), -Math.abs(vel2.getY())));
        }
    }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane, final Dimension screenSize) {
        return false;
    }

    @Override
    public String getMissileType() { return "bounce"; }
}
