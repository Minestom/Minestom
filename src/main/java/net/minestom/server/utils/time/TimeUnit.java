package net.minestom.server.utils.time;

import net.minestom.server.MinecraftServer;

public enum TimeUnit {

    TICK, MILLISECOND;

    public long toMilliseconds(long value) {
        switch (this) {
            case TICK:
                return MinecraftServer.TICK_MS * value;
            case MILLISECOND:
                return value;
        }
        return -1; // Unexpected
    }

}
