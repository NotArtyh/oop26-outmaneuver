package outmaneuver.view;

import java.util.List;

public final class RenderState {

    private final EntityRenderData plane;
    private final HudSnapshot hud;
    // [Alessio - missili] lista dei dati di render dei missili
    private final List<EntityRenderData> missiles;
    private final List<EntityRenderData> collectibles;
    private final List<EntityRenderData> collisions;

    private RenderState(final EntityRenderData plane,
                        final HudSnapshot hud,
                        final List<EntityRenderData> missiles,
                        final List<EntityRenderData> collectibles,
                        final List<EntityRenderData> collisions) {
        this.plane = plane;
        this.hud = hud;
        this.missiles = List.copyOf(missiles);
        this.collectibles = List.copyOf(collectibles);
        this.collisions = List.copyOf(collisions);
    }

    public EntityRenderData getPlane() {
        return plane;
    }

    public List<EntityRenderData> getMissiles() {
        return List.copyOf(missiles);
    }

    public HudSnapshot getHud() {
        return hud;
    }

    public List<EntityRenderData> getCollectibles() {
        return List.copyOf(collectibles);
    }

    public List<EntityRenderData> getCollisions() {
        return List.copyOf(collisions);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {

        private EntityRenderData planeData;
        private HudSnapshot hud;
        private List<EntityRenderData> missiles = List.of();
        private List<EntityRenderData> collectibles = List.of();
        private List<EntityRenderData> collisions = List.of();

        private Builder() { }

        public Builder planeData(final EntityRenderData value) {
            this.planeData = value;
            return this;
        }

        public Builder hud(final HudSnapshot value) {
            this.hud = value;
            return this;
        }

        public Builder missiles(final List<EntityRenderData> value) {
            this.missiles = List.copyOf(value);
            return this;
        }

        public Builder collectibles(final List<EntityRenderData> value) {
            this.collectibles = List.copyOf(value);
            return this;
        }

        public Builder collisions(final List<EntityRenderData> value) {
            this.collisions = List.copyOf(value);
            return this;
        }

        public RenderState build() {
            return new RenderState(planeData, hud, missiles, collectibles, collisions);
        }
    }
}
