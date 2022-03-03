package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collectors;

/**
 * Represents the context of a snapshot build.
 * Used in {@link Snapshotable#updateSnapshot(SnapshotUpdater)} to create snapshot references and avoid circular dependencies.
 * Updaters must never leave scope, as its data may be state related (change according to the currently processed snapshot).
 * <p>
 * Implementations do not need to be thread-safe and cannot be re-used.
 */
@ApiStatus.Experimental
public sealed interface SnapshotUpdater permits SnapshotUpdaterImpl {
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
        return SnapshotUpdaterImpl.update(snapshotable);
    }

    <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable);

    @Contract("!null -> !null")
    default <T extends Snapshot> AtomicReference<T> optionalReference(Snapshotable snapshotable) {
        return snapshotable != null ? reference(snapshotable) : null;
    }

    default <T extends Snapshot, S extends Snapshotable, K> @NotNull Map<K, AtomicReference<T>> referencesMap(@NotNull Collection<S> snapshotables,
                                                                                                              @NotNull Function<S, K> mappingFunction) {
        return snapshotables.stream().collect(Collectors.toUnmodifiableMap(mappingFunction, this::reference));
    }

    default <T extends Snapshot, S extends Snapshotable> @NotNull Map<Long, AtomicReference<T>> referencesMapLong(@NotNull Collection<S> snapshotables,
                                                                                                                  @NotNull ToLongFunction<S> mappingFunction) {
        Long2ObjectOpenHashMap<AtomicReference<T>> map = new Long2ObjectOpenHashMap<>(snapshotables.size());
        for (S snapshotable : snapshotables) {
            map.put(mappingFunction.applyAsLong(snapshotable), reference(snapshotable));
        }
        map.trim();
        return map;
    }

    default <T extends Snapshot, S extends Snapshotable> @NotNull Map<Integer, AtomicReference<T>> referencesMapInt(@NotNull Collection<S> snapshotables,
                                                                                                                    @NotNull ToIntFunction<S> mappingFunction) {
        Int2ObjectOpenHashMap<AtomicReference<T>> map = new Int2ObjectOpenHashMap<>(snapshotables.size());
        for (S snapshotable : snapshotables) {
            map.put(mappingFunction.applyAsInt(snapshotable), reference(snapshotable));
        }
        map.trim();
        return map;
    }
}
