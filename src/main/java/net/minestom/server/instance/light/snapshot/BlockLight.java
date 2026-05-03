package net.minestom.server.instance.light.snapshot;

import net.minestom.server.instance.light.LightCompute;

class BlockLight {
    private final byte[] light = LightCompute.EMPTY_CONTENT;

    byte[] get() {
        return light;
    }
}
