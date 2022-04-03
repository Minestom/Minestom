package net.minestom.server.map

import net.minestom.server.MinecraftServer.Companion.exceptionManager
import net.minestom.server.map.Framebuffer
import net.minestom.server.map.framebuffers.DirectFramebuffer
import java.awt.image.BufferedImage
import java.awt.Graphics2D
import java.awt.image.DataBufferInt
import net.minestom.server.map.framebuffers.Graphics2DFramebuffer
import net.minestom.server.map.MapColors
import net.minestom.server.map.LargeFramebuffer
import net.minestom.server.map.framebuffers.LargeFramebufferDefaultView
import net.minestom.server.map.framebuffers.LargeDirectFramebuffer
import java.lang.IndexOutOfBoundsException
import net.minestom.server.map.framebuffers.LargeGraphics2DFramebuffer
import net.minestom.server.map.MapColors.Multiplier
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.map.MapColors.PreciseMapColor
import net.minestom.server.map.MapColors.ColorMappingStrategy
import java.lang.IllegalArgumentException
import java.lang.NumberFormatException
import net.minestom.server.MinecraftServer
import net.minestom.server.network.packet.server.play.MapDataPacket
import net.minestom.server.network.packet.server.play.MapDataPacket.ColorContent
import java.util.function.Function

enum class MapColors(private val red: Int, private val green: Int, private val blue: Int) {
    NONE(0, 0, 0), GRASS(127, 178, 56), SAND(247, 233, 163), WOOL(199, 199, 199), FIRE(255, 0, 0), ICE(
        160,
        160,
        255
    ),
    METAL(167, 167, 167), PLANT(0, 124, 0), SNOW(255, 255, 255), CLAY(164, 168, 184), DIRT(151, 109, 77), STONE(
        112,
        112,
        112
    ),
    WATER(64, 64, 255), WOOD(143, 119, 72), QUARTZ(255, 252, 245), COLOR_ORANGE(216, 127, 51), COLOR_MAGENTA(
        178,
        76,
        216
    ),
    COLOR_LIGHT_BLUE(102, 153, 216), COLOR_YELLOW(229, 229, 51), COLOR_LIGHT_GREEN(127, 204, 25), COLOR_PINK(
        242,
        127,
        165
    ),
    COLOR_GRAY(76, 76, 76), COLOR_LIGHT_GRAY(153, 153, 153), COLOR_CYAN(76, 127, 153), COLOR_PURPLE(
        127,
        63,
        178
    ),
    COLOR_BLUE(51, 76, 178), COLOR_BROWN(102, 76, 51), COLOR_GREEN(102, 127, 51), COLOR_RED(
        153,
        51,
        51
    ),
    COLOR_BLACK(25, 25, 25), GOLD(250, 238, 77), DIAMOND(92, 219, 213), LAPIS(74, 128, 255), EMERALD(
        0,
        217,
        58
    ),
    PODZOL(129, 86, 49), NETHER(112, 2, 0), TERRACOTTA_WHITE(209, 177, 161), TERRACOTTA_ORANGE(
        159,
        82,
        36
    ),
    TERRACOTTA_MAGENTA(149, 87, 108), TERRACOTTA_LIGHT_BLUE(112, 108, 138), TERRACOTTA_YELLOW(
        186,
        133,
        36
    ),
    TERRACOTTA_LIGHT_GREEN(103, 117, 53), TERRACOTTA_PINK(160, 77, 78), TERRACOTTA_GRAY(
        57,
        41,
        35
    ),
    TERRACOTTA_LIGHT_GRAY(135, 107, 98), TERRACOTTA_CYAN(87, 92, 92), TERRACOTTA_PURPLE(
        122,
        73,
        88
    ),
    TERRACOTTA_BLUE(76, 62, 92), TERRACOTTA_BROWN(76, 50, 35), TERRACOTTA_GREEN(76, 82, 42), TERRACOTTA_RED(
        142,
        60,
        46
    ),
    TERRACOTTA_BLACK(37, 22, 16), CRIMSON_NYLIUM(189, 48, 49), CRIMSON_STEM(148, 63, 97), CRIMSON_HYPHAE(
        92,
        25,
        29
    ),
    WARPED_NYLIUM(22, 126, 134), WARPED_STEM(58, 142, 140), WARPED_HYPHAE(86, 44, 62), WARPED_WART_BLOCK(20, 180, 133);
    // From the wiki: https://minecraft.gamepedia.com/Map_item_format
    // Map Color ID 	Multiply R,G,B By 	= Multiplier
    //Base Color ID*4 + 0 	180 	0.71
    //Base Color ID*4 + 1 	220 	0.86
    //Base Color ID*4 + 2 	255 (same color) 	1
    //Base Color ID*4 + 3 	135 	0.53
    /**
     * Returns the color index with RGB multiplied by 0.53, to use on a map
     */
    fun multiply53(): Byte {
        return ((ordinal shl 2) + 3).toByte()
    }

