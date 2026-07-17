package net.minestom.server.world.timeline;

import net.minestom.server.registry.RegistryKey;
import net.minestom.server.world.attribute.EnvironmentAttribute;
import net.minestom.server.world.clock.ClockTimeMarker;
import net.minestom.server.world.clock.WorldClock;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

public record TimelineImpl(
        RegistryKey<WorldClock> clock,
        @Nullable Integer periodTicks,
        Map<EnvironmentAttribute<?>, Track<?, ?>> tracks,
        Map<RegistryKey<ClockTimeMarker>, TimeMarkerInfo> timeMarkers
) implements Timeline {

    public TimelineImpl {
        Objects.requireNonNull(clock, "clock");
        tracks = Map.copyOf(tracks);
        timeMarkers = Map.copyOf(timeMarkers);
    }
}
