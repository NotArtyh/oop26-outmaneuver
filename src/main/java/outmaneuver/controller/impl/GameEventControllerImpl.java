package outmaneuver.controller.impl;

import java.util.Objects;

import outmaneuver.controller.GameEventController;
import outmaneuver.controller.ScoreController;
import outmaneuver.controller.event.CollisionEvent;
import outmaneuver.controller.event.EffectEvent;
import outmaneuver.controller.event.Event;

import outmaneuver.controller.impl.missile.MissileControllerImpl;
import outmaneuver.model.area.collision.CollisionData;
import outmaneuver.model.area.entity.Entity;
import outmaneuver.model.area.entity.collectibles.Collectible;
import outmaneuver.model.area.entity.collectibles.ShieldPowerUp;
import outmaneuver.model.area.entity.collectibles.SpeedBoost;

public final class GameEventControllerImpl implements GameEventController {

    private final PlaneControllerImpl planeController;
    private final CollectibleControllerImpl collectibleController;
    private final MissileControllerImpl missileController;
    private final ScoreController scoreController;
    private final Runnable onGameOver;
    private boolean shieldActive;

    public GameEventControllerImpl(
            final MasterControllerImpl master,
            final ScoreController scoreController,
            final Runnable onGameOver) {
        this.planeController = master.getEntityController(PlaneControllerImpl.class).orElseThrow();
        this.collectibleController = master.getEntityController(CollectibleControllerImpl.class).orElseThrow();
        this.missileController = master.getEntityController(MissileControllerImpl.class).orElseThrow();
        this.scoreController = scoreController;
        this.onGameOver = Objects.requireNonNull(onGameOver, "onGameOver must not be null");
    }

    @Override
    public void onInternalEvent(final Event evt, final Object data) {
        if (!(data instanceof final CollisionData collisionData)) {
            return;
        }

        switch ((CollisionEvent) evt) {
            case PLANE_MISSILE_COLLISION -> {
                planeController.removeEntity((Entity) collisionData.getEntityA());
                if (!shieldActive) {
                    onGameOver.run();
                }
            }
            case PLANE_COLLECTIBLE_COLLISION -> {
                final var collectible = (Collectible) collisionData.getEntityB();
                if (collectible.getEffect() != null) {
                    collectibleController.addEffect(collectible.getEffect());
                }
                collectibleController.removeEntity(collectible);
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.PLANE_COLLECTIBLE_COLLISION, collectible);
                }
            }
            case MISSILE_MISSILE_COLLISION -> {
                missileController.removeEntity((Entity) collisionData.getEntityA());
                missileController.removeEntity((Entity) collisionData.getEntityB());
                if (scoreController != null) {
                    scoreController.onInternalEvent(CollisionEvent.MISSILE_MISSILE_COLLISION, collisionData);
                }
            }
        }
    }
}
