package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * Event raised before an individual block is sent to the client. Cancelling this event will cause the client to not
 * receive the update (see {@link net.minestom.server.network.packet.server.play.BlockChangePacket}), though the block
 * will still be changed server-side.
 */
public class PlayerBlockUpdateEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {
    private final Player player;
    private final BlockVec blockPosition;

    private Block block;
    private boolean cancelled;

    public PlayerBlockUpdateEvent(@NotNull Player player, @NotNull Block block, @NotNull Point position) {
        this.player = player;
        this.block = block;
        this.blockPosition = new BlockVec(position);
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @Override
    public @NotNull Block getBlock() {
        return block;
    }

    /**
     * Sets the block that will be seen by this client.
     *
     * @param block the block that will be seen by this client
     */
    public void setBlock(@NotNull Block block) {
        this.block = Objects.requireNonNull(block);
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return blockPosition;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }
}
