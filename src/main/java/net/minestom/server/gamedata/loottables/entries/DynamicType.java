package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;

import java.util.List;

/**
 * minecraft:dynamic
 */
public class DynamicType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        return new DynamicEntry(this, DynamicEntry.Type.valueOf(name.toUpperCase()), weight, quality, conditions);
    }
}
