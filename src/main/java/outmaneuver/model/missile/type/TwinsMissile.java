package outmaneuver.model.missile.type;

import java.util.ArrayList;
import java.util.List;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Genera 3 proiettili paralleli con lo stesso vettore velocità.
 * Il TwinsMissile stesso si distrugge subito.
 */
public final class TwinsMissile extends Missile {

    private final List<IMissile> launched = new ArrayList<>();

    public TwinsMissile(final double x, final double y,
                        final Plane plane, final MissileData data) {
        super(x, y, 0, 0, 0, -1);

        final double angle = Math.atan2(
                plane.getPosition().getY() - y,
                plane.getPosition().getX() - x);

        final double sharedVx = Math.cos(angle) * data.speed();
        final double sharedVy = Math.sin(angle) * data.speed();

        final double perpX = -Math.sin(angle);
        final double perpY =  Math.cos(angle);

        for (int i = -1; i <= 1; i++) {
            final double sx = x + perpX * data.spread() * i;
            final double sy = y + perpY * data.spread() * i;
            final ParallelMissile m = new ParallelMissile(sx, sy, sharedVx, sharedVy, data);
            launched.add(m);
        }

        destroy();
    }

    @Override
    public List<IMissile> getSpawnOnDeath() { return launched; }

    @Override
    public String getMissileType() { return "twins"; }

    public static final class ParallelMissile extends Missile {

        public ParallelMissile(final double x, final double y,
                               final double vx, final double vy,
                               final MissileData data) {
            super(x, y, data.speed(), 0.0, data.radius(), data.lifetime());
            setVelocity(vx, vy);
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
        public String getMissileType() { return "twins"; }
    }
}