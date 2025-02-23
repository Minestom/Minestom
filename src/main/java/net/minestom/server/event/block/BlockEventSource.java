package net.minestom.server.event.block;

import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.PlayerHand;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents the source the {@link net.minestom.server.event.trait.BlockEvent} was from
 */
public sealed interface BlockEventSource permits BlockEventSource.Instance, BlockEventSource.Player {

    record Player(
        @NotNull net.minestom.server.entity.Player player,
        @Nullable Point cursorPosition,
        @Nullable PlayerHand hand
    ) implements BlockEventSource {}

    record Instance(
        @NotNull net.minestom.server.instance.Instance instance
    ) implements BlockEventSource {}

}