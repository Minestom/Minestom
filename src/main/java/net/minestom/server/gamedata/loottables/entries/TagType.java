package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.utils.NamespaceID;

import java.io.FileNotFoundException;
import java.util.List;

/**
 * minecraft:tag
 */
public class TagType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        try {
            return new TagEntry(this, MinecraftServer.getTagManager().load(NamespaceID.from(name), "items"), expand, weight, quality, conditions);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }
}
