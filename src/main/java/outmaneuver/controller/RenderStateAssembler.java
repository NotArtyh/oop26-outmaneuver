package outmaneuver.controller;

import java.util.List;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.util.Vector2;
import outmaneuver.view.RenderState;


public interface RenderStateAssembler {

    RenderState assemble(List<Entity> entities, boolean paused, List<Vector2> collisionPoints);

    void reset();
}
    