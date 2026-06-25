package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.Event;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.impl.InputControllerImpl;
import outmaneuver.controller.impl.PlaneControllerImpl;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.util.Vector2;

class PlaneControllerImplTest {

    private static final double EPS = 1e-9;
    private static final int KEY_LEFT_A = 65;
    private static final int KEY_RIGHT_D = 68;
    private static final int PLANE_SPEED = 200;
    private static final int PLANE_TURN_RATE = 20;
    private static final double DIRECTION_AFTER_LEFT_TURN = -3.0;
    private static final double DOUBLED_SPEED_X = 400.0;
    private static final double START_X = 300;
    private static final double START_Y = 400;

    private InputControllerImpl input;
    private Plane plane;
    private PlaneControllerImpl planeCtrl;

    @BeforeEach
    void setUp() {
        input = new InputControllerImpl();
        plane = new PlaneImpl(new PlaneData("standard", PLANE_SPEED, 3, PLANE_TURN_RATE, "plane_standard", 0));
        planeCtrl = new PlaneControllerImpl(input, new ArrayList<>(), new CollisionEngine(new NoOpListener()));
        planeCtrl.spawnEntity(plane);
    }

    @Test
    void updateEntitiesMovesPlaneAlongItsDirection() {
        planeCtrl.updateEntities(1000);
        assertEquals((double) PLANE_SPEED, plane.getPosition().getX(), EPS);
        assertEquals(0.0, plane.getPosition().getY(), EPS);
    }

    @Test
    void updateEntitiesWithoutPlaneDoesNotThrow() {
        final PlaneControllerImpl emptyCtrl =
                new PlaneControllerImpl(input, new ArrayList<>(), new CollisionEngine(new NoOpListener()));
        org.junit.jupiter.api.Assertions.assertDoesNotThrow(() -> emptyCtrl.updateEntities(1000));
    }

    @Test
    void updateEntitiesTurnsLeftWhenLeftKeyPressed() {
        input.onKeyPressed(KEY_LEFT_A);
        planeCtrl.updateEntities(1000);
        assertEquals(TurnState.LEFT, plane.getTurnState());
        assertEquals(DIRECTION_AFTER_LEFT_TURN, plane.getDirection(), EPS);
    }

    @Test
    void updateEntitiesTurnsRightWhenRightKeyPressed() {
        input.onKeyPressed(KEY_RIGHT_D);
        planeCtrl.updateEntities(1000);
        assertEquals(TurnState.RIGHT, plane.getTurnState());
        assertEquals(3.0, plane.getDirection(), EPS);
    }

    @Test
    void updateEntitiesTurnStateIsNoneWithoutInput() {
        planeCtrl.updateEntities(1000);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void setSpeedMultiplierScalesMovementSpeed() {
        planeCtrl.setSpeedMultiplier(2.0);
        planeCtrl.updateEntities(1000);
        assertEquals(DOUBLED_SPEED_X, plane.getPosition().getX(), EPS);
    }

    @Test
    void clearAllResetsPlaneStateAndKeepsItRegistered() {
        plane.setPosition(new Vector2(START_X, START_Y));
        plane.setDirection(Math.PI / 2);
        plane.setTurnState(TurnState.LEFT);

        planeCtrl.removeAll();
        planeCtrl.clearAll();

        assertTrue(planeCtrl.getEntities().contains(plane), "Plane should be re-seeded by clearAll after removeAll");
        assertEquals(Vector2.ZERO, plane.getPosition());
        assertEquals(0.0, plane.getDirection(), EPS);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    private static final class NoOpListener implements InternalEventListener {
        @Override
        public void onInternalEvent(final Event evt, final Object data) {
        }
    }
}
