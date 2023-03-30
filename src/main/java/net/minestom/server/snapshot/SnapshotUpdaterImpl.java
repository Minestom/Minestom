

package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private final IdentityHashMap<Snapshotable, AtomicReference<Snapshot>> referenceMap = new IdentityHashMap<>();
    private IdentityHashMap<Snapshotable, AtomicReference<Snapshot>> readOnlyReferenceMap;
    private List<Entry> queue = new ArrayList<>();

    static <T extends Snapshot> @NotNull T update(@NotNull Snapshotable snapshotable) {
        var updater = new SnapshotUpdaterImpl();
        var ref = updater.reference(snapshotable);
        updater.update();
        return (T) ref.get();
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable) {
        AtomicReference<Snapshot> ref;
        // Very often the same snapshotable is referenced multiple times.
        var readOnly = this.readOnlyReferenceMap;
        if (readOnly != null && (ref = readOnly.get(snapshotable)) != null) {
            return (AtomicReference<T>) ref;
        }
        // If this is a new snapshotable, we need to create a new reference.
        synchronized (this) {
            ref = new AtomicReference<>();
            var prev = referenceMap.putIfAbsent(snapshotable, ref);
            if (prev != null) return (AtomicReference<T>) prev;
            this.queue.add(new Entry(snapshotable, ref));
            return (AtomicReference<T>) ref;
        }
    }

    record Entry(Snapshotable snapshotable, AtomicReference<Snapshot> ref) {
    }

    void update() {
        List<Entry> temp;
        while (!(temp = new ArrayList<>(queue)).isEmpty()) {
            queue = new ArrayList<>();
            readOnlyReferenceMap = (IdentityHashMap<Snapshotable, AtomicReference<Snapshot>>) referenceMap.clone();
            temp.parallelStream().forEach(entry -> {
                Snapshotable snap = entry.snapshotable;
                entry.ref.set(Objects.requireNonNull(snap.updateSnapshot(this), "Snapshot must not be null after an update!"));
            });
        }
    }
}
