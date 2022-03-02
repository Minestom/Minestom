package net.minestom.server.entity;

import it.unimi.dsi.fastutil.ints.IntList;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.snapshot.ChunkSnapshot;
import net.minestom.server.snapshot.EntitySnapshot;
import net.minestom.server.snapshot.InstanceSnapshot;
import net.minestom.server.snapshot.PlayerSnapshot;
import net.minestom.server.tag.Tag;
import net.minestom.server.tag.TagReadable;
import net.minestom.server.utils.collection.MappedCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

final class EntitySnapshotImpl {

     record Entity(EntityType type, UUID uuid, int id, Pos position, Vec velocity,
                                      AtomicReference<InstanceSnapshot> instanceRef, int chunkX, int chunkZ,
                                      IntList viewersId, IntList passengersId, int vehicleId,
                                      TagReadable tagReadable) implements EntitySnapshot {
        @Override
        public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
            return tagReadable.getTag(tag);
        }

        @Override
        public @NotNull InstanceSnapshot instance() {
            return instanceRef.getPlain();
        }

        @Override
        public @NotNull ChunkSnapshot chunk() {
            return Objects.requireNonNull(instance().chunk(chunkX, chunkZ));
        }

        @Override
        public @NotNull Collection<@NotNull PlayerSnapshot> viewers() {
            return new MappedCollection<>(viewersId, id -> instance().player(id));
        }

        @Override
        public @NotNull Collection<@NotNull EntitySnapshot> passengers() {
            return new MappedCollection<>(passengersId, id -> instance().entity(id));
        }

        @Override
        public @Nullable EntitySnapshot vehicle() {
            if (vehicleId == -1) return null;
            return instance().entity(vehicleId);
        }
    }

     record Player(EntitySnapshot snapshot, String username,
                                      GameMode gameMode) implements PlayerSnapshot {
        @Override
        public @NotNull EntityType type() {
            return snapshot.type();
        }

        @Override
        public @NotNull UUID uuid() {
            return snapshot.uuid();
        }

        @Override
        public int id() {
            return snapshot.id();
        }

        @Override
        public @NotNull Pos position() {
            return snapshot.position();
        }

        @Override
        public @NotNull Vec velocity() {
            return snapshot.velocity();
        }

        @Override
        public @NotNull InstanceSnapshot instance() {
            return snapshot.instance();
        }

        @Override
        public @NotNull ChunkSnapshot chunk() {
            return snapshot.chunk();
        }

        @Override
        public @NotNull Collection<@NotNull PlayerSnapshot> viewers() {
            return snapshot.viewers();
        }

        @Override
        public @NotNull Collection<@NotNull EntitySnapshot> passengers() {
            return snapshot.passengers();
        }

        @Override
        public @Nullable EntitySnapshot vehicle() {
            return snapshot.vehicle();
        }

        @Override
        public <T> @Nullable T getTag(@NotNull Tag<T> tag) {
            return snapshot.getTag(tag);
        }
    }
}
