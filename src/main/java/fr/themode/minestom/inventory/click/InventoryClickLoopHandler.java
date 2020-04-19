package fr.themode.minestom.inventory.click;

import fr.themode.minestom.item.ItemStack;

import java.util.function.BiConsumer;
import java.util.function.Function;

public class InventoryClickLoopHandler {

    private int start;
    private int end;
    private int step;
    private Function<Integer, Integer> indexModifier;
    private Function<Integer, ItemStack> itemGetter;
    private BiConsumer<Integer, ItemStack> itemSetter;

    public InventoryClickLoopHandler(int start, int end, int step,
                                     Function<Integer, Integer> indexModifier,
                                     Function<Integer, ItemStack> itemGetter,
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

    public Function<Integer, Integer> getIndexModifier() {
        return indexModifier;
    }

    public Function<Integer, ItemStack> getItemGetter() {
        return itemGetter;
    }

    public BiConsumer<Integer, ItemStack> getItemSetter() {
        return itemSetter;
    }
}
