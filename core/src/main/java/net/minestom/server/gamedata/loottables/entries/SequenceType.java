package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;

import java.util.List;

/**
 * minecraft:sequence
 */
public class SequenceType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        return new SequenceEntry(this, children, weight, quality, conditions);
    }
}
