package net.minestom.server.utils.time;

import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public final class TimeUnit {
    public static final TemporalUnit DAY = ChronoUnit.DAYS;
    public static final TemporalUnit HOUR = ChronoUnit.HOURS;
    public static final TemporalUnit MINUTE = ChronoUnit.MINUTES;
    public static final TemporalUnit SECOND = ChronoUnit.SECONDS;
    public static final TemporalUnit MILLISECOND = ChronoUnit.MILLIS;
    public static final TemporalUnit SERVER_TICK = Tick.SERVER_TICKS;
    public static final TemporalUnit CLIENT_TICK = Tick.CLIENT_TICKS;

    private TimeUnit() {
    }
}
