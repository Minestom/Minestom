package net.minestom.server.snapshot;

import net.minestom.server.entity.GameMode;
import org.jetbrains.annotations.NotNull;

public sealed interface PlayerSnapshot extends EntitySnapshot
        permits SnapshotImpl.Player {
    @NotNull String username();

    @NotNull GameMode gameMode();
}
