package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private static final Map<Snapshotable, AtomicReference<Snapshot>> REF_CACHE = new ConcurrentHashMap<>();
    private List<Runnable> queue = new ArrayList<>();

    static synchronized <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        var updater = new SnapshotUpdaterImpl();
        updater.reference(snapshotable);
        updater.update();
        REF_CACHE.clear();
        return (T) snapshotable.snapshot();
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable) {
        return (AtomicReference<T>) REF_CACHE.computeIfAbsent(snapshotable, snap -> {
            AtomicReference<Snapshot> ref = new AtomicReference<>();
            queue.add(() -> {
                snap.updateSnapshot(this);
                ref.setPlain(Objects.requireNonNull((T) snap.snapshot()));
            });
            return ref;
        });
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotables) {
        // TODO
        return null;
    }

    void update() {
        List<Runnable> test;
        while (!(test = new ArrayList<>(queue)).isEmpty()) {
            queue = new ArrayList<>();
            test.parallelStream().forEach(Runnable::run);
        }
    }
}
