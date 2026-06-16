package outmaneuver.model.area;


import outmaneuver.model.Entity;

public interface Plane extends Entity {

    double getDirection();

    void setDirection(double direction);

    PlaneStats getStats();

    void setStats(PlaneStats stats);

    TurnState getTurnState();

    void setTurnState(TurnState state);

    boolean isShieldActive();

    void activateShield();

    void deactivateShield();

    void applySpeedMultiplier(double factor, long durationMs);

    double getEffectiveSpeed();
}
