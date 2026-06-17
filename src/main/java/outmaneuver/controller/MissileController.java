package outmaneuver.controller;

import java.util.List;

import outmaneuver.model.area.collision.ICollidable;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.model.area.entity.missile.Missile;
import outmaneuver.view.EntityRenderData;

public interface MissileController {

    void update(Plane plane, double dt);

    List<EntityRenderData> getRenderData();

    List<Missile> getActiveMissiles();

    void onMissileMissileCollision(ICollidable a, ICollidable b);

    void onPlaneHit(ICollidable a, ICollidable b);

    void reset();
}
