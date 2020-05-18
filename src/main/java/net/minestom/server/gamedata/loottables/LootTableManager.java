package net.minestom.server.gamedata.loottables;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minestom.server.gamedata.Condition;
import net.minestom.server.registry.ResourceGatherer;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.NamespaceIDHashMap;

import java.io.*;

/**
 * Handles loading and configuration of loot tables
 */
public class LootTableManager {

    private NamespaceIDHashMap<Condition> conditions = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTableType> tableTypes = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTableEntryType> entryTypes = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTableFunction> functions = new NamespaceIDHashMap<>();
    private NamespaceIDHashMap<LootTable> cache = new NamespaceIDHashMap<>();
    private static Gson gson;

    static {
        gson = new GsonBuilder()
                .registerTypeAdapter(RangeContainer.class, new RangeContainer.Deserializer())
                .create();
    }

    /**
     * Registers a condition to the given namespaceID
     * @param namespaceID
     * @param condition
     */
    public void registerCondition(NamespaceID namespaceID, Condition condition) {
        conditions.put(namespaceID, condition);
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

    public Condition getCondition(NamespaceID id) {
        return conditions.get(id);
    }

    public LootTableType getTableType(NamespaceID id) {
        return tableTypes.get(id);
    }

    public LootTableEntryType getEntryType(NamespaceID id) {
        return entryTypes.get(id);
    }

    public LootTableFunction getFunction(NamespaceID id) {
        return functions.get(id);
    }
}
