package net.minestom.server.snapshot;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface SnapshotInfo {
    static @NotNull SnapshotInfo of(@NotNull Class<? extends Snapshot> type,
                                    @NotNull List<@NotNull Class<? extends Snapshot>> dependencies) {
        return new SnapshotInfoImpl(type, dependencies);
    }

    @NotNull Class<? extends Snapshot> type();

    @NotNull List<@NotNull Class<? extends Snapshot>> dependencies();
}
