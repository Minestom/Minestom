package net.minestom.server.snapshot;

import net.minestom.server.entity.GameMode;

public sealed interface PlayerSnapshot extends EntitySnapshot
        permits SnapshotImpl.Player {
    String username();

    GameMode gameMode();
}
