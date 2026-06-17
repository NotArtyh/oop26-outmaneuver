package outmaneuver.model.area.entity.missile;

import java.util.List;

import java.awt.Dimension;

import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.plane.Plane;
import outmaneuver.util.Vector2;
import outmaneuver.view.MissileRenderData;

public interface Missile extends Entity {

    // --- UPDATE E MOVIMENTO ---
    void update(Plane plane, double dt);
    void update(long deltaMs);
    boolean redirectIfOutOfBounds(Plane plane, Dimension screenSize);
    void setInitialDirection(Vector2 target);

    // --- STATO ---
    void destroy();
    boolean isAlive();

    // --- COLLISIONE ---
    boolean collidesWith(Plane plane);
    void onCollision(List<Missile> activeMissiles);
    void checkBounce(Vector2 planePos, Dimension screenSize);

    // --- EFFETTI ---
    void freeze(double duration);
    void slowDown(double factor, double duration);

    // --- SPAWN ON INIT ---
    List<Missile> getSpawnOnInit();

    // --- RENDER ---
    MissileRenderData getRenderData();
    boolean isGhostVisible();

    // --- GETTERS ---
    double getHitboxRadius();
    String getMissileType();
}
