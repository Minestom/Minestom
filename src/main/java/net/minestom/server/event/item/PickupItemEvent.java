package net.minestom.server.event.item;

import net.minestom.server.event.CancellableEvent;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class PickupItemEvent extends CancellableEvent {

    private final ItemStack itemStack;

    public PickupItemEvent(@NotNull ItemStack itemStack) {
        this.itemStack = itemStack;
    }

    @NotNull
    public ItemStack getItemStack() {
        return itemStack;
    }
}
