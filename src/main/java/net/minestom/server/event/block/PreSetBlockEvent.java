package net.minestom.server.event.block;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("unused")
public final class PreSetBlockEvent implements Event, BlockEvent, CancellableEvent {

    private boolean cancelled = false;

    private Block newBlock;

    private boolean doBlockUpdates = true;
    private boolean doesConsumeBlock = true;

    private final Block previousBlock;
    private final Instance instance;
    private final BlockFace blockFace;
    private final BlockVec position;
    private final BlockEventSource source;

    public PreSetBlockEvent(
        @NotNull Block newBlock,
        @NotNull Block previousBlock,
        @NotNull Instance instance,
        @Nullable BlockFace blockFace,
        @NotNull BlockVec position,
        @NotNull BlockEventSource source
    ) {
        this.newBlock = newBlock;
        this.previousBlock = previousBlock;
        this.instance = instance;
        this.blockFace = blockFace;
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

    public @NotNull Instance getInstance() {
        return instance;
    }

    public @Nullable BlockFace getBlockFace() {
        return blockFace;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return position;
    }

    public @NotNull BlockEventSource getSource() {
        return source;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean doBlockUpdates() {
        return doBlockUpdates;
    }

    public boolean consumesBlock() {
        return doesConsumeBlock;
    }

    public void setDoesConsumeBlock(boolean doesConsumeBlock) {
        this.doesConsumeBlock = doesConsumeBlock;
    }

    public void setDoBlockUpdates(boolean doBlockUpdates) {
        this.doBlockUpdates = doBlockUpdates;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void setBlock(@NotNull Block block) {
        this.newBlock = block;
    }
}