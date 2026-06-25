package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.EffectEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.impl.CollectibleControllerImpl;
import outmaneuver.model.area.effect.Effect;
import outmaneuver.model.area.effect.EffectImpl;
import outmaneuver.model.area.effect.EffectType;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.area.entity.plane.PlaneData;
import outmaneuver.model.area.entity.plane.PlaneImpl;
import outmaneuver.util.Vector2;
import outmaneuver.view.GameView;
import outmaneuver.view.RenderState;

class CollectibleControllerImplTest {

    private static final long SPAWN_INTERVAL_MS = 3000;
    private static final int PLANE_SPEED = 200;
    private static final int PLANE_TURN_RATE = 20;
    private static final int VIEW_WIDTH = 800;
    private static final int VIEW_HEIGHT = 600;
    private static final long SPEED_BOOST_DURATION_MS = 3000L;
    private static final long UPDATE_PAST_EXPIRY_MS = 150L;

    private CollisionEngine collisionEngine;
    private PlaneImpl plane;
    private RecordingListener listener;
    private CollectibleControllerImpl collectibleCtrl;

    @BeforeEach
    void setUp() {
        plane = new PlaneImpl(new PlaneData("standard", PLANE_SPEED, 3, PLANE_TURN_RATE, "aircraft_standard", 0));
        listener = new RecordingListener();
        collisionEngine = new CollisionEngine(listener);
        collectibleCtrl = new CollectibleControllerImpl(new ArrayList<>(), collisionEngine);
        collectibleCtrl.setEventListener(listener);
        collectibleCtrl.setView(new StubGameView(VIEW_WIDTH, VIEW_HEIGHT));
    }

    // ── spawnEntity / removeEntity (inherited from EntityControllerImpl) ──

    @Test
    void spawnEntityAddsCollectibleToEntities() {
        final Collectible col = star(new Vector2(500, 500));
        collectibleCtrl.spawnEntity(col);
        assertTrue(collectibleCtrl.getEntities().contains(col));
    }

    @Test
    void removeEntityRemovesCollectible() {
        final Collectible col = star(Vector2.ZERO);
        collectibleCtrl.spawnEntity(col);
        collectibleCtrl.removeEntity(col);
        assertFalse(collectibleCtrl.getEntities().contains(col));
    }

    // ── addEffect / hasEffect / getEffectMultiplier ───────────────────

    @Test
    void addEffectActivatesEffectAndFiresEffectApplied() {
        final Effect effect = new EffectImpl(EffectType.SPEED_BOOST, 2.0, SPEED_BOOST_DURATION_MS);
        collectibleCtrl.addEffect(effect);

        assertTrue(collectibleCtrl.hasEffect(EffectImpl.class));
        assertEquals(2.0, collectibleCtrl.getEffectMultiplier());
        assertTrue(listener.getEvents().contains(EffectEvent.EFFECT_APPLIED));
        assertTrue(listener.getPayloads().contains(effect));
    }

    @Test
    void addEffectSameTypeReplacesPreviousEffect() {
        collectibleCtrl.addEffect(new EffectImpl(EffectType.SPEED_BOOST, 2.0, SPEED_BOOST_DURATION_MS));
        final Effect replacement = new EffectImpl(EffectType.SPEED_BOOST, 4.0, 1000L);
        collectibleCtrl.addEffect(replacement);

        assertEquals(4.0, collectibleCtrl.getEffectMultiplier(),
                "A new effect of the same type should replace the active one rather than stack");
    }

