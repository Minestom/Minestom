package net.minestom.server.world;

import net.minestom.server.codec.Codec;

public enum MoonPhase {
    FULL_MOON,
    WANING_GIBBOUS,
    THIRD_QUARTER,
    WANING_CRESCENT,
    NEW_MOON,
    WAXING_CRESCENT,
    FIRST_QUARTER,
    WAXING_GIBBOUS;

    public static final Codec<MoonPhase> CODEC = Codec.Enum(MoonPhase.class);
}
