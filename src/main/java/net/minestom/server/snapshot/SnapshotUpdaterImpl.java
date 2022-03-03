

package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private final Map<Snapshotable, AtomicReference<Snapshot>> referenceMap = new ConcurrentHashMap<>();
    private List<Entry> queue = new ArrayList<>();

    static <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        var updater = new SnapshotUpdaterImpl();
        var ref = updater.reference(snapshotable);
        updater.update();
        return (T) ref.getPlain();
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable) {
        AtomicReference<Snapshot> ref = new AtomicReference<>();
        var prev = referenceMap.putIfAbsent(snapshotable, ref);
        if (prev == null) {
            synchronized (this) {
                queue.add(new Entry(snapshotable, ref));
            }
            return (AtomicReference<T>) ref;
        }
        return (AtomicReference<T>) prev;
    }

    record Entry(Snapshotable snapshotable, AtomicReference<Snapshot> ref) {
    }

    void update() {
        List<Entry> temp;
        while (!(temp = new ArrayList<>(queue)).isEmpty()) {
            queue = new ArrayList<>();
            temp.parallelStream().forEach(entry -> {
                Snapshotable snap = entry.snapshotable;
                entry.ref.setPlain(Objects.requireNonNull(snap.updateSnapshot(this), "Snapshot must not be null after an update!"));
            });
        }
    }
}
