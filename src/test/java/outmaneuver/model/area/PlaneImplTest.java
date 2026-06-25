package outmaneuver.model.area;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.model.area.entity.plane.PlaneStats;
import outmaneuver.model.area.entity.plane.TurnState;
import outmaneuver.util.Vector2;

class PlaneImplTest {

    private static final double EPS = 1e-12;
    private static final int STANDARD_BASE_SPEED = 200;
    private static final int STANDARD_HITBOX_RADIUS = 20;
    private static final double CUSTOM_BASE_SPEED = 999;
    private static final double CUSTOM_TURN_RATE = 5;
    private static final double INTERCEPTOR_BASE_SPEED = 280.0;
    private static final double INTERCEPTOR_HITBOX_RADIUS = 15.0;
    private static final int INTERCEPTOR_PRICE = 500;

    private PlaneImpl plane;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(
                new PlaneData("standard", STANDARD_BASE_SPEED, 3, STANDARD_HITBOX_RADIUS, "aircraft_standard", 0));
    }

    @Test
    void testDefaultConstruction() {
        assertEquals(Vector2.ZERO, plane.getPosition());
        assertEquals(0.0, plane.getDirection(), EPS);
        assertEquals(TurnState.NONE, plane.getTurnState());
        assertEquals("standard", plane.getStats().getId());
    }

    @Test
    void testSetPosition() {
        final var pos = new Vector2(100, 200);
        plane.setPosition(pos);
        assertEquals(pos, plane.getPosition());
    }

    @Test
    void testSetPositionNullThrows() {
        assertThrows(NullPointerException.class, () -> plane.setPosition(null));
    }

    @Test
    void testSetDirection() {
        plane.setDirection(Math.PI / 4);
        assertEquals(Math.PI / 4, plane.getDirection(), EPS);
    }

    @Test
    void testSetStats() {
        final var customStats = new PlaneStats() {
            @Override
            public String getId() {
                return "custom";
            }

            @Override
            public double getBaseSpeed() {
                return CUSTOM_BASE_SPEED;
            }

            @Override
            public double getTurnRate() {
                return CUSTOM_TURN_RATE;
            }

            @Override
            public double getHitboxRadius() {
                return 10;
            }

            @Override
            public String getSpriteId() {
                return "custom_sprite";
            }
        };
        plane.setStats(customStats);
        assertEquals("custom", plane.getStats().getId());
        assertEquals(CUSTOM_BASE_SPEED, plane.getStats().getBaseSpeed());
    }

    @Test
    void testSetStatsNullThrows() {
        assertThrows(NullPointerException.class, () -> plane.setStats(null));
    }

    @Test
    void testTurnState() {
        assertEquals(TurnState.NONE, plane.getTurnState());
        plane.setTurnState(TurnState.LEFT);
        assertEquals(TurnState.LEFT, plane.getTurnState());
        plane.setTurnState(TurnState.RIGHT);
        assertEquals(TurnState.RIGHT, plane.getTurnState());
        plane.setTurnState(TurnState.NONE);
        assertEquals(TurnState.NONE, plane.getTurnState());
    }

    @Test
    void testSetTurnStateNullThrows() {
        assertThrows(NullPointerException.class, () -> plane.setTurnState(null));
    }

    @Test
    void testPlaneData() {
        final var stats = new PlaneData("interceptor", INTERCEPTOR_BASE_SPEED, 2,
                INTERCEPTOR_HITBOX_RADIUS, "aircraft_interceptor", INTERCEPTOR_PRICE);
        assertEquals("interceptor", stats.getId());
        assertEquals(INTERCEPTOR_BASE_SPEED, stats.getBaseSpeed());
        assertEquals(2.0, stats.getTurnRate());
        assertEquals(INTERCEPTOR_HITBOX_RADIUS, stats.getHitboxRadius());
        assertEquals("aircraft_interceptor", stats.getSpriteId());
        assertEquals(INTERCEPTOR_PRICE, stats.price());
    }

    @Test
    void testTurnStateEnumValues() {
        assertEquals(3, TurnState.values().length);
        assertNotNull(TurnState.valueOf("NONE"));
        assertNotNull(TurnState.valueOf("LEFT"));
        assertNotNull(TurnState.valueOf("RIGHT"));
    }
}
