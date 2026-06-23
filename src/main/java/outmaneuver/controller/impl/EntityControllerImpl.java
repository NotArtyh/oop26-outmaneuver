package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.Event;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.session.IGameSession;
import outmaneuver.view.GameView;

public abstract class EntityControllerImpl implements EntityController {

    private final List<Entity> entities;
    private final CollisionEngine collisionEngine;
    private final IGameSession session;
    private GameView view;

    protected EntityControllerImpl(final List<Entity> entities,
                                final CollisionEngine collisionEngine,
                                final IGameSession session) {
        this.entities = Objects.requireNonNull(entities, "entities must not be null");
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
        this.session = Objects.requireNonNull(session, "session must not be null");
    }

    public void setView(final GameView view) {
        this.view = view;
    }

    protected GameView getView() {
        return view;
    }

    @Override
    public void updateEntities(final long deltaMs) {
        // ogni controller implementa il proprio updateEntities
    }

    @Override
    public void spawnEntity(final Entity entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        entities.add(entity);
        collisionEngine.register(entity);
    }
    

    // Rimozione

    @Override
    public void removeEntity(final Entity entity) {
            collisionEngine.unregister(entity);
            entities.remove(entity);
    }

    @Override
    public void clearAll(){
    entities.removeIf(e -> {
        if (!(e instanceof Plane)) {
            collisionEngine.unregister(e);
            return true;
        }
        return false;
        });
    }

    @Override
    public List<Entity> getEntities() { return List.copyOf(entities); }


    // [Collisioni] Reazione a TUTTE le collisioni in un unico posto. La reazione vera e' POLIMORFICA:
    // ogni entita' decide da se' (missile.onCollision / collectible.apply); qui si smista soltanto.
    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (!(data instanceof final CollisionData cd)) {
            return;
        }
        switch ((CollisionEvent) evt) {
            //AGGIUNTO: piano-missile e missile-missile gestiti UGUALE -> reazione polimorfica del missile
            // (shield regge, clock rallenta, gli altri esplodono). Il game over (se l'aereo non ha lo
            // scudo) lo decide il GameEventController, quindi qui NON serve controllare lo scudo.
            case PLANE_MISSILE_COLLISION, MISSILE_MISSILE_COLLISION -> {
                reactIfMissile(cd.getEntityA());
                reactIfMissile(cd.getEntityB());
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                // il collectible applica il suo effetto (polimorfico) e viene rimosso
                if (cd.getEntityA() instanceof final Plane plane
                        && cd.getEntityB() instanceof final Collectible collectible) {
                    collectible.apply(plane, session);
                    removeEntity(collectible);
                }
            }
            default -> { }
        }
    }

    //AGGIUNTO: fa reagire il missile (onCollision polimorfico) e lo rimuove se muore.
    // Lo shield regge il primo colpo: in quel caso resta vivo e non viene rimosso.
    private void reactIfMissile(final ICollidable e) {
        if (e instanceof final Missile m) {
            m.onCollision(activeMissiles());
            if (!m.isAlive()) {
                removeEntity(m);
            }
        }
    }

    //AGGIUNTO: lista dei missili vivi in scena (serve al clock per rallentare gli altri)
    protected List<Missile> activeMissiles() {
        return getEntities().stream()
                .filter(e -> e instanceof Missile)
                .map(e -> (Missile) e)
                .toList();
    }
}
