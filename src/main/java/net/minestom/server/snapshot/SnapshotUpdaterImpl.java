

package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private final Object2ObjectOpenHashMap<Snapshotable, AtomicReference<Snapshot>> referenceMap = new Object2ObjectOpenHashMap<>();
    private List<Entry> queue = new ArrayList<>();

    static <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        var updater = new SnapshotUpdaterImpl();
        var ref = updater.reference(snapshotable);
        updater.update();
        return (T) ref.get();
    }

    @Override
    public synchronized <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable) {
        AtomicReference<Snapshot> ref = new AtomicReference<>();
        var prev = referenceMap.putIfAbsent(snapshotable, ref);
        if (prev != null) return (AtomicReference<T>) prev;
        this.queue.add(new Entry(snapshotable, ref));
        return (AtomicReference<T>) ref;
    }

    record Entry(Snapshotable snapshotable, AtomicReference<Snapshot> ref) {
    }

    void update() {
        List<Entry> temp;
        while (!(temp = new ArrayList<>(queue)).isEmpty()) {
            queue = new ArrayList<>();
            temp.parallelStream().forEach(entry -> {
                Snapshotable snap = entry.snapshotable;
                entry.ref.set(Objects.requireNonNull(snap.updateSnapshot(this), "Snapshot must not be null after an update!"));
            });
        }
    }
}
