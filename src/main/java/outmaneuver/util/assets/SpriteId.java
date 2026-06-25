package outmaneuver.util.assets;

import java.util.Locale;

public enum SpriteId {

    PLANE_STANDARD,
    PLANE_FAST,
    PLANE_HEAVY,
    MISSILE_BASIC,
    MISSILE_FAST,
    MISSILE_SNIPER,
    MISSILE_BOUNCE,
    MISSILE_SHIELD,
    MISSILE_CLOCK,
    COLLECTIBLE_STAR,
    COLLECTIBLE_SPEED,
    COLLECTIBLE_SHIELD,

    EXPLOSION,

    SHIELD,

    CLOUD_1,
    CLOUD_2,
    CLOUD_3;

    public String getFilename() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static SpriteId fromFilename(final String filename) {
        return valueOf(filename.toUpperCase(Locale.ROOT));
    }
}
