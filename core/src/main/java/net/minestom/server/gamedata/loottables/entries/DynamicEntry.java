package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.data.Data;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.item.ItemStack;

import java.util.Collections;
import java.util.List;

public class DynamicEntry extends LootTable.Entry {

    public static final String DROP_LIST_KEY = "minestom:loot_table_drop_list";

    private final DynamicEntry.Type entryType;

    public DynamicEntry(DynamicType type, DynamicEntry.Type entryType, int weight, int quality, List<Condition> conditions) {
        super(type, weight, quality, conditions);
        this.entryType = entryType;
    }

    @Override
    public void generate(List<ItemStack> output, Data arguments) {
        List<ItemStack> toDrop = arguments.getOrDefault(DROP_LIST_KEY, Collections.emptyList());
        output.addAll(toDrop);
    }

    public DynamicEntry.Type getEntryType() {
        return entryType;
    }

    public enum Type {
        SELF,
        CONTENTS
    }
}
