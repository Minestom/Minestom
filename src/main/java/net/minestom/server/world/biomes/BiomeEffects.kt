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
import java.util.Map
import java.util.concurrent.CopyOnWriteArrayList

class BiomeEffects {
    fun toNbt(): NBTCompound {
        return NBT.Compound { nbt: MutableNBTCompound ->
            nbt.setInt("fog_color", fogColor)
            if (foliageColor != -1) nbt.setInt("foliage_color", foliageColor)
            if (grassColor != -1) nbt.setInt("grass_color", grassColor)
            nbt.setInt("sky_color", skyColor)
            nbt.setInt("water_color", waterColor)
            nbt.setInt("water_fog_color", waterFogColor)
            if (grassColorModifier != null) nbt.setString(
                "grass_color_modifier",
                grassColorModifier.name.toLowerCase(Locale.ROOT)
            )
            if (biomeParticle != null) nbt["particle"] = biomeParticle.toNbt()
            if (ambientSound != null) nbt.setString("ambient_sound", ambientSound.toString())
            if (moodSound != null) nbt["mood_sound"] = moodSound.toNbt()
            if (additionsSound != null) nbt["additions_sound"] = additionsSound.toNbt()
            if (music != null) nbt["music"] = music.toNbt()
        }
    }

    enum class GrassColorModifier {
        NONE, DARK_FOREST, SWAMP
    }

    inner class MoodSound {
        fun toNbt(): NBTCompound {
            return NBT.Compound(
                Map.of(
                    "sound", NBT.String(sound.toString()),
                    "tick_delay", NBT.Int(tickDelay),
                    "block_search_extent", NBT.Int(blockSearchExtent),
                    "offset", NBT.Double(offset)
                )
            )
        }
    }

    inner class AdditionsSound {
        fun toNbt(): NBTCompound {
            return NBT.Compound(
                Map.of(
                    "sound", NBT.String(sound.toString()),
                    "tick_chance", NBT.Double(tickChance)
                )
            )
        }
    }

    inner class Music {
        fun toNbt(): NBTCompound {
            return NBT.Compound(
                Map.of(
                    "sound", NBT.String(sound.toString()),
                    "min_delay", NBT.Int(minDelay),
                    "max_delay", NBT.Int(maxDelay),
                    "replace_current_music", NBT.Boolean(replaceCurrentMusic)
                )
            )
        }
    }

    class Builder internal constructor() {
        private var fogColor = 0
        private var skyColor = 0
        private var waterColor = 0
        private var waterFogColor = 0
        private var foliageColor = -1
        private var grassColor = -1
        private var grassColorModifier: GrassColorModifier? = null
        private var biomeParticle: BiomeParticle? = null
        private var ambientSound: NamespaceID? = null
        private var moodSound: MoodSound? = null
        private var additionsSound: AdditionsSound? = null
        private var music: Music? = null
        fun fogColor(fogColor: Int): Builder {
            this.fogColor = fogColor
            return this
        }

        fun skyColor(skyColor: Int): Builder {
            this.skyColor = skyColor
            return this
        }

        fun waterColor(waterColor: Int): Builder {
            this.waterColor = waterColor
            return this
        }

        fun waterFogColor(waterFogColor: Int): Builder {
            this.waterFogColor = waterFogColor
            return this
        }

        fun foliageColor(foliageColor: Int): Builder {
            this.foliageColor = foliageColor
            return this
        }

        fun grassColor(grassColor: Int): Builder {
            this.grassColor = grassColor
            return this
        }

        fun grassColorModifier(grassColorModifier: GrassColorModifier?): Builder {
            this.grassColorModifier = grassColorModifier
            return this
        }

        fun biomeParticle(biomeParticle: BiomeParticle?): Builder {
            this.biomeParticle = biomeParticle
            return this
        }

        fun ambientSound(ambientSound: NamespaceID?): Builder {
            this.ambientSound = ambientSound
            return this
        }

        fun moodSound(moodSound: MoodSound?): Builder {
            this.moodSound = moodSound
            return this
        }

        fun additionsSound(additionsSound: AdditionsSound?): Builder {
            this.additionsSound = additionsSound
            return this
        }

        fun music(music: Music?): Builder {
            this.music = music
            return this
        }

        fun build(): BiomeEffects {
            return BiomeEffects(
                fogColor, skyColor, waterColor, waterFogColor, foliageColor,
                grassColor, grassColorModifier, biomeParticle,
                ambientSound, moodSound, additionsSound, music
            )
        }
    }

    companion object {
        fun builder(): Builder {
            return Builder()
        }
    }
}