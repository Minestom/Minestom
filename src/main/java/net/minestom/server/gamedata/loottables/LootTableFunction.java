package net.minestom.server.gamedata.loottables;

import net.minestom.server.data.Data;
import net.minestom.server.item.ItemStack;

/**
 * Changes to apply to the stack being produced
 */
@FunctionalInterface
public interface LootTableFunction {

    /**
     * Applies changes to the stack being produced
     * @param stack
     * @param data arguments to pass to the function.
     * @return
     */
    ItemStack apply(ItemStack stack, Data data);

    LootTableFunction IDENTITY = (stack, _d) -> stack;
}

