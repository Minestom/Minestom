package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Contract;

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
    static <T extends Snapshot> T update(Snapshotable snapshotable) {
        return SnapshotUpdaterImpl.update(snapshotable);
    }

    <T extends Snapshot> AtomicReference<T> reference(Snapshotable snapshotable);

    @Contract("!null -> !null")
    default <T extends Snapshot> AtomicReference<T> optionalReference(Snapshotable snapshotable) {
        return snapshotable != null ? reference(snapshotable) : null;
    }

    default <T extends Snapshot, S extends Snapshotable, K> Map<K, AtomicReference<T>> referencesMap(Collection<S> snapshotables,
                                                                                                              Function<S, K> mappingFunction) {
        return snapshotables.stream().collect(Collectors.toUnmodifiableMap(mappingFunction, this::reference));
    }

    default <T extends Snapshot, S extends Snapshotable> Map<Long, AtomicReference<T>> referencesMapLong(Collection<S> snapshotables,
                                                                                                                  ToLongFunction<S> mappingFunction) {
        Long2ObjectOpenHashMap<AtomicReference<T>> map = new Long2ObjectOpenHashMap<>(snapshotables.size());
        for (S snapshotable : snapshotables) {
            map.put(mappingFunction.applyAsLong(snapshotable), reference(snapshotable));
        }
        map.trim();
        return map;
    }

    default <T extends Snapshot, S extends Snapshotable> Map<Integer, AtomicReference<T>> referencesMapInt(Collection<S> snapshotables,
                                                                                                                    ToIntFunction<S> mappingFunction) {
        Int2ObjectOpenHashMap<AtomicReference<T>> map = new Int2ObjectOpenHashMap<>(snapshotables.size());
        for (S snapshotable : snapshotables) {
            map.put(mappingFunction.applyAsInt(snapshotable), reference(snapshotable));
        }
        map.trim();
        return map;
    }
}
