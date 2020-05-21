package net.minestom.server.gamedata.loottables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.NamespaceIDHashMap;

import java.io.*;

/**
 * Handles loading and configuration of loot tables
 */
public class LootTableManager {

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
    }

    /**
     * Registers a condition factory to the given namespaceID
     * @param namespaceID
     * @param factory
     */
    public <T extends Condition> void registerConditionDeserializer(NamespaceID namespaceID, JsonDeserializer<T> factory) {
        conditionDeserializers.put(namespaceID, factory);
    }

    /**
     * Registers a loot table type to the given namespaceID
     * @param namespaceID
     * @param type
     */
    public void registerTableType(NamespaceID namespaceID, LootTableType type) {
        tableTypes.put(namespaceID, type);
    }

    /**
     * Registers a loot table entry type to the given namespaceID
     * @param namespaceID
     * @param type
     */
    public void registerEntryType(NamespaceID namespaceID, LootTableEntryType type) {
        entryTypes.put(namespaceID, type);
    }

    /**
     * Registers a loot table function to the given namespaceID
     * @param namespaceID
     * @param function
     */
    public void registerFunction(NamespaceID namespaceID, LootTableFunction function) {
        functions.put(namespaceID, function);
    }

    public LootTable load(NamespaceID name) throws FileNotFoundException {
        return load(name, new FileReader(new File(ResourceGatherer.DATA_FOLDER, "data/"+name.getDomain()+"/loot_tables/"+name.getPath()+".json")));
    }

    /**
     * Loads a loot table with the given name. Loot tables can be cached, so 'reader' is used only on cache misses
     * @param name the name to cache the loot table with
     * @param reader the reader to read the loot table from, if none cached. **Will** be closed no matter the results of this call
     * @return
     */
    public LootTable load(NamespaceID name, Reader reader) {
        try {
            return cache.computeIfAbsent(name, _name -> create(reader));
        } finally {
            try {
                reader.close();
            } catch (IOException e) {}
        }
    }

    private LootTable create(Reader reader) {
        LootTableContainer container = gson.fromJson(reader, LootTableContainer.class);
        return container.createTable(this);
    }

    /**
     * Returns the registered table type corresponding to the given namespace ID. If none is registered, throws {@link IllegalArgumentException}
     * @param id
     * @return
     */
    public LootTableType getTableType(NamespaceID id) {
        if(!tableTypes.containsKey(id))
            throw new IllegalArgumentException("Unknown table type: "+id);
        return tableTypes.get(id);
    }

    /**
     * Returns the registered entry type corresponding to the given namespace ID. If none is registered, throws {@link IllegalArgumentException}
     * @param id
     * @return
     */
    public LootTableEntryType getEntryType(NamespaceID id) {
        if(!entryTypes.containsKey(id))
            throw new IllegalArgumentException("Unknown entry type: "+id);
        return entryTypes.get(id);
    }

    /**
     * Returns the registered table type corresponding to the given namespace ID. If none is registered, returns {@link LootTableFunction#IDENTITY}
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
