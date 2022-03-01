package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Represents the context of a snapshot build.
 * Used in {@link Snapshotable#updateSnapshot(SnapshotUpdater)} to create snapshot references and avoid circular dependencies.
 * Updaters must never leave scope, as its data may be state related (change according to the currently processed snapshot).
 * <p>
 * Implementations do not need to be thread-safe and cannot be re-used.
 */
public sealed interface SnapshotUpdater permits SnapshotUpdaterImpl {

    <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable);

    default <T extends Snapshot> @Nullable AtomicReference<T> optionalReference(@Nullable Snapshotable snapshotable) {
        return snapshotable != null ? reference(snapshotable) : null;
    }

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
