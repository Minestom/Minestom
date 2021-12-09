package net.minestom.server.snapshot;

import java.util.List;

record SnapshotInfoImpl(Class<? extends Snapshot> type,
                        List<Class<? extends Snapshot>> dependencies) implements SnapshotInfo {
    public SnapshotInfoImpl {
        dependencies = List.copyOf(dependencies);
    }
}
