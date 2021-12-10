package net.minestom.server.snapshot;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import org.jctools.queues.SpscGrowableArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private static final Object MONITOR = new Object();
    private static final Map<Snapshotable, SnapshotReferences> SNAPSHOT_REFERENCES = new WeakHashMap<>();

    static void invalidate(Snapshotable snapshotable) {
        synchronized (MONITOR) {
            SnapshotReferences references = SNAPSHOT_REFERENCES.computeIfAbsent(snapshotable,
                    s -> new SnapshotReferences());
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
    private final ArrayList<Snapshotable> references = new ArrayList<>();
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
        AtomicReference<T> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> ref.setPlain((T) optionallyUpdate(snapshotable)));
        return ref;
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotables) {
        Object[] array = snapshotables.toArray(); // Array will be reused until the end to avoid allocations
        this.references.ensureCapacity(this.references.size() + array.length);
        for (Object o : array) this.references.add((Snapshotable) o);

        @SuppressWarnings("rawtypes") List wrappedList = ObjectArrayList.wrap(array);
        AtomicReference<List<T>> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> {
            // Update the array content to snapshots instead of snapshotables
            Arrays.setAll(array, i -> optionallyUpdate((Snapshotable) array[i]));
            //noinspection unchecked
            ref.setPlain(wrappedList);
        });
        return ref;
    }

    Snapshot update() {
        this.entries.drain(Runnable::run);
        synchronized (MONITOR) {
            SNAPSHOT_REFERENCES.compute(this.snapshotable, (s, ref) -> {
                if (ref == null) ref = new SnapshotReferences();
                ref.requiredReferences.clear();
                ref.requiredReferences.addAll(this.references); // Prevent duplicate from the array list
                return ref;
            });
            // Update all references to ensure proper invalidation
            for (Snapshotable snapshotable : this.references) {
                SNAPSHOT_REFERENCES.compute(snapshotable, (s, ref) -> {
                    if (ref == null) ref = new SnapshotReferences();
                    ref.referencedBy.add(this.snapshotable);
                    return ref;
                });
            }
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
