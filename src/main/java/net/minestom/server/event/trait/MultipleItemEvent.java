package net.minestom.server.event.trait;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface MultipleItemEvent extends ItemEvent {

    @NotNull ItemStack[] getItemStacks();

    /**
     * @return returns the first itemstack from the itemstack array
     * @deprecated please use provided getters from the actual event
     */
    @Override
    @Deprecated
    default @NotNull ItemStack getItemStack() {
        return getItemStacks()[0];
    }

}