    /**
     * Returns the color index with RGB multiplied by 0.86, to use on a map
     */
    fun multiply86(): Byte {
        return ((ordinal shl 2) + 1).toByte()
    }

    /**
     * Returns the color index with RGB multiplied by 0.71, to use on a map
     */
    fun multiply71(): Byte {
        return (ordinal shl 2).toByte()
    }

    /**
     * Returns the color index to use on a map
     */
    fun baseColor(): Byte {
        return ((ordinal shl 2) + 2).toByte()
    }

    fun red(): Int {
        return red
    }

    fun green(): Int {
        return green
    }

    fun blue(): Int {
        return blue
    }

    class PreciseMapColor internal constructor(val baseColor: MapColors, val multiplier: Multiplier) {

        val index: Byte
            get() = multiplier.apply(baseColor)

        companion object {
            @JvmOverloads
            fun toRGB(baseColor: MapColors = baseColor, multiplier: Multiplier = multiplier): Int {
                var r = baseColor.red().toDouble()
                var g = baseColor.green().toDouble()
                var b = baseColor.blue().toDouble()
                r *= multiplier.multiplier()
                g *= multiplier.multiplier()
                b *= multiplier.multiplier()
                val red = r.toInt()
                val green = g.toInt()
                val blue = b.toInt()
                return red shl 16 or (green shl 8) or blue
            }
        }
    }

    enum class Multiplier(private val indexGetter: Function<MapColors, Byte>, private val multiplier: Double) {
        x1_00(Function { obj: MapColors -> obj.baseColor() }, 1.00), x0_53(
            Function { obj: MapColors -> obj.multiply53() }, 0.53
        ),
        x0_71(
            Function { obj: MapColors -> obj.multiply71() }, 0.71
        ),
        x0_86(
            Function { obj: MapColors -> obj.multiply86() }, 0.86
        );

        fun multiplier(): Double {
            return multiplier
        }

        fun apply(baseColor: MapColors): Byte {
            return indexGetter.apply(baseColor)
        }
    }

    /**
     * How does Minestom compute RGB to MapColor transitions?
     */
    enum class ColorMappingStrategy {
        /**
         * If already computed, send the result. Otherwise, compute the closest color in a RGB Map, and add it to the map
         */
        LAZY,

        /**
         * All colors are already in the map after the first call. Heavy hit on the memory:
         * (2^24) * 4 bytes at the min (~64MB)
         */
        PRECISE,

        /**
         * RGB components are divided by 10 before issuing a lookup (as with the PRECISE strategy), but saves on memory usage
         */
        APPROXIMATE
    }

