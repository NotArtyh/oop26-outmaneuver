package outmaneuver.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.impl.ScoreControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.collectibles.StarCollectible;
import outmaneuver.model.session.ISession;
import outmaneuver.model.session.Session;
import outmaneuver.util.Vector2;

class ScoreControllerImplTest {

    private static final long PARTIAL_TICK_MS = 600;
    private static final long MULTI_SECOND_TICK_MS = 3500;
    private static final long RESET_TICK_MS = 900;
    private static final int STAR_SCORE_VALUE = 25;
    private static final int MISSILE_MISSILE_SCORE_VALUE = 20;

    private ISession session;
    private ScoreControllerImpl scoreCtrl;

    @BeforeEach
    void setUp() {
        session = new Session();
        scoreCtrl = new ScoreControllerImpl(session, () -> 16L);
    }

    @Test
    void constructorRejectsNullSession() {
        assertThrows(NullPointerException.class, () -> new ScoreControllerImpl(null, () -> 16L));
    }

    @Test
    void constructorRejectsNullSupplier() {
        assertThrows(NullPointerException.class, () -> new ScoreControllerImpl(session, null));
    }

    @Test
    void onTickAwardsOnePointPerElapsedSecond() {
        scoreCtrl.onTick(1000);
        assertEquals(1, session.getScore());
    }

    @Test
    void onTickAccumulatesPartialMillisecondsAcrossTicks() {
        scoreCtrl.onTick(PARTIAL_TICK_MS);
        assertEquals(0, session.getScore(), "Less than a second elapsed: no point yet");
        scoreCtrl.onTick(PARTIAL_TICK_MS);
        assertEquals(1, session.getScore(), "The two partial ticks sum to over a second");
    }

    @Test
    void onTickAwardsMultiplePointsForMultipleElapsedSeconds() {
        scoreCtrl.onTick(MULTI_SECOND_TICK_MS);
        assertEquals(3, session.getScore());
    }

    @Test
    void resetClearsPendingAccumulatedTime() {
        scoreCtrl.onTick(RESET_TICK_MS);
        scoreCtrl.reset();
        scoreCtrl.onTick(RESET_TICK_MS);
        assertEquals(0, session.getScore(), "reset() should drop the 900ms accumulated before it");
    }

    @Test
    void onInternalEventStarCollectibleAddsItsScoreValue() {
        final StarCollectible star = new StarCollectible(Vector2.ZERO, STAR_SCORE_VALUE);
        scoreCtrl.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, star);
        assertEquals(STAR_SCORE_VALUE, session.getScore());
    }

    @Test
    void onInternalEventMissileMissileCollisionAddsTwentyPoints() {
        scoreCtrl.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION,
                new CollisionData(null, null, Vector2.ZERO));
        assertEquals(MISSILE_MISSILE_SCORE_VALUE, session.getScore());
    }

    @Test
    void onInternalEventPlaneMissileCollisionDoesNotAwardPoints() {
        scoreCtrl.onInternalEvent(CollisionEvent.PLANE_MISSILE_COLLISION,
                new CollisionData(null, null, Vector2.ZERO));
        assertEquals(0, session.getScore());
    }
}
