package outmaneuver.model.area.entity.missile.data;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.util.json.GsonProvider;
import outmaneuver.util.json.JsonResourceLoader;

/**
 * Verifica che missiles.json venga caricato correttamente, in particolare che
 * l'effetto opzionale {@link MissileData.SlowEffect} sia presente solo per il
 * clock e assente (null) per tutti gli altri tipi.
 */
class JsonMissileRepositoryTest {

    private static final String BASIC_TYPE = "basic";
    private static final double EPSILON = 1e-9;
    private static final double CLOCK_SLOW_FACTOR = 0.3;
    private static final double CLOCK_SLOW_DURATION = 3.0;
    private static final double BASIC_SPEED = 320.0;
    private static final double BASIC_RADIUS = 15.0;
    private static final double BASIC_LIFETIME = 15.0;
    private static final int BASIC_OUT_OF_BOUNDS_MARGIN = 150;

    private MissileRepository repo;

    @BeforeEach
    void setUp() {
        repo = new JsonMissileRepository(
                JsonResourceLoader.forList("missiles.json", MissileData.class, GsonProvider.create()));
    }

    @Test
    void allKnownTypesLoad() {
        for (final String type : List.of(BASIC_TYPE, "fast", "sniper", "bounce", "shield", "clock")) {
            assertTrue(repo.loadByType(type).isPresent(), "manca il tipo: " + type);
        }
    }

    @Test
    void unknownTypeIsEmpty() {
        assertTrue(repo.loadByType("does-not-exist").isEmpty());
    }

    @Test
    void basicHasNoSlowEffect() {
        final MissileData basic = repo.loadByType(BASIC_TYPE).orElseThrow();
        assertNull(basic.slow(), "solo il clock deve avere l'effetto slow");
    }

    @Test
    void clockHasSlowEffect() {
        final MissileData clock = repo.loadByType("clock").orElseThrow();
        final MissileData.SlowEffect slow = clock.slow();
        assertNotNull(slow, "il clock deve avere l'effetto slow");
        assertEquals(CLOCK_SLOW_FACTOR, slow.factor(), EPSILON);
        assertEquals(CLOCK_SLOW_DURATION, slow.duration(), EPSILON);
    }

    @Test
    void commonFieldsAreParsed() {
        final Optional<MissileData> basic = repo.loadByType(BASIC_TYPE);
        assertTrue(basic.isPresent());
        final MissileData data = basic.get();
        assertEquals(BASIC_SPEED, data.speed(), EPSILON);
        assertEquals(BASIC_RADIUS, data.radius(), EPSILON);
        assertEquals(BASIC_LIFETIME, data.lifetime(), EPSILON);
        assertEquals(BASIC_OUT_OF_BOUNDS_MARGIN, data.outOfBoundsMargin());
    }
}
