package outmaneuver.model.profile;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerProfileTest {

    private static final String FAST_PLANE_ID = "fast";
    private static final String STANDARD_PLANE_ID = "standard";
    private static final int COINS_SMALL = 200;
    private static final int COINS_MEDIUM = 300;
    private static final int COINS_LARGE = 400;
    private static final int COINS_HUGE = 500;
    private static final int SCORE_SMALL = 250;
    private static final int SCORE_HIGH_TOP_SCORES = 15;
    private static final int TOP_SCORES_TRIMMED_SIZE = 150;

    private PlayerProfile profile;

    @BeforeEach
    void setUp(@TempDir final Path tmpDir) {
        final Path file = tmpDir.resolve("profile.json");
        final var repo = JsonPlayerProfileRepository.create(file);
        repo.persist(new PlayerProfileData("", 0, List.of(STANDARD_PLANE_ID), List.of()));
        profile = new PlayerProfile(repo);
    }

    // ── default state ──

    @Test
    void defaultCoinsIsZero() {
        assertEquals(0, profile.getCoins());
    }

    @Test
    void defaultOwnsStandardPlane() {
        assertTrue(profile.ownsPlane(STANDARD_PLANE_ID));
    }

    @Test
    void defaultDoesNotOwnUnknownPlane() {
        assertFalse(profile.ownsPlane(FAST_PLANE_ID));
    }

    @Test
    void defaultTopScoresIsEmpty() {
        assertTrue(profile.getTopScores().isEmpty());
    }

    // ── wallet ──

    @Test
    void addCoinsAccumulates() {
        profile.addCoins(COINS_MEDIUM);
        profile.addCoins(COINS_SMALL);
        assertEquals(COINS_HUGE, profile.getCoins());
    }

    @Test
    void addCoinsRejectsZero() {
        assertThrows(IllegalArgumentException.class, () -> profile.addCoins(0));
    }

    @Test
    void spendReturnsTrueAndDeducts() {
        profile.addCoins(COINS_HUGE);
        assertTrue(profile.spend(COINS_SMALL));
        assertEquals(COINS_MEDIUM, profile.getCoins());
    }

    @Test
    void spendReturnsFalseWhenInsufficient() {
        profile.addCoins(100);
        assertFalse(profile.spend(COINS_SMALL));
        assertEquals(100, profile.getCoins());
    }

    // ── owned planes ──

    @Test
    void addOwnedPlaneTracksNewPlane() {
        profile.addOwnedPlane(FAST_PLANE_ID);
        assertTrue(profile.ownsPlane(FAST_PLANE_ID));
    }

    @Test
    void addOwnedPlaneDuplicateIsIdempotent() {
        profile.addOwnedPlane(FAST_PLANE_ID);
        profile.addOwnedPlane(FAST_PLANE_ID);
        assertTrue(profile.ownsPlane(FAST_PLANE_ID));
    }

    // ── scores ──

    @Test
    void saveScoreAppearsInTopScores() {
        profile.saveScore(COINS_MEDIUM, "Alice");
        assertEquals(1, profile.getTopScores().size());
        assertEquals(COINS_MEDIUM, profile.getTopScores().get(0).score());
    }

    @Test
    void topScoresAreSortedDescending() {
        profile.saveScore(100, "A");
        profile.saveScore(COINS_LARGE, "B");
        profile.saveScore(COINS_SMALL, "C");
        assertEquals(COINS_LARGE, profile.getTopScores().get(0).score());
        assertEquals(100, profile.getTopScores().get(2).score());
    }

    @Test
    void topScoresTrimmedToTen() {
        for (int i = 1; i <= SCORE_HIGH_TOP_SCORES; i++) {
            profile.saveScore(i * 10, "Player");
        }
        assertEquals(SCORE_HIGH_TOP_SCORES, profile.getTopScores().size());
        assertEquals(TOP_SCORES_TRIMMED_SIZE, profile.getTopScores().get(0).score());
    }

    @Test
    void topScoresListIsUnmodifiable() {
        profile.saveScore(100, "A");
        assertThrows(UnsupportedOperationException.class,
                () -> profile.getTopScores().clear());
    }

    // ── persistence ──

    @Test
    void profilePersistsAcrossInstances(@TempDir final Path tmpDir) {
        final Path file = tmpDir.resolve("profile.json");
        final var repo = JsonPlayerProfileRepository.create(file);
        repo.persist(new PlayerProfileData("", 0, List.of(STANDARD_PLANE_ID), List.of()));
        final PlayerProfile p1 = new PlayerProfile(repo);
        p1.addCoins(SCORE_SMALL);
        p1.addOwnedPlane("heavy");
        p1.saveScore(COINS_HUGE, "Bob");

        final PlayerProfile p2 = new PlayerProfile(JsonPlayerProfileRepository.create(file));
        assertEquals(SCORE_SMALL, p2.getCoins());
        assertTrue(p2.ownsPlane("heavy"));
        assertEquals(COINS_HUGE, p2.getTopScores().get(0).score());
    }
}
