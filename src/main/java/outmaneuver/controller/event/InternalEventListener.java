package outmaneuver.controller.event;

@FunctionalInterface
public interface InternalEventListener {
    void onInternalEvent(Event evt, Object data);
}
