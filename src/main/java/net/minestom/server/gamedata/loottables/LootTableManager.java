package net.minestom.server.gamedata.loottables;

import com.google.gson.*;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.NamespaceIDHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.function.Function;

/**
 * Handles loading and configuration of loot tables
 */
public final class LootTableManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(LootTableManager.class);
    private NamespaceIDHashMap<JsonDeserializer<? extends Condition>> conditionDeserializers = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTableType> tableTypes = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTableEntryType> entryTypes = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTableFunction> functions = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTable> cache = new NamespaceIDHashMap<>();
    private Gson gson;

    public LootTableManager() {
        gson = new GsonBuilder()
                .registerTypeAdapter(RangeContainer.class, new RangeContainer.Deserializer())
                .registerTypeAdapter(ConditionContainer.class, new ConditionContainer.Deserializer(this))
                .create();
        // TODO:
        // loadLoottablesFileSystem();
    }

    private void loadLoottablesFileSystem() {
        // Block loot
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/loot_tables/block_loot_tables.json");
            if (a != null) {
                JsonArray blockLootTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement bLT : blockLootTags) {
                    JsonObject blockLootTag = bLT.getAsJsonObject();
                    NamespaceID id = NamespaceID.from(blockLootTag.get("blockId").getAsString());
                    // Add the blocks/ prefix
                    // Remove the minecraft: prefixing the blockId (so just the path)
                    String path = "blocks/" + id.getPath();
                    cache.put(NamespaceID.from("minecraft", path), gson.fromJson(blockLootTag, LootTableContainer.class).createTable(this));
                }
            } else {
                LOGGER.error("Could not find block loot tables in JAR Resources.");
            }
        }
        // Chest loot
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/loot_tables/chest_loot_tables.json");
            if (a != null) {
                JsonArray chestTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement cT : chestTags) {
                    JsonObject chestTag = cT.getAsJsonObject();
                    // Add the chests/ prefix
                    String path = "chests/" + chestTag.get("chestType").getAsString();
                    cache.put(NamespaceID.from("minecraft", path), gson.fromJson(chestTag, LootTableContainer.class).createTable(this));
                }
            } else {
                LOGGER.error("Could not find chest loot tables in JAR Resources.");
            }
        }
        // Entity loot
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/loot_tables/entity_loot_tables.json");
            if (a != null) {
                JsonArray entityTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement eT : entityTags) {
                    JsonObject entityTag = eT.getAsJsonObject();
                    // Add the chests/ prefix
                    String path = "entities/" + entityTag.get("entityId").getAsString();
                    cache.put(NamespaceID.from("minecraft", path), gson.fromJson(entityTag, LootTableContainer.class).createTable(this));
                }
            } else {
                LOGGER.error("Could not find entity loot tables in JAR Resources.");
            }
        }
        // Gameplay loot
        {
            InputStream a = getClass().getResourceAsStream("/minecraft_data/loot_tables/gameplay_loot_tables.json");
            if (a != null) {
                JsonArray gameplayTags = gson.fromJson(new BufferedReader(new InputStreamReader(a)), JsonArray.class);
                for (JsonElement gT : gameplayTags) {
                    JsonObject gameplayTag = gT.getAsJsonObject();
                    // Add the chests/ prefix
                    String path = "gameplay/" + gameplayTag.get("gameplayType").getAsString();
                    cache.put(NamespaceID.from("minecraft", path), gson.fromJson(gameplayTag, LootTableContainer.class).createTable(this));
                }
            } else {
                LOGGER.error("Could not find gameplay loot tables in JAR Resources.");
            }
        }
    }

    /**
     * Registers a condition factory to the given namespaceID
     *
     * @param namespaceID
     * @param factory
     */
    public <T extends Condition> void registerConditionDeserializer(NamespaceID namespaceID, JsonDeserializer<T> factory) {
        conditionDeserializers.put(namespaceID, factory);
    }

    /**
     * Registers a loot table type to the given namespaceID
     *
     * @param namespaceID
     * @param type
     */
    public void registerTableType(NamespaceID namespaceID, LootTableType type) {
        tableTypes.put(namespaceID, type);
    }

    /**
     * Registers a loot table entry type to the given namespaceID
     *
     * @param namespaceID
     * @param type
     */
    public void registerEntryType(NamespaceID namespaceID, LootTableEntryType type) {
        entryTypes.put(namespaceID, type);
    }

    /**
     * Registers a loot table function to the given namespaceID
     *
     * @param namespaceID
     * @param function
     */
    public void registerFunction(NamespaceID namespaceID, LootTableFunction function) {
        functions.put(namespaceID, function);
    }

    /**
     * Loads a loot table with the given name. Loot tables can be cached, so 'reader' is used only on cache misses
     *
     * @param name   the name to cache the loot table with
     * @param objectFunction the function to call to read the loot table from, if none cached.
     * @return
     */
    public LootTable load(NamespaceID name, Function<NamespaceID, JsonObject> objectFunction) {
        return cache.computeIfAbsent(name, _name -> create(objectFunction.apply(_name)));
    }

    private LootTable create(JsonObject jsonObject) {
        LootTableContainer container = gson.fromJson(jsonObject, LootTableContainer.class);
        return container.createTable(this);
    }

    /**
     * Returns the registered table type corresponding to the given namespace ID. If none is registered, throws {@link IllegalArgumentException}
     *
     * @param id
     * @return
     */
    public LootTableType getTableType(NamespaceID id) {
        if (!tableTypes.containsKey(id))
            throw new IllegalArgumentException("Unknown table type: " + id);
        return tableTypes.get(id);
    }

    /**
     * Returns the registered entry type corresponding to the given namespace ID. If none is registered, throws {@link IllegalArgumentException}
     *
     * @param id
     * @return
     */
    public LootTableEntryType getEntryType(NamespaceID id) {
        if (!entryTypes.containsKey(id))
            throw new IllegalArgumentException("Unknown entry type: " + id);
        return entryTypes.get(id);
    }

    /**
     * Returns the registered table type corresponding to the given namespace ID. If none is registered, returns {@link LootTableFunction#IDENTITY}
     *
     * @param id
     * @return
     */
    public LootTableFunction getFunction(NamespaceID id) {
        return functions.getOrDefault(id, LootTableFunction.IDENTITY);
    }

    public JsonDeserializer<? extends Condition> getConditionDeserializer(NamespaceID id) {
        return conditionDeserializers.getOrDefault(id, (json, typeOfT, context) -> Condition.ALWAYS_NO);
    }
}