    @Test
    void hasEffectFalseWhenNoEffectActive() {
        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class));
    }

    @Test
    void effectMultiplierDefaultsToOneWithoutActiveEffect() {
        assertEquals(1.0, collectibleCtrl.getEffectMultiplier());
    }

    // ── updateEntities – effect expiry (CollectibleControllerImpl-specific) ──

    @Test
    void updateEntitiesExpiresEffectAfterItsDurationAndFiresEffectExpired() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        collectibleCtrl.addEffect(effect);
        listener.getEvents().clear();
        listener.getPayloads().clear();

        collectibleCtrl.updateEntities(UPDATE_PAST_EXPIRY_MS);

        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class), "Expired effect should no longer be active");
        assertTrue(listener.getEvents().contains(EffectEvent.EFFECT_EXPIRED));
        assertTrue(listener.getPayloads().contains(effect));
    }

    @Test
    void updateEntitiesDoesNotExpireEffectBeforeItsDuration() {
        collectibleCtrl.addEffect(new EffectImpl(EffectType.SHIELD, 1000L));
        collectibleCtrl.updateEntities(10L);
        assertTrue(collectibleCtrl.hasEffect(EffectImpl.class));
    }

    // ── clearAll – clears active effects only, leaves entities untouched ──

    @Test
    void clearAllClearsActiveEffectsAndFiresEffectExpired() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 5000L);
        collectibleCtrl.addEffect(effect);
        listener.getEvents().clear();
        listener.getPayloads().clear();

        collectibleCtrl.clearAll();

        assertFalse(collectibleCtrl.hasEffect(EffectImpl.class));
        assertTrue(listener.getEvents().contains(EffectEvent.EFFECT_EXPIRED));
    }

    @Test
    void clearAllDoesNotRemoveSpawnedEntities() {
        collectibleCtrl.spawnEntity(plane);
        final Collectible col = star(new Vector2(200, 200));
        collectibleCtrl.spawnEntity(col);

        collectibleCtrl.clearAll();

        assertTrue(collectibleCtrl.getEntities().contains(plane));
        assertTrue(collectibleCtrl.getEntities().contains(col),
                "clearAll only resets active effects; entity cleanup is handled elsewhere");
    }

    // ── updateEntities – spawn timing ─────────────────────────────────

    @Test
    void updateEntitiesDoesNotSpawnBeforeInterval() {
        collectibleCtrl.spawnEntity(plane);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS - 1);
        assertEquals(1, collectibleCtrl.getEntities().size(),
                "Only the plane should be present before the spawn interval elapses");
    }

    @Test
    void updateEntitiesSpawnsCollectibleAfterInterval() {
        collectibleCtrl.spawnEntity(plane);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS);

        final long collectibleCount = collectibleCtrl.getEntities().stream()
                .filter(e -> e instanceof Collectible)
                .count();
        assertEquals(1, collectibleCount, "A collectible should spawn once the interval elapses");
    }

    @Test
    void updateEntitiesAccumulatesDeltaAcrossTicks() {
        collectibleCtrl.spawnEntity(plane);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS / 2);
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS / 2);

        final long collectibleCount = collectibleCtrl.getEntities().stream()
                .filter(e -> e instanceof Collectible)
                .count();
        assertEquals(1, collectibleCount, "Accumulated deltas should trigger a spawn once their sum reaches the interval");
    }

    @Test
    void updateEntitiesDoesNotSpawnWithoutPlane() {
        collectibleCtrl.updateEntities(SPAWN_INTERVAL_MS);
        assertTrue(collectibleCtrl.getEntities().isEmpty(),
                "No spawn should happen without a plane to anchor the spawn position");
    }

    @Test
    void updateEntitiesDoesNotSpawnWithZeroViewSize() {
        final CollectibleControllerImpl zeroViewCtrl =
                new CollectibleControllerImpl(new ArrayList<>(), collisionEngine);
        zeroViewCtrl.setView(new StubGameView(0, 0));
        zeroViewCtrl.spawnEntity(plane);
        zeroViewCtrl.updateEntities(SPAWN_INTERVAL_MS);

        assertEquals(1, zeroViewCtrl.getEntities().size(),
                "Only the plane should remain when the view size is zero");
    }

    private static Collectible star(final Vector2 position) {
        return new StarCollectible(position, 10);
    }

    private static final class RecordingListener implements InternalEventListener {
        private final List<Event> events = new ArrayList<>();
        private final List<Object> payloads = new ArrayList<>();

        List<Event> getEvents() {
            return events;
        }

        List<Object> getPayloads() {
            return payloads;
        }

        @Override
        public void onInternalEvent(final Event evt, final Object data) {
            events.add(evt);
            payloads.add(data);
        }
    }

    private static final class StubGameView implements GameView {
        private final int width;
        private final int height;

        StubGameView(final int width, final int height) {
            this.width = width;
            this.height = height;
        }

        @Override
        public void renderFrame(final RenderState state) {
        }

        @Override
        public int getWidth() {
            return width;
        }

        @Override
        public int getHeight() {
            return height;
        }
    }
}
