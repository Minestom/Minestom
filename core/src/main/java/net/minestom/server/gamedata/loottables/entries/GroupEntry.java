package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.item.ItemStack;

import java.util.List;

public class GroupEntry extends LootTable.Entry {
    private final List<LootTable.Entry> children;

    public GroupEntry(GroupType type, List<LootTable.Entry> children, int weight, int quality, List<Condition> conditions) {
        super(type, weight, quality, conditions);
        this.children = children;
    }

    @Override
    public void generate(List<ItemStack> output, Data arguments) {
        for (LootTable.Entry child : children) {
            child.generateStacks(output, arguments);
        }
    }
}
