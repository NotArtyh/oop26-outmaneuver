package outmaneuver.controller.impl;

import java.util.List;
import java.util.Objects;

import outmaneuver.controller.CollisionEngine;
import outmaneuver.controller.EntityController;
import outmaneuver.controller.event.Event;
import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.view.GameView;

// CHECKSTYLE: AbstractClassName OFF
public abstract class EntityControllerImpl implements EntityController {
    // CHECKSTYLE: AbstractClassName ON

    private final List<Entity> entities;
    private final CollisionEngine collisionEngine;
    private InternalEventListener eventListener;
    private GameView view;

    protected EntityControllerImpl(final List<Entity> entities,
                                final CollisionEngine collisionEngine) {
        this.entities = Objects.requireNonNull(entities, "entities must not be null");
        this.collisionEngine = Objects.requireNonNull(collisionEngine, "collisionEngine must not be null");
    }

    @Override
    public final void setView(final GameView view) {
        this.view = view;
    }

    public final void setEventListener(final InternalEventListener listener) {
        this.eventListener = listener;
    }

    protected final GameView getView() {
        return view;
    }

    @Override
    public void updateEntities(final long deltaMs) {
        // ogni controller implementa il proprio updateEntities
    }

    // CHECKSTYLE: DesignForExtension OFF
    @Override
    public void spawnEntity(final Entity entity) {
        Objects.requireNonNull(entity, "entity must not be null");
        entities.add(entity);
        collisionEngine.register(entity);
    }
    // CHECKSTYLE: DesignForExtension ON

    // Rimozione di una singola entity

    @Override
    public final void removeEntity(final Entity entity) {
            collisionEngine.unregister(entity);
            entities.remove(entity);
    }

    @Override
    public void clearAll() {
        // resettato dal controller specifico
    }

    @Override
    public final void removeAll() {
        entities.forEach(collisionEngine::unregister);
        entities.clear();
    }

    @Override
    public final List<Entity> getEntities() {
        return List.copyOf(entities);
    }

    @Override
    public final void onInternalEvent(final Event evt, final Object data) {
        if (eventListener != null) {
            eventListener.onInternalEvent(evt, data);
        }
    }

}
