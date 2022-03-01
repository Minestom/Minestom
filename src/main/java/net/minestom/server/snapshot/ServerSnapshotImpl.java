package net.minestom.server.snapshot;

import net.minestom.server.MinecraftServer;
import net.minestom.server.ServerProcess;
import net.minestom.server.instance.Instance;
import net.minestom.server.utils.collection.MappedCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

record ServerSnapshotImpl(Collection<InstanceSnapshot> instances) implements ServerSnapshot {

    static ServerSnapshot update() {
        final ServerProcess process = MinecraftServer.process();
        final Set<Instance> instances = process.instance().getInstances();
        return SnapshotUpdater.update(updater -> {
            List<AtomicReference<InstanceSnapshot>> list = new ArrayList<>();
            instances.forEach(instance -> list.add(updater.reference(instance)));
            return new ServerSnapshotImpl(MappedCollection.plainReferences(list));
        });
    }
}
