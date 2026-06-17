package outmaneuver.model.area.entity.missile.type;

import java.util.List;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Quando collide rallenta tutti i missili attivi.
 */
public final class ClockMissile extends MissileImpl {

    //  tengo data solo se ti serve dopo il costruttore
    private final MissileData data;

    public ClockMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
        this.data = data;
    }

    @Override
    public void onCollision(final List<Missile> activeMissiles) {
        for (final Missile other : activeMissiles) {
            if (!other.isAlive() || other.equals(this)) continue;
            other.slowDown(data.slowFactor(), data.slowDuration());
        }
        destroy();
    }

    @Override
    public String getMissileType() { return "clock"; }
}