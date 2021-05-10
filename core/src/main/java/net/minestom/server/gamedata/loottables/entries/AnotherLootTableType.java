package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.utils.NamespaceID;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * Allows to sample from a different loot table
 *
 * minecraft:loot_table
 */
public class AnotherLootTableType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        try {
            return new AnotherLootTableEntry(this, lootTableManager.load(NamespaceID.from(name)), weight, quality, conditions);
        } catch (FileNotFoundException e) {
            throw new IllegalArgumentException(name+" is not a valid loot table name", e);
        }
    }
}
