package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class TagEntry extends LootTable.Entry {
    // TODO: replace with Tag reference
    private final String name;
    private final boolean expand;

    TagEntry(TagType type, String name, boolean expand, int weight, int quality, List<Condition> conditions) {
        super(type, weight, quality, conditions);
        this.name = name;
        this.expand = expand;
    }

    @Override
    public void generateStacks(List<ItemStack> output, Data arguments) {
        // TODO: load tags
        if(expand) {
            // TODO: choose a single random item from the tag
        } else {
            // TODO: add all items from the tag
        }
    }
}
