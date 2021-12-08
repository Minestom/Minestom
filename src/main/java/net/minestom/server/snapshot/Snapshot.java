package net.minestom.server.snapshot;

import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

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
        <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Class<T> snapshotType,
                                                                   @NotNull Snapshotable snapshotable);

        default <T extends Snapshot> @Nullable AtomicReference<T> optionalReference(@NotNull Class<T> snapshotType,
                                                                                    @Nullable Snapshotable snapshotable) {
            return snapshotable != null ? reference(snapshotType, snapshotable) : null;
        }

        <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Class<T> snapshotType,
                                                                          @NotNull Collection<? extends Snapshotable> snapshotable);

        default <T extends Snapshot, S extends Snapshotable, K> @NotNull Map<K, AtomicReference<T>> referencesMap(@NotNull Class<T> snapshotType,
                                                                                                                  @NotNull Collection<S> snapshotable,
                                                                                                                  @NotNull Function<S, K> mappingFunction) {
            var map = new HashMap<K, AtomicReference<T>>(snapshotable.size());
            snapshotable.forEach(s -> map.put(mappingFunction.apply(s), reference(snapshotType, s)));
            return map;
        }
    }
}
