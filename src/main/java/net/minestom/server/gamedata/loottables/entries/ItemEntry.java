package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.LinkedList;
import java.util.List;

public class ItemEntry extends LootTable.Entry {

    private final List<LootTableFunction> functions;
    private final Material item;

    ItemEntry(ItemType type, Material baseItem, int weight, int quality, List<LootTableFunction> functions, List<Condition> conditions) {
        super(type, weight, quality, conditions);
        this.item = baseItem;
        this.functions = new LinkedList<>(functions);
    }

    @Override
    public void generate(List<ItemStack> output, Data arguments) {
        ItemStack stack = new ItemStack(item, (byte)1);
        for (LootTableFunction function : functions) {
            stack = function.apply(stack, arguments);
        }
        if(!stack.isAir()) {
            output.add(stack);
        }
    }

    public List<LootTableFunction> getFunctions() {
        return functions;
    }

    public Material getItem() {
        return item;
    }
}
