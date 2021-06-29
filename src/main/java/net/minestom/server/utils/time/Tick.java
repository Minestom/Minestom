package net.minestom.server.utils.time;

import net.minestom.server.MinecraftServer;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

public class Tick implements TemporalUnit {
    public static final Tick TICKS = new Tick();

    private Tick() {
    }

    @Override
    public Duration getDuration() {
        return Duration.ofMillis(MinecraftServer.TICK_MS);
    }

    @Override
    public boolean isDurationEstimated() {
        return false;
    }

    @Override
    public boolean isDateBased() {
        return false;
    }

    @Override
    public boolean isTimeBased() {
        return true;
    }

    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        //noinspection unchecked
        return (R) temporal.plus(amount, this);
    }

    @Override
    public long between(Temporal temporal1Inclusive, Temporal temporal2Exclusive) {
        return temporal1Inclusive.until(temporal2Exclusive, this);
    }
}
