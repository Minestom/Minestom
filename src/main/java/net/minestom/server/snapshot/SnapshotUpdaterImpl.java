package net.minestom.server.snapshot;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jctools.queues.SpscGrowableArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private static final ConcurrentMap<Snapshotable, SnapshotReferences> SNAPSHOT_REFERENCES;

    static {
        Cache<Snapshotable, SnapshotReferences> referencesCache = Caffeine.newBuilder().weakKeys().build();
        SNAPSHOT_REFERENCES = referencesCache.asMap();
    }

    static void invalidate(Snapshotable snapshotable) {
        SNAPSHOT_REFERENCES.compute(snapshotable, (snap, references) -> {
            if (references == null) references = new SnapshotReferences();
            references.invalidations.add(snap); // Invalidate self
            // Invalidate snapshots referencing this snapshot
            references.referencedBy.forEach(referencedBy ->
                    SNAPSHOT_REFERENCES.get(referencedBy).invalidations.add(snapshotable));
            return references;
        });
    }

    private final SpscGrowableArrayQueue<Runnable> entries = new SpscGrowableArrayQueue<>(1024);
    private final SnapshotReferences references = new SnapshotReferences();
    private final Snapshotable snapshotable;

    private Collection<Snapshotable> invalidations = List.of();

    SnapshotUpdaterImpl(Snapshotable snapshotable) {
        this.snapshotable = snapshotable;
        optionallyUpdate(snapshotable);
    }

    @Override
    public @NotNull Collection<Snapshotable> invalidations() {
        return invalidations;
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable) {
        this.references.requiredReferences.add(snapshotable);
        SNAPSHOT_REFERENCES.compute(snapshotable, (snap, references) -> {
            if (references == null) references = new SnapshotReferences();
            references.referencedBy.add(this.snapshotable);
            return references;
        });
        AtomicReference<T> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> ref.setPlain((T) optionallyUpdate(snapshotable)));
        return ref;
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotables) {
        this.references.requiredReferences.addAll(snapshotables);
        for (var snapshotable : snapshotables) {
            SNAPSHOT_REFERENCES.compute(snapshotable, (snap, references) -> {
                if (references == null) references = new SnapshotReferences();
                references.referencedBy.add(this.snapshotable);
                return references;
            });
        }
        AtomicReference<List<T>> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> {
            List<T> list = (List<T>) snapshotables.stream().map(this::optionallyUpdate).toList();
            ref.setPlain(list);
        });
        return ref;
    }

    Snapshot update() {
        this.entries.drain(Runnable::run);
        SNAPSHOT_REFERENCES.put(snapshotable, references);
        return snapshotable.snapshot();
    }

    private Snapshot optionallyUpdate(Snapshotable snapshotable) {
        SnapshotReferences references = SNAPSHOT_REFERENCES.get(snapshotable);
        if (references == null) {
            this.invalidations = List.of();
            return update(snapshotable);
        }
        Set<Snapshotable> invalidations = references.invalidations;
        if (invalidations.isEmpty()) return snapshotable.snapshot();
        List<Snapshotable> invalidatedSnapshots = new ArrayList<>(invalidations.size());
        invalidations.removeIf(snap -> {
            invalidatedSnapshots.add(snap);
            return true;
        });
        this.invalidations = List.copyOf(invalidatedSnapshots);
        return update(snapshotable);
    }

    private Snapshot update(Snapshotable snapshotable) {
        return snapshotable.updateSnapshot(this);
    }

    private static final class SnapshotReferences {
        // Represents the references required for a snapshot
        private final Set<Snapshotable> requiredReferences = Collections.newSetFromMap(new WeakHashMap<>());
        // Represents where this snapshot is referenced
        private final Set<Snapshotable> referencedBy = Collections.newSetFromMap(new WeakHashMap<>());
        // Represents snapshots that invalidate this snapshot
        private final Set<Snapshotable> invalidations;

        {
            Cache<Snapshotable, Boolean> invalidationCache = Caffeine.newBuilder().weakKeys().build();
            invalidations = Collections.newSetFromMap(invalidationCache.asMap());
        }
    }
}
