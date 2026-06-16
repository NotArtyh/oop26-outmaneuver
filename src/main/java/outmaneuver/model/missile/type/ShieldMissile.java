package outmaneuver.model.missile.type;

import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;

/*
 * Richiede due collisioni per essere distrutto.
 */
public final class ShieldMissile extends Missile {

    private final double shieldRadius;
    private boolean shielded = true;

    public ShieldMissile(final double x, final double y, final MissileData data) {
        super(x, y, data.speed(), data.maxTurn(), data.radius(), data.lifetime());
        this.shieldRadius = data.shieldRadius();
    }

    public void hit() {
        if (shielded) {
            shielded = false;
        } else {
            destroy();
        }
    }

    public boolean isShielded() { return shielded; }
    public double getShieldRadius() { return shieldRadius; }

    @Override
    public String getMissileType() { return "shield"; }
}