package net.minestom.server.snapshot;

import net.minestom.server.entity.GameMode;
import org.jetbrains.annotations.NotNull;

public interface PlayerSnapshot extends EntitySnapshot {
    @NotNull String username();

    @NotNull GameMode gameMode();
}
