package net.minestom.server.world.biomes;

import net.minestom.server.registry.Registry;
import net.minestom.server.utils.NamespaceID;
import org.jglrxavpok.hephaistos.nbt.NBT;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTType;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 * <p>
 * Contains {@link Biome#PLAINS} by default but can be removed.
 */
public final class BiomeManager {
    private final Map<Integer, Biome> biomes = new ConcurrentHashMap<>();

    // https://minecraft.fandom.com/wiki/Rain
    private final static Double SNOW_TEMPERATURE = 0.15;
    private static final boolean loadBiomes = Boolean.getBoolean("minestom.load-vanilla-biomes");

    public BiomeManager() {
        addBiome(Biome.PLAINS);

        if (loadBiomes) {
            Registry.createContainer(Registry.Resource.BIOMES,
                (namespace, properties) -> {
                    NamespaceID namespaceID = NamespaceID.from(namespace);
                    var builder = Biome.builder().name(namespaceID);

                    BiomeEffects.Builder effectsBuilder = BiomeEffects.builder();
                    if (properties.containsKey("foliageColor")) effectsBuilder.foliageColor(properties.getInt("foliageColor"));
                    if (properties.containsKey("grassColor")) effectsBuilder.grassColor(properties.getInt("grassColor"));
                    if (properties.containsKey("skyColor")) effectsBuilder.skyColor(properties.getInt("skyColor"));
                    if (properties.containsKey("waterColor")) effectsBuilder.waterColor(properties.getInt("waterColor"));
                    if (properties.containsKey("waterFogColor")) effectsBuilder.waterFogColor(properties.getInt("waterFogColor"));
                    if (properties.containsKey("fogColor")) effectsBuilder.fogColor(properties.getInt("fogColor"));
                    builder.effects(effectsBuilder.build());

                    double temperature = properties.getDouble("temperature", 0.5F);
                    double downfall = properties.getDouble("downfall", 0.5F);
                    boolean hasPrecipitation = properties.getBoolean("has_precipitation", true);

                    builder.temperature((float) temperature)
                            .downfall((float) downfall);

                    Biome.Precipitation precipitationType = hasPrecipitation
                            ? temperature < SNOW_TEMPERATURE
                                ? Biome.Precipitation.SNOW
                                : Biome.Precipitation.RAIN
                            : Biome.Precipitation.NONE;
                    builder.precipitation(precipitationType);

                    return builder.build();
                }).values().forEach((biome -> {
                    if (biome.name().equals("minecraft:plains")) return;
                    addBiome(biome);
                }));
        }
    }

    /**
     * Adds a new biome. This does NOT send the new list to players.
     *
     * @param biome the biome to add
     */
    public void addBiome(Biome biome) {
        this.biomes.put(biome.id(), biome);
        System.out.println("Added biome " + biome.name() + " with id " + biome.id());
    }

    /**
     * Removes a biome. This does NOT send the new list to players.
     *
     * @param biome the biome to remove
     */
    public void removeBiome(Biome biome) {
        this.biomes.remove(biome.id());
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
    public Biome getById(int id) {
        return biomes.get(id);
    }

    public Biome getByName(NamespaceID namespaceID) {
        Biome biome = null;
        for (final Biome biomeT : biomes.values()) {
            if (biomeT.namespace().equals(namespaceID)) {
                biome = biomeT;
                break;
            }
        }
        return biome;
    }

    public NBTCompound toNBT() {
        return NBT.Compound(Map.of(
                "type", NBT.String("minecraft:worldgen/biome"),
                "value", NBT.List(NBTType.TAG_Compound, biomes.values().stream().map(Biome::toNbt).toList())));
    }
}
