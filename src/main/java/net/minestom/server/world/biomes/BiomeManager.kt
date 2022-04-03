package net.minestom.server.world.biomes

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setString
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setInt
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.set
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setFloat
import org.jglrxavpok.hephaistos.nbt.NBTCompound.modify
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setByte
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound.setLong
import org.jglrxavpok.hephaistos.mcdata.VanillaMinY
import org.jglrxavpok.hephaistos.mcdata.VanillaMaxY
import net.minestom.server.utils.NamespaceID
import net.minestom.server.world.biomes.BiomeEffects
import net.minestom.server.world.biomes.Biome.Precipitation
import net.minestom.server.world.biomes.Biome.TemperatureModifier
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.CompoundBuilder
import org.jglrxavpok.hephaistos.nbt.mutable.MutableNBTCompound
import java.util.concurrent.atomic.AtomicInteger
import net.minestom.server.world.biomes.BiomeEffects.GrassColorModifier
import net.minestom.server.world.biomes.BiomeParticle
import net.minestom.server.world.biomes.BiomeEffects.MoodSound
import net.minestom.server.world.biomes.BiomeEffects.AdditionsSound
import net.minestom.server.world.biomes.BiomeEffects.Music
import org.jglrxavpok.hephaistos.nbt.NBTType
import net.minestom.server.world.biomes.BiomeParticle.BlockOption
import java.util.function.BiConsumer
import net.minestom.server.world.biomes.BiomeParticle.DustOption
import net.minestom.server.world.biomes.BiomeParticle.ItemOption
import org.jglrxavpok.hephaistos.nbt.NBTString
import net.minestom.server.world.DimensionType
import net.minestom.server.world.DimensionType.DimensionTypeBuilder
import java.util.*
import java.util.Map
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Allows servers to register custom dimensions. Also used during player joining to send the list of all existing dimensions.
 *
 *
 * Contains [Biome.PLAINS] by default but can be removed.
 */
class BiomeManager {
    private val biomes: Int2ObjectMap<Biome> = Int2ObjectOpenHashMap()

    init {
        addBiome(Biome.Companion.PLAINS)
    }

    /**
     * Adds a new biome. This does NOT send the new list to players.
     *
     * @param biome the biome to add
     */
    @Synchronized
    fun addBiome(biome: Biome) {
        biomes[biome.id()] = biome
    }

    /**
     * Removes a biome. This does NOT send the new list to players.
     *
     * @param biome the biome to remove
     */
    @Synchronized
    fun removeBiome(biome: Biome) {
        biomes.remove(biome.id())
    }

    /**
     * Returns an immutable copy of the biomes already registered.
     *
     * @return an immutable copy of the biomes already registered
     */
    @Synchronized
    fun unmodifiableCollection(): Collection<Biome> {
        return Collections.unmodifiableCollection(biomes.values)
    }

    /**
     * Gets a biome by its id.
     *
     * @param id the id of the biome
     * @return the [Biome] linked to this id
     */
    @Synchronized
    fun getById(id: Int): Biome {
        return biomes[id]
    }

    @Synchronized
    fun getByName(namespaceID: NamespaceID): Biome? {
        var biome: Biome? = null
        for (biomeT in biomes.values) {
            if (biomeT.name() == namespaceID) {
                biome = biomeT
                break
            }
        }
        return biome
    }

    @Synchronized
    fun toNBT(): NBTCompound {
        return NBT.Compound(
            Map.of(
                "type", NBT.String("minecraft:worldgen/biome"),
                "value", NBT.List(NBTType.TAG_Compound, biomes.values.stream().map { obj: Biome -> obj.toNbt() }
                    .toList())))
    }
}