package net.minestom.server.snapshot;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.EntityType;
import net.minestom.server.tag.TagReadable;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.UUID;

public sealed interface EntitySnapshot extends Snapshot, TagReadable
        permits PlayerSnapshot, SnapshotImpl.Entity {
    EntityType type();

    UUID uuid();

    int id();

    Pos position();

    Vec velocity();

    InstanceSnapshot instance();

    ChunkSnapshot chunk();

    Collection<PlayerSnapshot> viewers();

    Collection<EntitySnapshot> passengers();

    @Nullable EntitySnapshot vehicle();
}
