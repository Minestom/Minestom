package net.minestom.server.snapshot;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.tag.TagReadable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface EntitySnapshot extends Snapshot, TagReadable {
    @NotNull EntityType type();

    @NotNull UUID uuid();

    int id();

    @NotNull Pos position();

    @NotNull Vec velocity();

    @NotNull InstanceSnapshot instance();

    @NotNull ChunkSnapshot chunk();

    @NotNull List<@NotNull PlayerSnapshot> viewers();

    @NotNull List<@NotNull EntitySnapshot> passengers();

    @Nullable EntitySnapshot vehicle();
}
