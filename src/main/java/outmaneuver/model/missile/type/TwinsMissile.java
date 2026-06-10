package outmaneuver.model.missile.type;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import java.util.ArrayList;
import java.util.List;

/*
 * Genera 3 proiettili paralleli con lo stesso vettore velocità.
 * Il TwinsMissile stesso si distrugge subito — spawna solo i 3 figli.
 */
public final class TwinsMissile extends Missile {

    private static final double SPEED  = 580.0;
    private static final double SPREAD = 130.0;

    private final List<IMissile> launched = new ArrayList<>();

    public TwinsMissile(final double x, final double y, final Plane plane) {
        super(x, y, 0, 0, 0, -1);

        final double angle = Math.atan2(
                plane.getPosition().getY() - y,
                plane.getPosition().getX() - x);

        final double sharedVx = Math.cos(angle) * SPEED;
        final double sharedVy = Math.sin(angle) * SPEED;

        final double perpX = -Math.sin(angle);
        final double perpY =  Math.cos(angle);

        for (int i = -1; i <= 1; i++) {
            final double sx = x + perpX * SPREAD * i;
            final double sy = y + perpY * SPREAD * i;
            final ParallelMissile m = new ParallelMissile(sx, sy, sharedVx, sharedVy);
            launched.add(m);
        }

        destroy();
    }

    @Override
    public List<IMissile> getSpawnOnDeath() { return launched; }

    @Override
    public String getMissileType() { return "twins"; }

    // Proiettile parallelo con velocità fissa — non sterza
    public static final class ParallelMissile extends Missile {

        public ParallelMissile(final double x, final double y,
                               final double vx, final double vy) {
            super(x, y, 580.0, 0.0, 6.0, -1);
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