package outmaneuver.model.area.entity.missile.type;

import java.awt.Dimension;
import java.util.Random;

import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;

public final class BounceMissile extends MissileImpl {

    private final int bounceMargin;

    public BounceMissile(final Vector2 spawnPos, final MissileData data) {
        super(spawnPos, data.speed(), data.maxTurn(), data.radius(), data.lifetime(),
              data.predictionTime(), (int) data.outOfBoundsMargin());
        this.bounceMargin = (int) data.bounceMargin();
        setVelocity(Vector2.fromAngle(new Random().nextDouble() * Math.PI * 2).scale(data.speed()));
    }

    @Override
    public void update(final Plane plane, final double dt) {
        if (shouldSkipUpdate(dt)) return;
        move(dt);
    }

    @Override
    public void checkBounce(final Vector2 planePos, final Dimension screenSize) {
        final Vector2 pos = getPosition();
        final double relX = pos.getX() - planePos.getX();
        final double relY = pos.getY() - planePos.getY();
        final double halfW = screenSize.width  / 2.0;
        final double halfH = screenSize.height / 2.0;

        double vx = getVx();
        double vy = getVy();

        if (relX < -halfW + bounceMargin) vx =  Math.abs(vx);
        else if (relX >  halfW - bounceMargin) vx = -Math.abs(vx);

        if (relY < -halfH + bounceMargin) vy =  Math.abs(vy);
        else if (relY >  halfH - bounceMargin) vy = -Math.abs(vy);

        setVelocity(vx, vy);
    }

    @Override
    public boolean redirectIfOutOfBounds(final Plane plane, final Dimension screenSize) {
        return false;
    }

    @Override
    public String getMissileType() { return "bounce"; }
}