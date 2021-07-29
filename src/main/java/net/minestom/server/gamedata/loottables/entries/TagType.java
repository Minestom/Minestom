package net.minestom.server.gamedata.loottables.entries;

import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.gamedata.tags.Tag;

import java.util.List;

/**
 * minecraft:tag
 */
public class TagType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        return new TagEntry(this, MinecraftServer.getTagManager().getTag(Tag.BasicType.ITEMS, name), expand, weight, quality, conditions);
    }
}
