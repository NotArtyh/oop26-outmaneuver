package outmaneuver.model.area.effect;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EffectImplTest {

    private static final long SHIELD_DURATION_HALF_MS = 50L;
    private static final long SHIELD_DURATION_THIRD_MS = 40L;
    private static final double SPEED_BOOST_MULTIPLIER = 2.5;

    @Test
    void isActiveImmediatelyAfterCreation() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 1000L);
        assertTrue(effect.isActive());
    }

    @Test
    void becomesInactiveOnceRemainingDurationIsExhausted() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        effect.update(100L);
        assertFalse(effect.isActive());
    }

    @Test
    void remainsActiveBeforeDurationElapses() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        effect.update(SHIELD_DURATION_HALF_MS);
        assertTrue(effect.isActive());
    }

    @Test
    void durationIsConsumedAcrossMultipleUpdates() {
        final Effect effect = new EffectImpl(EffectType.SHIELD, 100L);
        effect.update(SHIELD_DURATION_THIRD_MS);
        assertTrue(effect.isActive());
        effect.update(SHIELD_DURATION_THIRD_MS);
        assertTrue(effect.isActive());
        effect.update(SHIELD_DURATION_THIRD_MS);
        assertFalse(effect.isActive());
    }

    @Test
    void typeReturnsConfiguredType() {
        assertEquals(EffectType.SHIELD, new EffectImpl(EffectType.SHIELD, 1000L).getType());
        assertEquals(EffectType.SPEED_BOOST, new EffectImpl(EffectType.SPEED_BOOST, 2.0, 1000L).getType());
    }

    @Test
    void multiplierDefaultsToZeroWithoutDurationOnlyConstructor() {
        assertEquals(0.0, new EffectImpl(EffectType.SHIELD, 1000L).getMultiplier());
    }

    @Test
    void multiplierReturnsConfiguredValue() {
        assertEquals(SPEED_BOOST_MULTIPLIER,
                new EffectImpl(EffectType.SPEED_BOOST, SPEED_BOOST_MULTIPLIER, 1000L).getMultiplier());
    }
}
