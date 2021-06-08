package net.minestom.server.event.trait;

import net.minestom.server.event.Event;
import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * Represents any event called about an {@link ItemStack}.
 */
public interface ItemEvent extends Event {
    @NotNull ItemStack getItemStack();
}
