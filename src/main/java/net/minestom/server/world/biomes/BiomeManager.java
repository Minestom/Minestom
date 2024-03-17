package net.minestom.server.world.biomes;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 * <p>
 */
public final class BiomeManager {
    private final Map<Integer, Biome> biomes = new ConcurrentHashMap<>();
    private final Map<NamespaceID, Biome> biomesByName = new ConcurrentHashMap<>();
    private final Map<NamespaceID, Integer> idMappings = new ConcurrentHashMap<>();

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);
    private CompoundBinaryTag nbtCache = null;

    public BiomeManager() {
        // Need to register plains for the client to work properly
        // Plains is always ID 0
        addBiome(BiomeImpl.get("minecraft:plains"));
    }

    public void loadVanillaBiomes() {
        for (BiomeImpl biome : BiomeImpl.values()) {
            if (getByName(biome.namespace()) == null)
                addBiome(biome);
        }
    }

    /**
     * Adds a new biome. This does NOT send the new list to players.
     *
     * @param biome the biome to add
     */
    public void addBiome(@NotNull Biome biome) {
        Check.stateCondition(getByName(biome.namespace()) != null, "The biome " + biome.namespace() + " has already been registered");

        var id = ID_COUNTER.getAndIncrement();
        this.biomes.put(id, biome);
        this.biomesByName.put(biome.namespace(), biome);
        this.idMappings.put(biome.namespace(), id);
        nbtCache = null;
    }

    /**
     * Removes a biome. This does NOT send the new list to players.
     *
     * @param biome the biome to remove
     */
    public void removeBiome(@NotNull Biome biome) {
        var id = idMappings.get(biome.namespace());
        if (id != null) {
            biomes.remove(id);
            biomesByName.remove(biome.namespace());
            idMappings.remove(biome.namespace());
            nbtCache = null;
        }
    }

    /**
     * Returns an immutable copy of the biomes already registered.
     *
     * @return an immutable copy of the biomes already registered
     */
    public Collection<Biome> unmodifiableCollection() {
        return Collections.unmodifiableCollection(biomes.values());
    }

    /**
     * Gets a biome by its id.
     *
     * @param id the id of the biome
     * @return the {@link Biome} linked to this id
     */
    @Nullable
    public Biome getById(int id) {
        return biomes.get(id);
    }

    @Nullable
    public Biome getByName(@NotNull NamespaceID namespaceID) {
        return biomesByName.get(namespaceID);
    }

    @Nullable
    public Biome getByName(@NotNull String namespaceID) {
        NamespaceID namespace = NamespaceID.from(namespaceID);
        return getByName(namespace);
    }

    public @NotNull CompoundBinaryTag toNBT() {
        if (nbtCache != null) return nbtCache;

        ListBinaryTag.Builder<CompoundBinaryTag> entries = ListBinaryTag.builder(BinaryTagTypes.COMPOUND);
        for (Biome biome : biomes.values()) {
            entries.add(CompoundBinaryTag.builder()
                    .putInt("id", getId(biome))
                    .putString("name", biome.namespace().toString())
                    .put("element", biome.toNbt())
                    .build());
        }
        nbtCache = CompoundBinaryTag.builder()
                .putString("type", "minecraft:worldgen/biome")
                .put("value", entries.build())
                .build();
        return nbtCache;
    }

    /**
     * Gets the id of a biome.
     *`
     * @param biome
     * @return the id of the biome, or -1 if the biome is not registered
     */
    public int getId(Biome biome) {
        return idMappings.getOrDefault(biome.namespace(), -1);
    }
}