    companion object {
        private val rgbMap = ConcurrentHashMap<Int, PreciseMapColor?>()

        // only used if mappingStrategy == ColorMappingStrategy.PRECISE
        @Volatile
        private var rgbArray: Array<PreciseMapColor?>? = null
        private val mappingStrategy: ColorMappingStrategy? = null
        private const val MAPPING_ARGUMENT = "minestom.map.rgbmapping"

        // only used if MAPPING_ARGUMENT is "approximate"
        private const val REDUCTION_ARGUMENT = "minestom.map.rgbreduction"
        private const val colorReduction = 0

        init {
            var strategy: ColorMappingStrategy
            val strategyStr = System.getProperty(MAPPING_ARGUMENT)
            if (net.minestom.server.map.strategyStr == null) {
                net.minestom.server.map.strategy = ColorMappingStrategy.LAZY
            } else {
                try {
                    net.minestom.server.map.strategy =
                        ColorMappingStrategy.valueOf(net.minestom.server.map.strategyStr.toUpperCase())
                } catch (e: IllegalArgumentException) {
                    System.err.println("Unknown color mapping strategy: " + net.minestom.server.map.strategyStr)
                    System.err.println("Defaulting to LAZY.")
                    net.minestom.server.map.strategy = ColorMappingStrategy.LAZY
                }
            }
            mappingStrategy = net.minestom.server.map.strategy
            val reduction = 10
            val reductionStr = System.getProperty(REDUCTION_ARGUMENT)
            if (net.minestom.server.map.reductionStr != null) {
                try {
                    net.minestom.server.map.reduction = net.minestom.server.map.reductionStr.toInt()
                } catch (e: NumberFormatException) {
                    System.err.println("Invalid integer in reduction argument: " + net.minestom.server.map.reductionStr)
                    exceptionManager.handleException(e)
                }
                if (net.minestom.server.map.reduction < 0 || net.minestom.server.map.reduction >= 255) {
                    System.err.println("Reduction was found to be invalid: " + net.minestom.server.map.reduction + ". Must in 0-255, defaulting to 10.")
                    net.minestom.server.map.reduction = 10
                }
            }
            colorReduction = net.minestom.server.map.reduction
        }

        private fun fillRGBMap() {
            for (base in values()) {
                if (base == NONE) continue
                for (m in Multiplier.values()) {
                    val preciseMapColor = PreciseMapColor(base, m)
                    var rgb: Int = preciseMapColor.toRGB()
                    if (mappingStrategy == ColorMappingStrategy.APPROXIMATE) {
                        rgb = reduceColor(rgb)
                    }
                    rgbMap[rgb] = preciseMapColor
                }
            }
        }

        private fun fillRGBArray() {
            rgbArray = arrayOfNulls(0xFFFFFF + 1)
            for (rgb in 0..0xFFFFFF) {
                rgbArray!![rgb] = mapColor(rgb)
            }
        }

        fun closestColor(argb: Int): PreciseMapColor? {
            var noAlpha = argb and 0xFFFFFF
            if (mappingStrategy == ColorMappingStrategy.PRECISE) {
                if (rgbArray == null) {
                    synchronized(MapColors::class.java) {
                        if (rgbArray == null) {
                            fillRGBArray()
                        }
                    }
                }
                return rgbArray!![noAlpha]
            }
            if (rgbMap.isEmpty()) {
                synchronized(rgbMap) {
                    if (rgbMap.isEmpty()) {
                        fillRGBMap()
                    }
                }
            }
            if (mappingStrategy == ColorMappingStrategy.APPROXIMATE) {
                noAlpha = reduceColor(noAlpha)
            }
            return rgbMap.computeIfAbsent(noAlpha) { rgb: Int -> mapColor(rgb) }
        }

        private fun reduceColor(rgb: Int): Int {
            var red = rgb shr 16 and 0xFF
            var green = rgb shr 8 and 0xFF
            var blue = rgb and 0xFF
            red = red / colorReduction
            green = green / colorReduction
            blue = blue / colorReduction
            return red shl 16 or (green shl 8) or blue
        }

        private fun mapColor(rgb: Int): PreciseMapColor? {
            var closest: PreciseMapColor? = null
            var closestDistance = Int.MAX_VALUE
            for (base in values()) {
                if (base == NONE) continue
                for (m in Multiplier.values()) {
                    val rgbKey = PreciseMapColor.toRGB(base, m)
                    val redKey = rgbKey shr 16 and 0xFF
                    val greenKey = rgbKey shr 8 and 0xFF
                    val blueKey = rgbKey and 0xFF
                    val red = rgb shr 16 and 0xFF
                    val green = rgb shr 8 and 0xFF
                    val blue = rgb and 0xFF
                    val dr = redKey - red
                    val dg = greenKey - green
                    val db = blueKey - blue
                    val dist = dr * dr + dg * dg + db * db
                    if (dist < closestDistance) {
                        closest = PreciseMapColor(base, m)
                        closestDistance = dist
                    }
                }
            }
            return closest
        }
    }
}