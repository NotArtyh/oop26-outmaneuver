package outmaneuver.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import outmaneuver.model.area.entity.missile.Missile;

/**
 * Decide QUALE tipo di missile spawnare, in funzione del tempo e dei missili già a schermo.
 *
 * <p>Il modello è descritto da due funzioni matematiche continue:
 *
 * <pre>
 *   1) Difficoltà nel tempo (curva logistica / sigmoide):
 *
 *          D(t) = D_MAX / (1 + e^(-k (t - t0)))
 *
 *      parte quasi piatta, accelera intorno al flesso t0, poi plafona a D_MAX.
 *      Rappresenta la minaccia "desiderata" a schermo in quel momento.
 *
 *   2) Scelta del tipo (gate logistico sullo spazio rimanente):
 *
 *          H        = D(t) - S            (spazio rimanente; S = minaccia già a schermo)
 *          w_i(t,S) = pref_i * sigma(s * (H - threat_i))   [ + bonus clock se H è piccolo ]
 *
 *      sigma(x) = 1 / (1 + e^(-x)) è la sigmoide: un tipo "si accende" gradualmente
 *      quando entra nel budget e "si spegne" quando lo sfora.
 * </pre>
 *
 * In una frase: aumentando t cresce D (più difficile); aumentando S cala H, quindi si
 * scelgono tipi più facili o il clock (che aiuta). Il fast ha anche un tetto rigido.
 *
 * Non si occupa di QUANDO o DOVE spawnare: quello spetta al MissileController.
 * Classe pura (stato solo nel generatore casuale): testabile in isolamento con un seed.
 */
public final class MissileSpawnDirector {

    // (1) Difficoltà logistica nel tempo: D(t) = D_MAX / (1 + e^(-k (t - t0))).
    // Curva delicata: flesso spostato avanti e pendenza dolce → partita più lunga.
    private static final double D_MAX            = 12.0;   // plafond di minaccia a schermo
    private static final double D_MIDPOINT       = 160.0;  // t0: flesso, metà difficoltà (s)
    private static final double D_STEEPNESS      = 0.020;  // k: ripidità della S

    // (2) Gate logistico della scelta: sigma(GATE_STEEPNESS * (H - threat_i)).
    private static final double GATE_STEEPNESS   = 1.5;

    // Preferenze di base per tipo (prima del gate).
    private static final double PREF_STAPLE      = 3.0;    // basic: il tipo "di base", più frequente
    private static final double PREF_NORMAL      = 1.0;    // tutti gli altri

    // Aiuto: quando lo spazio è poco, il clock diventa più probabile (valvola di sfogo).
    private static final double CLOCK_RELIEF     = 1.5;
    private static final double RELIEF_MIDPOINT  = 2.0;    // sotto questo spazio scatta l'aiuto

    // Vincolo rigido: mai più di così tanti fast a schermo.
    private static final int MAX_FAST_ON_SCREEN  = 2;

    private final Random rng;

    public MissileSpawnDirector() {
        this(new Random());
    }

    MissileSpawnDirector(final Random rng) {
        this.rng = rng;
    }

    /**
     * Sceglie il tipo del prossimo missile.
     *
     * @param elapsedTime secondi di gioco trascorsi (t)
     * @param active      missili attualmente in scena
     * @return il tipo scelto
     */
    public MissileKind nextKind(final double elapsedTime, final List<Missile> active) {
        final double headroom = targetThreat(elapsedTime) - currentThreat(active);
        final boolean fastAtCap = countOf(MissileKind.FAST, active) >= MAX_FAST_ON_SCREEN;

        final List<Weighted> pool = new ArrayList<>();
        for (final MissileKind kind : MissileKind.values()) {
            if (!kind.isUnlockedAt(elapsedTime) || (kind == MissileKind.FAST && fastAtCap)) {
                continue;
            }
            pool.add(new Weighted(kind, weightFor(kind, headroom)));
        }
        final MissileKind chosen = pickWeighted(pool);
        return chosen != null ? chosen : MissileKind.BASIC;
    }

    /** Difficoltà desiderata al tempo t: curva logistica D(t) = D_MAX / (1 + e^(-k (t - t0))). */
    private double targetThreat(final double elapsedTime) {
        return D_MAX / (1.0 + Math.exp(-D_STEEPNESS * (elapsedTime - D_MIDPOINT)));
    }

    /** Peso w_i = pref_i * sigma(s (H - threat_i)) [+ bonus clock quando H è piccolo]. */
    private double weightFor(final MissileKind kind, final double headroom) {
        double weight = preference(kind) * sigmoid(GATE_STEEPNESS * (headroom - kind.threat()));
        if (kind == MissileKind.CLOCK) {
            weight += CLOCK_RELIEF * sigmoid(GATE_STEEPNESS * (RELIEF_MIDPOINT - headroom));
        }
        return weight;
    }

    private double preference(final MissileKind kind) {
        return kind == MissileKind.BASIC ? PREF_STAPLE : PREF_NORMAL;
    }

    /** Sigmoide logistica: sigma(x) = 1 / (1 + e^(-x)). */
    private static double sigmoid(final double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private MissileKind pickWeighted(final List<Weighted> pool) {
        final double total = pool.stream().mapToDouble(Weighted::weight).sum();
        if (total <= 0) {
            return null;
        }
        double roll = rng.nextDouble() * total;
        for (final Weighted w : pool) {
            roll -= w.weight();
            if (roll < 0) {
                return w.kind();
            }
        }
        return null;
    }

    /** Minaccia attualmente a schermo: S = somma dei pesi dei missili vivi. */
    private double currentThreat(final List<Missile> active) {
        return active.stream()
                .filter(Missile::isAlive)
                .mapToInt(m -> MissileKind.fromId(m.getMissileType()).threat())
                .sum();
    }

    private long countOf(final MissileKind kind, final List<Missile> active) {
        return active.stream()
                .filter(Missile::isAlive)
                .filter(m -> kind.id().equals(m.getMissileType()))
                .count();
    }

    private record Weighted(MissileKind kind, double weight) { }
}
