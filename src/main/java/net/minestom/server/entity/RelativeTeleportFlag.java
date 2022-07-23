package net.minestom.server.entity;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.EnumSet;
import java.util.Set;

@ApiStatus.Experimental
public enum RelativeTeleportFlag {

    X(0),
    Y(1),
    Z(2),
    YAW(3),
    PITCH(4);

    private final int shift;

    RelativeTeleportFlag(int shift) {
        this.shift = shift;
    }

    private int getMask() {
        return 1 << this.shift;
    }

    private boolean isSet(int mask) {
        return (mask & this.getMask()) == this.getMask();
    }

    public static @NotNull Set<RelativeTeleportFlag> unpack(byte mask) {
        Set<RelativeTeleportFlag> set = EnumSet.noneOf(RelativeTeleportFlag.class);
        for (RelativeTeleportFlag flag : values()) if (flag.isSet(mask)) set.add(flag);
        return set;
    }

    public static byte pack(@NotNull RelativeTeleportFlag... flags) {
        byte i = 0;
        for (RelativeTeleportFlag flag : flags) i |= flag.getMask();
        return i;
    }

}
