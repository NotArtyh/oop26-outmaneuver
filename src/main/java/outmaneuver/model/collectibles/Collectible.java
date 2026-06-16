package outmaneuver.model.collectibles;

import outmaneuver.model.Entity;
import outmaneuver.model.area.Plane;
import outmaneuver.model.session.IGameSession;


public interface Collectible extends Entity {

    void apply(Plane plane, IGameSession session);
}
