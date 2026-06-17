package outmaneuver.model.area.entity.missile.type;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

public final class GhostMissile extends MissileImpl {

    private final double visibleDuration;
    private final double invisibleDuration;

    private double cycleTimer;
    private boolean visible = true;

    public GhostMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(),
              data.predictionTime(), (int) data.outOfBoundsMargin());
        this.visibleDuration   = data.visibleDuration();
        this.invisibleDuration = data.invisibleDuration();
        this.cycleTimer        = visibleDuration;
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        cycleTimer -= dt;
        if (cycleTimer <= 0) {
            visible    = !visible;
            cycleTimer = visible ? visibleDuration : invisibleDuration;
        }
        move(dt);
    }

    @Override
    public boolean isGhostVisible() { return visible; }

    @Override
    public String getMissileType() { return "ghost"; }
}
