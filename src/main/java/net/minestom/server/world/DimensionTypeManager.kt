package net.minestom.server.world

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
import java.util.concurrent.CopyOnWriteArrayList

/**
 * Allows servers to register custom dimensions. Also used during player login to send the list of all existing dimensions.
 *
 *
 * Contains [DimensionType.OVERWORLD] by default but can be removed.
 */
class DimensionTypeManager {
    private val dimensionTypes: MutableList<DimensionType?> = CopyOnWriteArrayList()

    init {
        addDimension(DimensionType.Companion.OVERWORLD)
    }

    /**
     * Adds a new dimension type. This does NOT send the new list to players.
     *
     * @param dimensionType the dimension to add
     */
    fun addDimension(dimensionType: DimensionType) {
        dimensionType.registered = true
        dimensionTypes.add(dimensionType)
    }

    /**
     * Removes a dimension type. This does NOT send the new list to players.
     *
     * @param dimensionType the dimension to remove
     * @return if the dimension type was removed, false if it was not present before
     */
    fun removeDimension(dimensionType: DimensionType): Boolean {
        dimensionType.registered = false
        return dimensionTypes.remove(dimensionType)
    }

    /**
     * @param namespaceID The dimension name
     * @return true if the dimension is registered
     */
    fun isRegistered(namespaceID: NamespaceID): Boolean {
        return isRegistered(getDimension(namespaceID))
    }

    /**
     * @param dimensionType dimension to check if is registered
     * @return true if the dimension is registered
     */
    fun isRegistered(dimensionType: DimensionType?): Boolean {
        return dimensionType != null && dimensionTypes.contains(dimensionType) && dimensionType.isRegistered
    }

    /**
     * Return to a @[DimensionType] only if present and registered
     *
     * @param namespaceID The Dimension Name
     * @return a DimensionType if it is present and registered
     */
    fun getDimension(namespaceID: NamespaceID): DimensionType? {
        return unmodifiableList().stream()
            .filter { dimensionType: DimensionType? -> dimensionType.getName() == namespaceID }
            .filter { obj: DimensionType? -> obj!!.isRegistered }.findFirst().orElse(null)
    }

    /**
     * Returns an immutable copy of the dimension types already registered.
     *
     * @return an unmodifiable [List] containing all the added dimensions
     */
    fun unmodifiableList(): List<DimensionType?> {
        return Collections.unmodifiableList(dimensionTypes)
    }

    /**
     * Creates the [NBTCompound] containing all the registered dimensions.
     *
     *
     * Used when a player connects.
     *
     * @return an nbt compound containing the registered dimensions
     */
    fun toNBT(): NBTCompound {
        return NBT.Compound { dimensions: MutableNBTCompound ->
            dimensions.setString("type", "minecraft:dimension_type")
            dimensions["value"] = NBT.List(
                NBTType.TAG_Compound,
                dimensionTypes.stream()
                    .map { obj: DimensionType? -> obj!!.toIndexedNBT() }
                    .toList()
            )
        }
    }
}