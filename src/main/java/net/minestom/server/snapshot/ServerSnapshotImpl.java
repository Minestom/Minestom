package net.minestom.server.snapshot;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.MappedCollection;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

record ServerSnapshotImpl(Collection<InstanceSnapshot> instances) implements ServerSnapshot {
    static volatile ServerSnapshot snapshot;

    static ServerSnapshot get() {
        return snapshot;
    }

    @ApiStatus.Internal
    static void update() {
        final Set<Instance> instances = MinecraftServer.getInstanceManager().getInstances();
        var updater = new SnapshotUpdaterImpl();
        updater.update();
        SnapshotUpdater.update(new Snapshotable() {
            @Override
            public @NotNull Snapshot snapshot() {
                return snapshot;
            }

            @Override
            public void updateSnapshot(@NotNull SnapshotUpdater updater) {
                List<AtomicReference<InstanceSnapshot>> list = new ArrayList<>();
                instances.forEach(instance -> list.add(updater.reference(instance)));
                snapshot = new ServerSnapshotImpl(MappedCollection.plainReferences(list));
            }
        });
    }
}
