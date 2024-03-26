package net.minestom.server.entity;

import org.jetbrains.annotations.NotNull;

public enum RelativeFlag {
    X(0x01),
    Y(0x02),
    Z(0x04),
    YAW(0x08),
    PITCH(0x10);

    private final int bit;

    RelativeFlag(int bit) {
        this.bit = bit;
    }

    public int bit() {
        return bit;
    }

    public static int getBitsFromFlags(RelativeFlag... flags) {
        int bits = 0;
        for (RelativeFlag flag : flags) {
            bits = bits | flag.bit();
        }

        return bits;
    }

    public static RelativeFlag @NotNull [] allFlags() {
        return new RelativeFlag[] { X, Y, Z, YAW, PITCH };
    }

    public static RelativeFlag @NotNull [] axisFlags() {
        return new RelativeFlag[] { X, Y, Z };
    }

    public static RelativeFlag @NotNull [] lookFlags() {
        return new RelativeFlag[] { YAW, PITCH };
    }
}