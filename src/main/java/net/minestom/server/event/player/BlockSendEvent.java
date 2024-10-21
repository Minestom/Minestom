package net.minestom.server.event.player;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * Event raised before an individual block is sent to the client. Cancelling this event will cause the client to not
 * receive the update (see {@link net.minestom.server.network.packet.server.play.BlockChangePacket}), though the block
 * will still be changed server-side.
 * <p>
 * Note that blocks that are changed directly through
 * {@link net.minestom.server.instance.Chunk#setBlock(int, int, int, Block)} or similar will not trigger this event, as
 * they are not transmitted to clients until the chunk is loaded or resent (see: {@link PlayerChunkLoadEvent}).
 */
public class BlockSendEvent implements PlayerInstanceEvent, BlockEvent, CancellableEvent {
    private final Player player;
    private final Block block;
    private final BlockVec blockPosition;

    private boolean cancelled;

    public BlockSendEvent(@NotNull Player player, @NotNull Block block, @NotNull Point position) {
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
