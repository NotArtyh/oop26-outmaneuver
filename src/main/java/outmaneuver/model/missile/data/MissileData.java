package outmaneuver.model.missile.data;

import java.util.Objects;

/*
 * Record con tutti i parametri possibili di un missile.
 * I campi specifici per tipo hanno default 0 se non usati.
 */
public record MissileData(
        String type,
        double speed,
        double maxTurn,
        double radius,
        double lifetime,

        // GhostMissile
        double visibleDuration,
        double invisibleDuration,

        // FreezeMissile
        double freezeRadius,
        double freezeDuration,

        // ClockMissile
        double slowFactor,
        double slowDuration,

        // ShieldMissile
        double shieldRadius,

        // TwinsMissile
        double spread,

        // BounceMissile — niente di extra

        // SniperMissile — niente di extra

        // Predizione redirect
        double predictionTime
) {
    public MissileData {
        Objects.requireNonNull(type, "type must not be null");
        if (speed <= 0) throw new IllegalArgumentException("speed must be positive");
        if (radius <= 0) throw new IllegalArgumentException("radius must be positive");
    }
}