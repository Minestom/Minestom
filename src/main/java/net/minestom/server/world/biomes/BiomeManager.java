package net.minestom.server.world.biomes;

import net.minestom.server.network.packet.server.CachedPacket;
import net.minestom.server.network.packet.server.SendablePacket;
import net.minestom.server.network.packet.server.configuration.RegistryDataPacket;
import net.minestom.server.utils.NamespaceID;
import net.minestom.server.utils.validate.Check;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 * <p>
 */
public final class BiomeManager {
    private final CachedPacket registryDataPacket = new CachedPacket(this::createRegistryDataPacket);

    private final List<Biome> biomes = new CopyOnWriteArrayList<>();
    private final Map<NamespaceID, Biome> biomesByName = new ConcurrentHashMap<>();
    private final Map<NamespaceID, Integer> idMappings = new ConcurrentHashMap<>();

    private final AtomicInteger ID_COUNTER = new AtomicInteger(0);

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

        var id = this.biomes.size();
        this.biomes.add(biome);
        this.biomesByName.put(biome.namespace(), biome);
        this.idMappings.put(biome.namespace(), id);
        registryDataPacket.invalidate();
    }

    //todo supporting remove is probably challenging at best since we no longer send ids explicitly, so you cannot skip an ID.
//    /**
//     * Removes a biome. This does NOT send the new list to players.
//     *
//     * @param biome the biome to remove
//     */
//    public void removeBiome(@NotNull Biome biome) {
//        var id = idMappings.get(biome.namespace());
//        if (id != null) {
//            biomes.remove(id.intValue());
//            biomesByName.remove(biome.namespace());
//            idMappings.remove(biome.namespace());
//            registryDataPacket.invalidate();
//        }
//    }

    /**
     * Returns an immutable copy of the biomes already registered.
     *
     * @return an immutable copy of the biomes already registered
     */
    public Collection<Biome> unmodifiableCollection() {
        return Collections.unmodifiableCollection(biomes);
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

    /**
     * Gets the id of a biome.
     *`
     * @param biome
     * @return the id of the biome, or -1 if the biome is not registered
     */
    public int getId(Biome biome) {
        return idMappings.getOrDefault(biome.namespace(), -1);
    }
//
//    public @NotNull NBTCompound toNBT() {
//        if (nbtCache != null) return nbtCache;
//        nbtCache = NBT.Compound(Map.of(
//                "type", NBT.String("minecraft:worldgen/biome"),
//                "value", NBT.List(NBTType.TAG_Compound, biomes.values().stream().map(biome -> {
//                    return NBT.Compound(Map.of(
//                            "id", NBT.Int(getId(biome)),
//                            "name", NBT.String(biome.namespace().toString()),
//                            "element", biome.toNbt()
//                    ));
//                }).toList())));
//
//        return nbtCache;
//    }

    public @NotNull SendablePacket registryDataPacket() {
        return registryDataPacket;
    }

    private @NotNull RegistryDataPacket createRegistryDataPacket() {
        return new RegistryDataPacket(
                "minecraft:worldgen/biome",
                biomes.stream()
                        .map(Biome::toRegistryEntry)
                        .toList()
        );
    }
}
