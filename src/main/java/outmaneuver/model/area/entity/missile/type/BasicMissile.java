package outmaneuver.model.area.entity.missile.type;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

public final class BasicMissile extends MissileImpl {

    public BasicMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(),
              data.predictionTime(), (int) data.outOfBoundsMargin());
    }

    @Override
    public String getMissileType() { return "basic"; }
}
