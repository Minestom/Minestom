package net.minestom.server.gamedata.loottables;

import net.minestom.server.gamedata.Condition;

import java.util.List;

@FunctionalInterface
public interface LootTableEntryType {
    LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality);
}
