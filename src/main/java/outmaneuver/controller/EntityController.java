package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.model.area.Plane;


public interface EntityController extends InternalEventListener {

    void updateEntities(long deltaMs);

    void clearAll();

    Plane getPlane();

}
