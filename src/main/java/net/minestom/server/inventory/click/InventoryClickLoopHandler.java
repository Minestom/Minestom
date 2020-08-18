package net.minestom.server.inventory.click;

import it.unimi.dsi.fastutil.ints.Int2IntFunction;
import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import net.minestom.server.item.ItemStack;

import java.util.function.BiConsumer;

public class InventoryClickLoopHandler {

    private final int start;
    private final int end;
    private final int step;
    private final Int2IntFunction indexModifier;
    private final Int2ObjectFunction<ItemStack> itemGetter;
    private final BiConsumer<Integer, ItemStack> itemSetter;

    public InventoryClickLoopHandler(int start, int end, int step,
                                     Int2IntFunction indexModifier,
                                     Int2ObjectFunction<ItemStack> itemGetter,
                                     BiConsumer<Integer, ItemStack> itemSetter) {
        this.start = start;
        this.end = end;
        this.step = step;
        this.indexModifier = indexModifier;
        this.itemGetter = itemGetter;
        this.itemSetter = itemSetter;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    public int getStep() {
        return step;
    }

    public Int2IntFunction getIndexModifier() {
        return indexModifier;
    }

    public Int2ObjectFunction<ItemStack> getItemGetter() {
        return itemGetter;
    }

    public BiConsumer<Integer, ItemStack> getItemSetter() {
        return itemSetter;
    }
}
