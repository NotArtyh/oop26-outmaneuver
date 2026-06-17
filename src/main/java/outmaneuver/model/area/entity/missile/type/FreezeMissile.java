package outmaneuver.model.area.entity.missile.type;

import java.util.List;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

public final class FreezeMissile extends MissileImpl {

    private final MissileData data;

    public FreezeMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(),
              data.predictionTime(), (int) data.outOfBoundsMargin());
        this.data = data;
    }

    @Override
    public void onCollision(final List<Missile> activeMissiles) {
        final Vector2 myPos = getPosition();
        for (final Missile other : activeMissiles) {
            if (!other.isAlive() || other.equals(this)) continue;
            final double dist = other.getPosition().add(myPos.scale(-1)).magnitude();
            if (dist <= data.freezeRadius()) {
                other.freeze(data.freezeDuration());
            }
        }
        destroy();
    }

    @Override
    public String getMissileType() { return "freeze"; }
}
