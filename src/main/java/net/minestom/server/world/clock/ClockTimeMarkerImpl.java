package net.minestom.server.world.clock;

import net.minestom.server.registry.RegistryKey;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public record ClockTimeMarkerImpl(RegistryKey<WorldClock> clock, int ticks, @Nullable Integer periodTicks,
                                  boolean showInCommands) implements ClockTimeMarker {
    public ClockTimeMarkerImpl {
        Objects.requireNonNull(clock, "clock");
        Check.argCondition(ticks < 0, "ticks must be positive");
    }
}
