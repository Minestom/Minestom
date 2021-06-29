package net.minestom.server.utils.time;

import net.minestom.server.MinecraftServer;

import java.time.Duration;
import java.time.temporal.Temporal;
import java.time.temporal.TemporalUnit;

/**
 * A TemporalUnit that represents one tick.
 */
public final class Tick implements TemporalUnit {
    /**
     * A TemporalUnit representing the server tick. This is defined using
     * {@link MinecraftServer#TICK_MS}.
     */
    public static Tick SERVER_TICKS = new Tick(MinecraftServer.TICK_MS);

    /**
     * A TemporalUnit representing the client tick. This is always equal to 50ms.
     */
    public static Tick CLIENT_TICKS = new Tick(50);

    private final long milliseconds;
    private final int tps;

    /**
     * Creates a new tick.
     *
     * @param length the length of the tick in milliseconds
     */
    private Tick(long length) {
        if (length <= 0) {
            throw new IllegalArgumentException("length cannot be negative");
        }

        this.milliseconds = length;
        this.tps = Math.toIntExact(Duration.ofSeconds(1).dividedBy(Duration.ofMillis(this.milliseconds)));
    }

    /**
     * Creates a duration from an amount of ticks.
     *
     * @param ticks the amount of ticks
     * @return the duration
     */
    public static Duration server(long ticks) {
        return Duration.of(ticks, SERVER_TICKS);
    }

    /**
     * Creates a duration from an amount of client-side ticks.
     *
     * @param ticks the amount of ticks
     * @return the duration
     */
    public static Duration client(long ticks) {
        return Duration.of(ticks, CLIENT_TICKS);
    }

    /**
     * Gets the number of whole ticks that occur in the provided duration. Note that this
     * method returns an {@code int} as this is the unit that Minecraft stores ticks in.
     *
     * @param duration the duration
     * @return the number of whole ticks in this duration
     * @throws ArithmeticException if the duration is zero or an overflow occurs
     */
    public int fromDuration(Duration duration) {
        return Math.toIntExact(duration.dividedBy(this.getDuration()));
    }

    /**
     * Gets the whole number of these ticks that occur in one second.
     *
     * @return the number
     */
    public int getTicksPerSecond() {
        return this.tps;
    }

    @Override
    public Duration getDuration() {
        return Duration.ofMillis(this.milliseconds);
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

    @SuppressWarnings("unchecked") // following ChronoUnit#addTo
    @Override
    public <R extends Temporal> R addTo(R temporal, long amount) {
        return (R) temporal.plus(amount, this);
    }

    @Override
    public long between(Temporal start, Temporal end) {
        return start.until(end, this);
    }
}