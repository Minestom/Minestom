package net.minestom.server.snapshot;

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
    static <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        return (T) new SnapshotUpdaterImpl(snapshotable).update();
    }

    static void invalidateSnapshot(@NotNull Snapshotable snapshotable) {
        SnapshotUpdaterImpl.invalidate(snapshotable);
    }

    @NotNull Collection<Snapshotable> invalidations();

    <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable);

    default <T extends Snapshot> @Nullable AtomicReference<T> optionalReference(@Nullable Snapshotable snapshotable) {
        return snapshotable != null ? reference(snapshotable) : null;
    }

    <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotables);

    default <T extends Snapshot, S extends Snapshotable, K> @NotNull Map<K, AtomicReference<T>> referencesMap(@NotNull Collection<S> snapshotable,
                                                                                                              @NotNull Function<S, K> mappingFunction) {
        return snapshotable.stream().collect(Collectors.toUnmodifiableMap(mappingFunction, this::reference));
    }
}
