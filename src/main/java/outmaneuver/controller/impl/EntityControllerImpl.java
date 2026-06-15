package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.EntityController;
import outmaneuver.controller.InputController;
import outmaneuver.controller.MissileController;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.Plane;
import outmaneuver.model.area.TurnState;
import outmaneuver.model.missile.IMissile;
import outmaneuver.util.Vector2;

public final class EntityControllerImpl implements EntityController {

    private final Plane plane;
    private final InputController inputController;
    private final InternalEventListener eventListener;
    private final MissileController missileController;

    public EntityControllerImpl(final Plane plane,
                                final InputController inputController,
                                final InternalEventListener eventListener,
                                final MissileController missileController) {
        this.plane = Objects.requireNonNull(plane, "plane must not be null");
        this.inputController = Objects.requireNonNull(inputController, "inputController must not be null");
        this.eventListener = Objects.requireNonNull(eventListener, "eventListener must not be null");
        this.missileController = Objects.requireNonNull(missileController, "missileController must not be null");
    }

    @Override
    public void updateEntities(final long deltaMs) {
        final double deltaSec = deltaMs / 1000.0;
        final double turnDir = inputController.getTurnDirection();

        plane.setTurnState(turnDir < 0 ? TurnState.LEFT
                : turnDir > 0 ? TurnState.RIGHT
                : TurnState.NONE);

        final double newDir = plane.getDirection() + turnDir * plane.getStats().getTurnRate() * deltaSec;
        plane.setDirection(normaliseAngle(newDir));

        final Vector2 velocity = Vector2.fromAngle(plane.getDirection())
                .scale(plane.getEffectiveSpeed());
        final Vector2 newPos = plane.getPosition().add(velocity.scale(deltaSec));
        plane.setPosition(newPos);

        missileController.update(plane, deltaSec);
    }

    @Override
    public void clearAll() {
        plane.setPosition(Vector2.ZERO);
        plane.setDirection(0);
        plane.setTurnState(TurnState.NONE);
        missileController.reset();
    }

    @Override
    public Plane getPlane() {
        return plane;
    }

    @Override
    public List<IMissile> getMissiles() {
        return missileController.getActiveMissiles(); // AGGIUNTO
    }

    private static double normaliseAngle(final double angle) {
        final double twoPi = 2 * Math.PI;
        double normalised = angle % twoPi;
        if (normalised > Math.PI) {
            normalised -= twoPi;
        } else if (normalised < -Math.PI) {
            normalised += twoPi;
        }
        return normalised;
    }
}