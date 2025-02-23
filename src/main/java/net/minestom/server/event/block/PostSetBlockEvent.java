package net.minestom.server.event.block;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

@SuppressWarnings("unused")
public final class PostSetBlockEvent implements Event, BlockEvent, CancellableEvent {

    private boolean cancelled = false;

    private final Block newBlock;
    private final Block previousBlock;
    private final Instance instance;
    private final BlockFace blockFace;
    private final BlockVec position;
    private final BlockEventSource source;

    public PostSetBlockEvent(
        @NotNull Block newBlock,
        @NotNull Block previousBlock,
        @NotNull Instance instance,
        @NotNull BlockFace face,
        @NotNull BlockVec position,
        @NotNull BlockEventSource source
    ) {
        this.newBlock = newBlock;
        this.previousBlock = previousBlock;
        this.instance = instance;
        this.blockFace = face;
        this.position = position;
        this.source = source;
    }

    @Override
    public @NotNull Block getBlock() {
        return newBlock;
    }

    public @NotNull Block getPreviousBlock() {
        return previousBlock;
    }

    public Instance getInstance() {
        return instance;
    }

    public BlockFace getBlockFace() {
        return blockFace;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return position;
    }

    public BlockEventSource getSource() {
        return source;
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
