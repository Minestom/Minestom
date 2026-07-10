package net.minestom.server.timer;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;

final class TaskScheduleImpl {
    static final TaskSchedule NEXT_TICK = new TickSchedule(1);
    static final TaskSchedule PARK = new Park();
    static final TaskSchedule STOP = new Stop();
    static final TaskSchedule IMMEDIATE = new Immediate();

    record DurationSchedule(Duration duration) implements TaskSchedule {
    }

    record TickSchedule(int tick) implements TaskSchedule {
        public TickSchedule {
            if (tick <= 0)
                throw new IllegalArgumentException("Tick must be greater than 0 (" + tick + ")");
        }
    }

    record FutureSchedule(CompletableFuture<?> future) implements TaskSchedule {
    }

    record Park() implements TaskSchedule {
    }

    record Stop() implements TaskSchedule {
    }

    record Immediate() implements TaskSchedule {
    }
}
