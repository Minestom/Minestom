package fr.themode.minestom.timer;

import fr.themode.minestom.Main;

public enum TimeUnit {

    TICK, MILLISECOND, SECOND;

    public long toMilliseconds(int value) {
        switch (this) {
            case TICK:
                return Main.TICK_MS * value;
            case SECOND:
                return value * 1000;
            case MILLISECOND:
                return value;
        }
        return -1; // Unexpected
    }

}
