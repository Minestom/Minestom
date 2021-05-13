package net.minestom.server.gamedata.loottables.entries;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.minestom.server.MinecraftServer;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.gamedata.loottables.LootTable;
import net.minestom.server.gamedata.loottables.LootTableEntryType;
import net.minestom.server.gamedata.loottables.LootTableFunction;
import net.minestom.server.gamedata.loottables.LootTableManager;
import net.minestom.server.utils.NamespaceID;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

/**
 * Allows to sample from a different loot table
 * <p>
 * minecraft:loot_table
 */
public class AnotherLootTableType implements LootTableEntryType {
    @Override
    public LootTable.Entry create(LootTableManager lootTableManager, String name, List<Condition> conditions, List<LootTable.Entry> children, boolean expand, List<LootTableFunction> functions, int weight, int quality) {
        return new AnotherLootTableEntry(this, lootTableManager.load(NamespaceID.from(name), namespaceID -> {
            try {
                return new Gson().fromJson(
                        new FileReader(new File(new File("./minecraft_data"), MinecraftServer.VERSION_NAME.replaceAll("\\.", "_") + "_gen_data/data/" + namespaceID.getDomain() + "/loot_tables/" + namespaceID.getPath() + ".json")
                        ), JsonObject.class);
            } catch (FileNotFoundException e) {
                throw new IllegalArgumentException(name + " is not a valid loot table name", e);
            }
        }), weight, quality, conditions);
    }
}
