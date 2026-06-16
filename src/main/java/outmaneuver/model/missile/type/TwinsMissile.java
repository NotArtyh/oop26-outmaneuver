package outmaneuver.model.missile.type;

import java.util.ArrayList;
import java.util.List;

import outmaneuver.model.missile.IMissile;
import outmaneuver.model.missile.Missile;
import outmaneuver.model.missile.data.MissileData;
import outmaneuver.util.Vector2;

/*
 * Genera 3 SniperMissile paralleli con lo stesso vettore velocità.
 * Il TwinsMissile stesso si distrugge subito.
 */
public final class TwinsMissile extends Missile {

    private final List<IMissile> launched = new ArrayList<>();
    private final MissileData data;

    public TwinsMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, 0, 0, 0, -1, 0, 0);
        this.data = data;
    }

    @Override
    public void setInitialDirection(final Vector2 target) {
        final Vector2 myPos = getPosition();
        final double angle = target.add(myPos.scale(-1)).angle();
        final Vector2 sharedVel = Vector2.fromAngle(angle).scale(data.speed());
        final Vector2 perp = new Vector2(-Math.sin(angle), Math.cos(angle));

        for (int i = -1; i <= 1; i++) {
            final Vector2 pos = myPos.add(perp.scale(data.spread() * i));
            final SniperMissile child = new SniperMissile(pos, data);
            child.setVelocity(sharedVel);
            launched.add(child);
        }
    }

    @Override
    public List<IMissile> getSpawnOnInit() { return launched; }

    @Override
    public String getMissileType() { return "twins"; }
}
