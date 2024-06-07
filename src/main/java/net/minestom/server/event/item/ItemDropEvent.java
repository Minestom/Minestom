package net.minestom.server.event.item;

import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.ItemEvent;
import net.minestom.server.event.trait.PlayerInstanceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.drop.DropReason;
import org.jetbrains.annotations.NotNull;

public class ItemDropEvent implements PlayerInstanceEvent, ItemEvent, CancellableEvent {

    private final Player player;
    private final ItemStack itemStack;
    private final DropReason dropReason;
    private final DropAmount dropAmount;

    private boolean cancelled;

    public ItemDropEvent(@NotNull Player player, @NotNull ItemStack itemStack, @NotNull DropReason reason, @NotNull DropAmount amount) {
        this.player = player;
        this.itemStack = itemStack;
        this.dropReason = reason;
        this.dropAmount = amount;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
    }

    public DropReason getDropReason() {
        return dropReason;
    }

    public DropAmount getDropAmount() {
        return dropAmount;
    }

    public enum DropAmount {
        SINGLE,
        STACK
    }

}
