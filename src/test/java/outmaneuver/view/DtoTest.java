package outmaneuver.view;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;

class DtoTest {

    private static final double EPS = 1e-12;
    private static final String AIRCRAFT_STANDARD = "aircraft_standard";
    private static final int PLANE_HEALTH = 200;
    private static final double PLANE_DIRECTION_RAD = 1.5;
    private static final double POSITION_X = 150;
    private static final double POSITION_Y = 250;
    private static final double OTHER_POSITION = 999;

    @Test
    void testEntityRenderData() {
        final var data = new EntityRenderData(100, PLANE_HEALTH, PLANE_DIRECTION_RAD, "aircraft_test", 10);
        assertEquals(100, data.getX());
        assertEquals(PLANE_HEALTH, data.getY());
        assertEquals(PLANE_DIRECTION_RAD, data.getDirectionRad(), EPS);
        assertEquals("aircraft_test", data.getSpriteId());
        assertEquals(10, data.getRadius(), EPS);
    }

    @Test
    void testRenderStateBuilder() {
        final var plane = new PlaneImpl(new PlaneData("standard", PLANE_HEALTH, 3, 20, AIRCRAFT_STANDARD, 0));
        plane.setPosition(new Vector2(POSITION_X, POSITION_Y));
        plane.setDirection(Math.PI / 3);

        final var planeData = new EntityRenderData(
                plane.getPosition().getX(),
                plane.getPosition().getY(),
                plane.getDirection(),
                plane.getStats().getSpriteId(),
                plane.getStats().getHitboxRadius());

        final var state = RenderState.builder()
                .planeData(planeData)
                .build();

        final var result = state.getPlane();
        assertEquals(POSITION_X, result.getX(), EPS);
        assertEquals(POSITION_Y, result.getY(), EPS);
        assertEquals(Math.PI / 3, result.getDirectionRad(), EPS);
        assertEquals(AIRCRAFT_STANDARD, result.getSpriteId());
    }

    @Test
    void testRenderStateImmutability() {
        final var plane = new PlaneImpl(new PlaneData("standard", PLANE_HEALTH, 3, 20, AIRCRAFT_STANDARD, 0));
        final var planeData = new EntityRenderData(
                plane.getPosition().getX(),
                plane.getPosition().getY(),
                plane.getDirection(),
                plane.getStats().getSpriteId(),
                plane.getStats().getHitboxRadius());

        final var state = RenderState.builder()
                .planeData(planeData)
                .build();

        final var result = state.getPlane();
        assertEquals(0, result.getX(), EPS);

        plane.setPosition(new Vector2(OTHER_POSITION, OTHER_POSITION));
        assertEquals(0, state.getPlane().getX(), EPS);
    }
}
