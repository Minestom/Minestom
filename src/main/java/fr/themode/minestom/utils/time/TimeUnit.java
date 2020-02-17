package fr.themode.minestom.utils.time;

import fr.themode.minestom.MinecraftServer;

public enum TimeUnit {

    TICK, MILLISECOND, SECOND;

    public long toMilliseconds(int value) {
        switch (this) {
            case TICK:
                return MinecraftServer.TICK_MS * value;
            case SECOND:
                return value * 1000;
            case MILLISECOND:
                return value;
        }
        return -1; // Unexpected
    }

}
