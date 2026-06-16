package outmaneuver.model.missile.type;

import java.util.List;

import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Richiede due collisioni per essere distrutto.
 */
public final class ShieldMissile extends Missile {

    private boolean shielded = true;

    public ShieldMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(), data.predictionTime(), (int) data.outOfBoundsMargin());
    }

    @Override
    public void onCollision(final List<IMissile> activeMissiles) {
        if (shielded) {
            shielded = false;
        } else {
            destroy();
        }
    }

    public boolean isShielded() { return shielded; }

    @Override
    public String getMissileType() { return "shield"; }
}