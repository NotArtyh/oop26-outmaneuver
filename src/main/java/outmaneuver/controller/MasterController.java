package outmaneuver.controller;

import outmaneuver.controller.event.InternalEventListener;
import outmaneuver.view.GameView;
import outmaneuver.controller.event.GameEvent;

public interface MasterController extends InternalEventListener {

    void handleEvent(GameEvent event);

    void attachView(GameView view);

    void start();

    void stop();

    void shutdown();
}
