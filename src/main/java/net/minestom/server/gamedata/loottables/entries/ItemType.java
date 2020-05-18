package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NamespaceID;

import java.util.List;

public class ItemType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        NamespaceID itemID = NamespaceID.from(name);
        // TODO: handle non-vanilla IDs ?
        return new ItemEntry(this, Material.valueOf(itemID.getPath().toUpperCase()), weight, quality, functions, conditions);
    }
}
