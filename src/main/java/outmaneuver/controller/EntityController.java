package outmaneuver.controller;

import java.util.List;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.Entity;
import outmaneuver.model.area.Plane;


public interface EntityController extends InternalEventListener {

    void updateEntities(long deltaMs);

    void clearAll();

    void spawnPlane(Entity plane);

    void spawnMissile(Entity missile);

    void spawnCollectible(Entity collectible);

    void removeEntity(Entity entity);

    List<Entity> getEntities();

    Plane getPlane();
}
