package net.minestom.server.world.biomes

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
import java.util.Locale
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
import java.util.concurrent.CopyOnWriteArrayList

class BiomeParticle {
    fun toNbt(): NBTCompound {
        return NBT.Compound(
            java.util.Map.of<String, NBT>(
                "probability", NBT.Float(probability),
                "options", option.toNbt()
            )
        )
    }

    interface Option {
        fun toNbt(): NBTCompound
    }

    inner class BlockOption : Option {
        override fun toNbt(): NBTCompound {
            return NBT.Compound { nbtCompound: MutableNBTCompound ->
                nbtCompound.setString("type", Companion.type)
                nbtCompound.setString("Name", block.name())
                val propertiesMap: Map<String, String> = block.properties()
                if (propertiesMap.size != 0) {
                    nbtCompound["Properties"] = NBT.Compound { p: MutableNBTCompound ->
                        propertiesMap.forEach { (key: String?, value: String?) ->
                            p.setString(
                                key, value
                            )
                        }
                    }
                }
            }
        }

        companion object {
            //TODO also can be falling_dust
            private const val type = "block"
        }
    }

    inner class DustOption : Option {
        override fun toNbt(): NBTCompound {
            return NBT.Compound(
                java.util.Map.of(
                    "type", NBT.String(Companion.type),
                    "r", NBT.Float(red),
                    "g", NBT.Float(green),
                    "b", NBT.Float(blue),
                    "scale", NBT.Float(scale)
                )
            )
        }

        companion object {
            private const val type = "dust"
        }
    }

    inner class ItemOption : Option {
        override fun toNbt(): NBTCompound {
            //todo test count might be wrong type
            val nbtCompound: NBTCompound = item.getMeta().toNBT()
            return nbtCompound.modify { n: MutableNBTCompound -> n.setString("type", Companion.type) }
        }

        companion object {
            private const val type = "item"
        }
    }

    inner class NormalOption : Option {
        override fun toNbt(): NBTCompound {
            return NBT.Compound(java.util.Map.of("type", NBT.String(type.toString())))
        }
    }
}