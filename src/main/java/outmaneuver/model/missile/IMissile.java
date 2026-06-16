package outmaneuver.model.missile;

import java.util.List;

import java.awt.Dimension;

import outmaneuver.model.area.Plane;
import outmaneuver.model.collision.ICollidable;
import outmaneuver.util.Vector2;
import outmaneuver.view.MissileRenderData;


public interface IMissile extends ICollidable {

    // --- UPDATE E MOVIMENTO ---
    void update(Plane plane, double dt);
    boolean redirectIfOutOfBounds(Plane plane, Dimension screenSize);
    void setInitialDirection(Vector2 target);

    // --- STATO ---
    void destroy();
    boolean isAlive();

    // --- COLLISIONE ---
    boolean collidesWith(Plane plane);
    void onCollision(List<IMissile> activeMissiles);
    void checkBounce(Vector2 planePos, Dimension screenSize);

    // --- EFFETTI ---
    void freeze(double duration);
    void slowDown(double factor, double duration);

    // --- SPAWN ON INIT ---
    List<IMissile> getSpawnOnInit();

    // --- RENDER ---
    MissileRenderData getRenderData();
    boolean isGhostVisible();

    // --- GETTERS ---
    double getWorldX();
    double getWorldY();
    double getHitboxRadius();
    String getMissileType();
}