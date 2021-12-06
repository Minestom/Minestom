package net.minestom.server.snapshot;

import net.minestom.server.tag.TagReadable;
import net.minestom.server.world.DimensionType;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface InstanceSnapshot extends Snapshot, TagReadable {
    @NotNull DimensionType dimensionType();

    long worldAge();

    long time();

    @NotNull List<@NotNull ChunkSnapshot> chunks();

    @NotNull List<@NotNull EntitySnapshot> entities();

    @NotNull List<@NotNull PlayerSnapshot> players();
}
