package net.minestom.server.event.instance;

import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.event.Event;
import net.minestom.server.event.trait.BlockEvent;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.InstanceEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import org.jetbrains.annotations.NotNull;

public class InstanceBlockChangeEvent implements Event, BlockEvent, CancellableEvent, InstanceEvent {

    private final BlockEvent.Source source;
    private final BlockVec position;

    private Block newBlock;

    private boolean cancelled = false;
    private boolean doBlockUpdates = true;
    private boolean doesConsumeItem = true;

    public InstanceBlockChangeEvent(@NotNull BlockEvent.Source source,
                                    @NotNull Block newBlock,
                                    @NotNull BlockVec position) {
        this.source = source;
        this.newBlock = newBlock;
        this.position = position;
    }

    @Override
    public @NotNull Source getSource() {
        return source;
    }

    @Override
    public @NotNull Instance getInstance() {
        return source.instance();
    }

    @Override
    public @NotNull Block getBlock() {
        return newBlock;
    }

    @Override
    public @NotNull BlockVec getBlockPosition() {
        return position;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    public boolean fromPlayer() {
        return source instanceof Source.Player;
    }

    public boolean doBlockUpdates() {
        return doBlockUpdates;
    }

    public boolean doesConsumeItem() {
        return doesConsumeItem;
    }

    public void setBlock(Block block) {
        this.newBlock = block;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public void doBlockUpdates(boolean doBlockUpdates) {
        this.doBlockUpdates = doBlockUpdates;
    }

    public void doesConsumeItem(boolean doesConsumeItem) {
        this.doesConsumeItem = doesConsumeItem;
    }

}
