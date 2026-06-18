package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.view.GameView;

public interface MasterController extends InternalEventListener {

    void handleEvent(OutmaneuverEvent event);

    void attachView(GameView view);

    void start();

    void stop();

    void shutdown();
}
