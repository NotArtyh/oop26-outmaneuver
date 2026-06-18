package outmaneuver.controller.impl;

import java.awt.Dimension;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.InternalEvent;
import outmaneuver.controller.MissileController;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.missile.data.MissileData;
import outmaneuver.model.area.entity.missile.data.MissileRepository;
import outmaneuver.model.area.entity.missile.type.BasicMissile;
import outmaneuver.model.area.entity.missile.type.BounceMissile;
import outmaneuver.model.area.entity.missile.type.ClockMissile;
import outmaneuver.model.area.entity.missile.type.FastMissile;
import outmaneuver.model.area.entity.missile.type.ShieldMissile;
import outmaneuver.model.area.entity.missile.type.SniperMissile;
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
    private static final double START_DELAY      = 3.0;
    private static final double INITIAL_INTERVAL = 2.5;
    private static final double MIN_INTERVAL     = 0.35;
    private static final double INTERVAL_SCALE   = 0.018;
    private static final int    BORDER_MARGIN    = 60;

    // --- SOGLIE DIFFICOLTA' (secondi) ---
    private static final double TIER1_TIME = 15.0;
    private static final double TIER2_TIME = 30.0;
    private static final double TIER3_TIME = 60.0;

    private final MissileRepository missileRepo;
    private final Random rng = new Random();

    private double startDelay    = START_DELAY;
    private double spawnTimer     = 0;
    private double spawnInterval  = INITIAL_INTERVAL;
    private double elapsedTime    = 0;

    public MissileControllerImpl(final List<Entity> entities,
                                 final CollisionEngine collisionEngine,
                                 final IGameSession session,
                                 final MissileRepository missileRepo) {
        super(entities, collisionEngine, session);
        this.missileRepo = Objects.requireNonNull(missileRepo, "missileRepo must not be null");
    }

    @Override
    public void updateEntities(final long deltaMs) {
        final double dt = deltaMs / 1000.0;
        if (startDelay > 0) {
            startDelay -= dt;
            return;
        }
        final Plane plane = findPlane();
        if (plane == null || getView() == null) {
            return;
        }
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

        final Missile m = createRandom(randomBorderPosition(planePos, screen));
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

    private Plane findPlane() {
        return getEntities().stream()
                .filter(e -> e instanceof Plane)
                .map(e -> (Plane) e)
                .findFirst()
                .orElse(null);
    }

    private Missile createRandom(final Vector2 spawnPos) {
        final String type = randomType();
        final MissileData data = missileRepo.loadByType(type).orElseThrow(
                () -> new IllegalStateException("Missile type not found: " + type));
        return switch (type) {
            case "basic"  -> new BasicMissile(spawnPos, data);
            case "fast"   -> new FastMissile(spawnPos, data);
            case "sniper" -> new SniperMissile(spawnPos, data);
            case "bounce" -> new BounceMissile(spawnPos, data);
            case "clock"  -> new ClockMissile(spawnPos, data);
            case "shield" -> new ShieldMissile(spawnPos, data);
            default       -> new BasicMissile(spawnPos, data);
        };
    }

    private String randomType() {
        if (elapsedTime < TIER1_TIME) {
            return "basic";
        } else if (elapsedTime < TIER2_TIME) {
            return switch (rng.nextInt(5)) {
                case 0, 1, 2 -> "basic";
                case 3       -> "fast";
                default      -> "sniper";
            };
        } else if (elapsedTime < TIER3_TIME) {
            return switch (rng.nextInt(6)) {
                case 0, 1, 2 -> "basic";
                case 3       -> "fast";
                case 4       -> "sniper";
                default      -> "bounce";
            };
        } else {
            return switch (rng.nextInt(8)) {
                case 0, 1, 2 -> "basic";
                case 3       -> "sniper";
                case 4       -> "bounce";
                case 5       -> "clock";
                case 6       -> "shield";
                default      -> "fast";
            };
        }
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
