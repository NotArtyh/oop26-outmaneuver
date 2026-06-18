package outmaneuver.controller;

import java.util.function.BiFunction;

import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.type.BasicMissile;
import outmaneuver.model.area.entity.missile.type.BounceMissile;
import outmaneuver.model.area.entity.missile.type.ClockMissile;
import outmaneuver.model.area.entity.missile.type.FastMissile;
import outmaneuver.model.area.entity.missile.type.ShieldMissile;
import outmaneuver.model.area.entity.missile.type.SniperMissile;
import outmaneuver.util.Vector2;

/**
 * Catalogo dei tipi di missile con i loro metadati di difficoltà.
 * Per ogni tipo tiene insieme, in un unico posto:
 *  - l'id (la stringa usata nel JSON e nel render),
 *  - il peso di minaccia (quanto rende dura la situazione a schermo),
 *  - il tempo di sblocco (da quanti secondi di gioco può comparire),
 *  - la factory che lo costruisce.
 *
 * Aggiungere un nuovo tipo di missile = aggiungere una sola riga qui.
 */
public enum MissileKind {

    // id        minaccia  sblocco(s)  factory
    BASIC ("basic",   1,    0.0, BasicMissile::new),
    BOUNCE("bounce",  1,   12.0, BounceMissile::new),
    SNIPER("sniper",  2,   28.0, SniperMissile::new),
    FAST  ("fast",    4,   45.0, FastMissile::new),
    CLOCK ("clock",   1,   70.0, ClockMissile::new),
    SHIELD("shield",  2,   70.0, ShieldMissile::new);

    private final String id;
    private final int threat;
    private final double unlockTime;
    private final BiFunction<Vector2, MissileData, Missile> factory;

    MissileKind(final String id, final int threat, final double unlockTime,
                final BiFunction<Vector2, MissileData, Missile> factory) {
        this.id = id;
        this.threat = threat;
        this.unlockTime = unlockTime;
        this.factory = factory;
    }

    public String id() {
        return id;
    }

    int threat() {
        return threat;
    }

    boolean isUnlockedAt(final double elapsedTime) {
        return elapsedTime >= unlockTime;
    }

    public Missile create(final Vector2 spawnPos, final MissileData data) {
        return factory.apply(spawnPos, data);
    }

    static MissileKind fromId(final String id) {
        for (final MissileKind k : values()) {
            if (k.id.equals(id)) {
                return k;
            }
        }
        throw new IllegalArgumentException("Unknown missile kind: " + id);
    }
}
