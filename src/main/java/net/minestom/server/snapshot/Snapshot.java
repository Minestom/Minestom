package net.minestom.server.snapshot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents a snapshot of a game object.
 * <p>
 * Implementations must be valued-based (immutable & not relying on identity).
 */
@ApiStatus.Experimental
public sealed interface Snapshot permits
        ServerSnapshot, InstanceSnapshot,
        EntitySnapshot, ChunkSnapshot, InventorySnapshot {

    interface Updater {
        <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable);

        default <T extends Snapshot> @Nullable AtomicReference<T> optionalReference(@Nullable Snapshotable snapshotable) {
            return snapshotable != null ? reference(snapshotable) : null;
        }

        <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotable);

        default <T extends Snapshot, S extends Snapshotable, K> @NotNull Map<K, AtomicReference<T>> referencesMap(@NotNull Collection<S> snapshotable,
                                                                                                                  @NotNull Function<S, K> mappingFunction) {
            return snapshotable.stream().collect(Collectors.toUnmodifiableMap(mappingFunction, this::reference));
        }
    }
}
