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
    static @NotNull SnapshotUpdater newUpdater() {
        return new SnapshotUpdaterImpl();
    }

    static <T extends Snapshot> @NotNull T update(@NotNull SnapshotUpdater updater, @NotNull Snapshotable snapshotable) {
        Snapshot snapshot = snapshotable.updateSnapshot(updater);
        updater.apply();
        return (T) snapshot;
    }

    static <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        return update(newUpdater(), snapshotable);
    }

    <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable);

    default <T extends Snapshot> @Nullable AtomicReference<T> optionalReference(@Nullable Snapshotable snapshotable) {
        return snapshotable != null ? reference(snapshotable) : null;
    }

    <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotable);

    default <T extends Snapshot, S extends Snapshotable, K> @NotNull Map<K, AtomicReference<T>> referencesMap(@NotNull Collection<S> snapshotable,
                                                                                                              @NotNull Function<S, K> mappingFunction) {
        return snapshotable.stream().collect(Collectors.toUnmodifiableMap(mappingFunction, this::reference));
    }

    /**
     * Retrieves all references recursively.
     * <p>
     * Must only be after this updater has been used
     */
    void apply();
}
