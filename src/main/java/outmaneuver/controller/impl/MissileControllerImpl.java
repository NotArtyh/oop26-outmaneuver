package outmaneuver.controller.impl;

import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.InternalEvent;
import outmaneuver.controller.MissileController;
import outmaneuver.controller.MissileKind;
import outmaneuver.controller.MissileSpawnDirector;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.util.Vector2;

/**
 * Controller dei missili: decide quando e dove farli nascere, li muove e
 * reagisce alle loro collisioni. Il rilevamento delle collisioni spetta al
 * CollisionEngine; la reazione vera (shield/clock/destroy) ai metodi del missile.
 */
public final class MissileControllerImpl extends EntityControllerImpl implements MissileController {

    // --- COSTANTI SPAWN ---
    // L'intervallo tra due spawn parte da INITIAL_INTERVAL e cala nel tempo
    // (INITIAL_INTERVAL - elapsedTime * INTERVAL_SCALE) fino a MIN_INTERVAL.
    // Curva tarata per una partita di ~5 minuti a chi gioca bene: inizio morbido,
    // rampa lenta, ritmo massimo raggiunto solo verso i 5 minuti.
    private static final double START_DELAY      = 3.0;
    private static final double INITIAL_INTERVAL = 6.5;
    private static final double MIN_INTERVAL     = 0.4;
    private static final double INTERVAL_SCALE   = 0.020;
    private static final int    BORDER_MARGIN    = 60;

    private final MissileRepository missileRepo;
    private final MissileSpawnDirector spawnDirector;
    private final Random rng = new Random();

    private double startDelay    = START_DELAY;
    private double spawnTimer     = 0;
    private double spawnInterval  = INITIAL_INTERVAL;
    private double elapsedTime    = 0;

    public MissileControllerImpl(final List<Entity> entities,
                                 final CollisionEngine collisionEngine,
                                 final IGameSession session,
                                 final MissileRepository missileRepo,
                                 final MissileSpawnDirector spawnDirector) {
        super(entities, collisionEngine, session);
        this.missileRepo = Objects.requireNonNull(missileRepo, "missileRepo must not be null");
        this.spawnDirector = Objects.requireNonNull(spawnDirector, "spawnDirector must not be null");
    }

    @Override
    public void updateEntities(final long deltaMs) {
        final double dt = deltaMs / 1000.0;
        if (startDelay > 0) {
            startDelay -= dt;
            return;
        }
        final Optional<Plane> planeOpt = findPlane();
        if (planeOpt.isEmpty() || getView() == null) {
            return;
        }
        final Plane plane = planeOpt.get();
        final Dimension screen = new Dimension(getView().getWidth(), getView().getHeight());

        elapsedTime += dt;
        maybeSpawn(dt, plane.getPosition(), screen);
        moveMissiles(plane, screen, dt);
    }

    @Override
    public void onInternalEvent(final InternalEvent evt, final Object data) {
        if (!(data instanceof final CollisionData cd)) {
            return;
        }
        switch (evt) {
            case PLANE_MISSILE_COLLISION -> {
                // Colpito il piano, il missile è consumato (il game over lo decide il master).
                destroyIfMissile(cd.getEntityA());
                destroyIfMissile(cd.getEntityB());
            }
            case MISSILE_MISSILE_COLLISION -> {
                // Reazione polimorfica: shield regge, clock rallenta, gli altri esplodono.
                reactIfMissile(cd.getEntityA());
                reactIfMissile(cd.getEntityB());
            }
            default -> { }
        }
    }

    @Override
    public void clearAll() {
        super.clearAll();
        startDelay    = START_DELAY;
        spawnTimer    = 0;
        elapsedTime   = 0;
        spawnInterval = INITIAL_INTERVAL;
    }

    private void maybeSpawn(final double dt, final Vector2 planePos, final Dimension screen) {
        spawnTimer += dt;
        if (spawnTimer < spawnInterval) {
            return;
        }
        spawnTimer = 0;
        spawnInterval = Math.max(MIN_INTERVAL, INITIAL_INTERVAL - elapsedTime * INTERVAL_SCALE);

        final Missile m = createMissile(randomBorderPosition(planePos, screen));
        m.setInitialDirection(planePos);
        spawnEntity(m);
    }

    private void moveMissiles(final Plane plane, final Dimension screen, final double dt) {
        for (final Missile m : activeMissiles()) {
            if (m.isAlive()) {
                m.update(plane, dt);
            }
            if (m.isAlive()) {
                m.checkBounce(plane.getPosition(), screen);
                m.redirectIfOutOfBounds(plane, screen);
            }
            if (!m.isAlive()) {
                removeEntity(m);
            }
        }
    }

    private void destroyIfMissile(final ICollidable e) {
        if (e instanceof final Missile m) {
            m.destroy();
        }
    }

    private void reactIfMissile(final ICollidable e) {
        if (e instanceof final Missile m) {
            m.onCollision(activeMissiles());
        }
    }

    private List<Missile> activeMissiles() {
        return getEntities().stream()
                .filter(e -> e instanceof Missile)
                .map(e -> (Missile) e)
                .toList();
    }

    private Optional<Plane> findPlane() {
        return getEntities().stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst();
    }

    private Missile createMissile(final Vector2 spawnPos) {
        // Il tipo lo decide il director in base a tempo e missili a schermo; qui si istanzia.
        final MissileKind kind = spawnDirector.nextKind(elapsedTime, activeMissiles());
        final MissileData data = missileRepo.loadByType(kind.id()).orElseThrow(
                () -> new IllegalStateException("Missile type not found: " + kind.id()));
        return kind.create(spawnPos, data);
    }

    private Vector2 randomBorderPosition(final Vector2 planePos, final Dimension screen) {
        final double cx = planePos.getX();
        final double cy = planePos.getY();
        final double halfW = screen.width  / 2.0;
        final double halfH = screen.height / 2.0;
        return switch (rng.nextInt(4)) {
            case 0  -> new Vector2(cx + (rng.nextDouble() * 2 - 1) * halfW, cy - halfH - BORDER_MARGIN);
            case 1  -> new Vector2(cx + (rng.nextDouble() * 2 - 1) * halfW, cy + halfH + BORDER_MARGIN);
            case 2  -> new Vector2(cx - halfW - BORDER_MARGIN, cy + (rng.nextDouble() * 2 - 1) * halfH);
            default -> new Vector2(cx + halfW + BORDER_MARGIN, cy + (rng.nextDouble() * 2 - 1) * halfH);
        };
    }
}
