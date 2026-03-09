package net.minestom.server.world.clock;

import net.kyori.adventure.key.Key;
import net.minestom.server.codec.Codec;
import net.minestom.server.registry.RegistryKey;
import org.jetbrains.annotations.Nullable;

public sealed interface ClockTimeMarker extends ClockTimeMarkers permits ClockTimeMarkerImpl {
    // The default keys for ClockTimeMarker aren't a registry currently, just keys for a map.
    Codec<RegistryKey<ClockTimeMarker>> CODEC = RegistryKey.uncheckedCodec();

    static ClockTimeMarker create(RegistryKey<WorldClock> clock, int ticks, @Nullable Integer periodTicks,
                                  boolean showInCommands) {
        return new ClockTimeMarkerImpl(clock, ticks, periodTicks, showInCommands);
    }

    static RegistryKey<ClockTimeMarker> key(Key key) {
        return RegistryKey.unsafeOf(key);
    }

    RegistryKey<WorldClock> clock();

    int ticks();

    @Nullable Integer periodTicks();

    boolean showInCommands();
}
