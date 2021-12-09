package net.minestom.server.snapshot;

import org.jctools.queues.SpscGrowableArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private static final Object MONITOR = new Object();
    private static final Map<Snapshotable, SnapshotReferences> SNAPSHOT_REFERENCES = new WeakHashMap<>();

    static void invalidate(Snapshotable snapshotable) {
        synchronized (MONITOR) {
            SnapshotReferences references = SNAPSHOT_REFERENCES.get(snapshotable);
            if (references == null) {
                references = new SnapshotReferences();
                SNAPSHOT_REFERENCES.put(snapshotable, references);
            }
            references.invalidations.add(snapshotable); // Invalidate self
            // Invalidate snapshots referencing this snapshot
            Set<Snapshotable> interpreted = new HashSet<>();
            recursiveInvalidate(references, snapshotable, interpreted);
        }
    }

    static void recursiveInvalidate(SnapshotReferences references, Snapshotable origin,
                                    Set<Snapshotable> interpreted) {
        for (var snapshotable : references.referencedBy) {
            if (snapshotable == origin)
                continue;
            if (!interpreted.add(snapshotable))
                continue; // Prevent circular dependencies from causing stack overflow
            references = SNAPSHOT_REFERENCES.get(snapshotable);
            if (references == null) continue;
            references.invalidations.add(origin);
            recursiveInvalidate(references, origin, interpreted);
        }
    }

    private final SpscGrowableArrayQueue<Runnable> entries = new SpscGrowableArrayQueue<>(1024);
    private final Collection<Snapshotable> references = new ArrayList<>();
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
        this.references.add(snapshotable);
        synchronized (MONITOR) {
            SNAPSHOT_REFERENCES.computeIfAbsent(snapshotable, s -> new SnapshotReferences())
                    .referencedBy.add(this.snapshotable);
        }
        AtomicReference<T> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> ref.setPlain((T) optionallyUpdate(snapshotable)));
        return ref;
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotables) {
        List<Snapshotable> entries = List.copyOf(snapshotables);
        this.references.addAll(entries);
        synchronized (MONITOR) {
            for (var snapshotable : entries) {
                SNAPSHOT_REFERENCES.computeIfAbsent(snapshotable, s -> new SnapshotReferences())
                        .referencedBy.add(this.snapshotable);
            }
        }
        AtomicReference<List<T>> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> {
            List<T> list = (List<T>) entries.stream().map(this::optionallyUpdate).toList();
            ref.setPlain(list);
        });
        return ref;
    }

    Snapshot update() {
        this.entries.drain(Runnable::run);
        synchronized (MONITOR) {
            SnapshotReferences ref = SNAPSHOT_REFERENCES.computeIfAbsent(this.snapshotable, s -> new SnapshotReferences());
            ref.requiredReferences.clear();
            ref.requiredReferences.addAll(this.references); // Prevent duplicate from the array list
        }
        return snapshotable.snapshot();
    }

    private Snapshot optionallyUpdate(Snapshotable snapshotable) {
        boolean requireUpdate = false;
        synchronized (MONITOR) {
            SnapshotReferences references = SNAPSHOT_REFERENCES.get(snapshotable);
            if (references == null) {
                requireUpdate = true;
                this.invalidations = List.of();
            } else {
                Set<Snapshotable> invalidations = references.invalidations;
                if (!invalidations.isEmpty()) {
                    requireUpdate = true;
                    this.invalidations = List.copyOf(invalidations);
                    invalidations.clear();
                }
            }
        }
        if (requireUpdate) snapshotable.updateSnapshot(this);
        return snapshotable.snapshot();
    }

    private static final class SnapshotReferences {
        // Represents the references required for a snapshot
        private final Set<Snapshotable> requiredReferences = Collections.newSetFromMap(new WeakHashMap<>());
        // Represents where this snapshot is referenced
        private final Set<Snapshotable> referencedBy = Collections.newSetFromMap(new WeakHashMap<>());
        // Represents snapshots that invalidate this snapshot
        private final Set<Snapshotable> invalidations = Collections.newSetFromMap(new WeakHashMap<>());
    }
}
