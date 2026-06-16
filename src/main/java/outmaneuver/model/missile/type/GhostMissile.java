package outmaneuver.model.missile.type;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Alterna tra visibile e invisibile ogni tot secondi.
 * Durate caricate da missiles.json.
 */
public final class GhostMissile extends Missile {

    private final MissileData data;
    private double  phaseTimer   = 0;
    private boolean ghostVisible = true;

    public GhostMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
        this.data = data;
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;

        phaseTimer += dt;
        final double phaseDuration = ghostVisible ? data.visibleDuration() : data.invisibleDuration();
        if (phaseTimer >= phaseDuration) {
            ghostVisible = !ghostVisible;
            phaseTimer   = 0;
        }

        steer(plane.getPosition());
        move(dt);
    }

    @Override
    public boolean isGhostVisible() { return ghostVisible; }

    @Override
    public String getMissileType() { return "ghost"; }
}