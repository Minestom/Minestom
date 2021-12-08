package net.minestom.server.snapshot;

import org.jctools.queues.SpscGrowableArrayQueue;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

final class SnapshotUpdaterImpl implements SnapshotUpdater {
    private final SpscGrowableArrayQueue<Runnable> entries = new SpscGrowableArrayQueue<>(1024);

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<T> reference(@NotNull Snapshotable snapshotable) {
        AtomicReference<T> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> ref.setPlain((T) snapshotable.updateSnapshot(this)));
        return ref;
    }

    @Override
    public <T extends Snapshot> @NotNull AtomicReference<List<T>> references(@NotNull Collection<? extends Snapshotable> snapshotable) {
        AtomicReference<List<T>> ref = new AtomicReference<>();
        this.entries.relaxedOffer(() -> {
            List<T> list = (List<T>) snapshotable.stream().map(snap -> snap.updateSnapshot(this)).toList();
            ref.setPlain(list);
        });
        return ref;
    }

    @Override
    public void apply() {
        this.entries.drain(Runnable::run);
    }
}
