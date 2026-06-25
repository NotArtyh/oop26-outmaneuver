package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.missile.MissileImpl;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.util.Vector2;

class CollisionEngineTest {

    private RecordingListener listener;
    private CollisionEngine engine;

    @BeforeEach
    void setUp() {
        listener = new RecordingListener();
        engine = new CollisionEngine(listener);
    }

    @Test
    @SuppressWarnings("PMD.CompareObjectsWithEquals")
    void overlappingMissilesTriggerCollisionEvent() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        engine.register(a);
        engine.register(b);
        engine.tick();

        assertTrue(listener.getEvents().contains(CollisionEvent.MISSILE_MISSILE_COLLISION),
                "Overlapping missiles should trigger a missile-missile collision event");

        final CollisionData data = (CollisionData) payloadFor(CollisionEvent.MISSILE_MISSILE_COLLISION);
        assertTrue(data.getEntityA() == a && data.getEntityB() == b
                || data.getEntityA() == b && data.getEntityB() == a);
    }

    @Test
    void distantMissilesDoNotTriggerCollisionEvent() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(1000, 1000));

        engine.register(a);
        engine.register(b);
        engine.tick();

        assertTrue(listener.getEvents().isEmpty(), "Distant missiles should not trigger a collision event");
    }

    @Test
    void unregisteredMissileDoesNotTriggerCollisionEvent() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        engine.register(a);
        engine.register(b);
        engine.unregister(b);
        engine.tick();

        assertTrue(listener.getEvents().isEmpty(), "Unregistered entities should not participate in collisions");
    }

    @Test
    void unregisteringAllEntitiesStopsCollisions() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));
        final MissileImpl b = new TestMissile(new Vector2(5, 0));

        engine.register(a);
        engine.register(b);
        engine.unregister(a);
        engine.unregister(b);
        engine.tick();

        assertTrue(listener.getEvents().isEmpty(), "Unregistering all entities should remove them from collision checks");
    }

    @Test
    void sameMissileDoesNotCollideWithItself() {
        final MissileImpl a = new TestMissile(new Vector2(0, 0));

        engine.register(a);
        engine.tick();

        assertTrue(listener.getEvents().isEmpty(), "A single entity must not collide with itself");
    }

    private Object payloadFor(final CollisionEvent event) {
        final int index = listener.getEvents().indexOf(event);
        assertTrue(index >= 0);
        return listener.getPayloads().get(index);
    }

    /** Missile concreto minimale per i test: raggio 8 cosi' due missili vicini collidono. */
    private static final class TestMissile extends MissileImpl {
        TestMissile(final Vector2 pos) {
            super(pos, new MissileData("test", 1.0, 0.0, 8.0, -1.0, 0.0, 0, null));
        }
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
}
