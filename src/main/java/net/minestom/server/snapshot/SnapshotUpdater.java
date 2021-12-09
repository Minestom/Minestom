package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the context of a snapshot build.
 * Used in {@link Snapshotable#updateSnapshot(SnapshotUpdater)} to create snapshot references and avoid circular dependencies.
 * <p>
 * Implementations do not need to be thread-safe and cannot be re-used.
 */
public interface SnapshotUpdater {
    /**
     * Updates the snapshot of the given snapshotable.
     * <p>
     * Method must be called during a safe-point (when the server state is stable).
     *
     * @param snapshotable the snapshot container
     * @param <T>          the snapshot type
     * @return the new updated snapshot
     */
    static <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        return (T) new SnapshotUpdaterImpl(snapshotable).update();
    }

    /**
     * Invalidates a snapshot. Update will occur on next snapshot build using {@link #update(Snapshotable)}.
     * <p>
     * Invalidation can be expensive, as many references may be affected.
     *
     * @param snapshotable the snapshot container
     */
    static void invalidateSnapshot(@NotNull Snapshotable snapshotable) {
        SnapshotUpdaterImpl.invalidate(snapshotable);
    }

    /**
     * Retrieves all the snapshot holders that have been invalidated, requiring this snapshot to be potentially updated.
     * Useful to avoid re-computing up-to-date fields.
     * <p>
     * The collection may be ignored if the snapshot has never been built yet.
     *
     * @return the collection of snapshot holders that have been invalidated
     */
    @NotNull Collection<@NotNull Snapshotable> invalidations();

    <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable);

    default <T extends Snapshot> @Nullable AtomicReference<T> optionalReference(@Nullable Snapshotable snapshotable) {
        return snapshotable != null ? reference(snapshotable) : null;
    }

    <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotables);

    default <T extends Snapshot, S extends Snapshotable, K> @NotNull Map<K, AtomicReference<T>> referencesMap(@NotNull Collection<S> snapshotables,
                                                                                                              @NotNull Function<S, K> mappingFunction) {
        return snapshotables.stream().collect(Collectors.toUnmodifiableMap(mappingFunction, this::reference));
    }

    default <T extends Snapshot, S extends Snapshotable> @NotNull Map<Long, AtomicReference<T>> referencesMapLong(@NotNull Collection<S> snapshotables,
                                                                                                                  @NotNull Function<S, Long> mappingFunction) {
        Long2ObjectOpenHashMap<AtomicReference<T>> map = new Long2ObjectOpenHashMap<>(snapshotables.size());
        for (S snapshotable : snapshotables) {
            map.put(mappingFunction.apply(snapshotable).longValue(), reference(snapshotable));
        }
        map.trim();
        return map;
    }

    default <T extends Snapshot, S extends Snapshotable> @NotNull Map<Integer, AtomicReference<T>> referencesMapInt(@NotNull Collection<S> snapshotables,
                                                                                                                    @NotNull Function<S, Integer> mappingFunction) {
        Int2ObjectOpenHashMap<AtomicReference<T>> map = new Int2ObjectOpenHashMap<>(snapshotables.size());
        for (S snapshotable : snapshotables) {
            map.put(mappingFunction.apply(snapshotable).intValue(), reference(snapshotable));
        }
        map.trim();
        return map;
    }
}
