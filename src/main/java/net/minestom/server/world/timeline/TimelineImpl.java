package net.minestom.server.world.timeline;

import net.minestom.server.world.attribute.EnvironmentAttribute;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public record TimelineImpl(
        @Nullable Integer periodTicks,
        Map<EnvironmentAttribute<?>, Track<?, ?>> tracks
) implements Timeline {

    public TimelineImpl {
        tracks = Map.copyOf(tracks);
    }

}
