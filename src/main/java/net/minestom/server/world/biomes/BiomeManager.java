package net.minestom.server.world.biomes;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;


/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 * <p>
 * Contains {@link Biome#PLAINS} by default but can be removed.
 */
public final class BiomeManager {
    private final Int2ObjectMap<Biome> biomes = new Int2ObjectOpenHashMap<>();

    public BiomeManager() {
        addBiome(Biome.PLAINS);
    }

    /**
     * Adds a new biome. This does NOT send the new list to players.
     *
     * @param biome the biome to add
     */
    public synchronized void addBiome(Biome biome) {
        this.biomes.put(biome.id(), biome);
    }

    /**
     * Removes a biome. This does NOT send the new list to players.
     *
     * @param biome the biome to remove
     */
    public synchronized void removeBiome(Biome biome) {
        this.biomes.remove(biome.id());
    }

    /**
     * Returns an immutable copy of the biomes already registered.
     *
     * @return an immutable copy of the biomes already registered
     */
    public synchronized Collection<Biome> unmodifiableCollection() {
        return Collections.unmodifiableCollection(biomes.values());
    }

    /**
     * Gets a biome by its id.
     *
     * @param id the id of the biome
     * @return the {@link Biome} linked to this id
     */
    public synchronized Biome getById(int id) {
        return biomes.get(id);
    }

    public synchronized Biome getByName(NamespaceID namespaceID) {
        Biome biome = null;
        for (final Biome biomeT : biomes.values()) {
            if (biomeT.name().equals(namespaceID)) {
                biome = biomeT;
                break;
            }
        }
        return biome;
    }

    public synchronized NBTCompound toNBT() {
        return NBT.Compound(Map.of(
                "type", NBT.String("minecraft:worldgen/biome"),
                "value", NBT.List(NBTType.TAG_Compound, biomes.values().stream().map(Biome::toNbt).toList())));
    }
}
