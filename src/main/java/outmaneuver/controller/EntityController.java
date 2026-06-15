package outmaneuver.controller;

import java.util.List;

import outmaneuver.model.area.Plane;
import outmaneuver.model.missile.IMissile;

public interface EntityController {

    void updateEntities(long deltaMs);

    void clearAll();

    Plane getPlane();

    // restituisce la lista dei missili che sono attualmente vivi a schermo in quel momento.
    List<IMissile> getMissiles();
}
