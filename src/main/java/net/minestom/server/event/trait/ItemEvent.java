package net.minestom.server.event.trait;

import net.minestom.server.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface ItemEvent extends EventTrait {
    @NotNull ItemStack getItemStack();
}
