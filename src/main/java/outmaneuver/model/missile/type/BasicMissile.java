package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;

/*
 * Missile base — seek steering standard.
 */
public final class BasicMissile extends Missile {

    private static final double SPEED    = 350.0;
    private static final double MAX_TURN = 0.015;
    private static final double RADIUS   = 10.0;
    private static final double LIFETIME = 12.0;

    public BasicMissile(final double x, final double y) {
        super(x, y, SPEED, MAX_TURN, RADIUS, LIFETIME);
    }

    @Override
    protected double getMaxLifetime() { return LIFETIME; }

    @Override
    public String getMissileType() { return "basic"; }
}