package outmaneuver.model.missile.type;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Alterna tra visibile e invisibile ogni tot secondi.
 * Durate caricate da missiles.json.
 */
public final class GhostMissile extends Missile {

    private final double visibleDuration;
    private final double invisibleDuration;

    private double  phaseTimer   = 0;
    private boolean ghostVisible = true;

    public GhostMissile(final double x, final double y, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
        this.visibleDuration   = data.visibleDuration();
        this.invisibleDuration = data.invisibleDuration();
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;

        phaseTimer += dt;
        final double phaseDuration = ghostVisible ? visibleDuration : invisibleDuration;
        if (phaseTimer >= phaseDuration) {
            ghostVisible = !ghostVisible;
            phaseTimer   = 0;
        }

        steer(plane.getPosition().getX(), plane.getPosition().getY());
        move(dt);
    }

    @Override
    public boolean isGhostVisible() { return ghostVisible; }

    @Override
    public String getMissileType() { return "ghost"; }
}