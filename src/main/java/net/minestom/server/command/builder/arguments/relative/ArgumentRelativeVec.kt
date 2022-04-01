package net.minestom.server.command.builder.arguments.relative

import org.jglrxavpok.hephaistos.parser.SNBTParser.parse
import net.minestom.server.command.builder.arguments.number.ArgumentNumber
import java.util.function.BiConsumer
import net.minestom.server.utils.binary.BinaryWriter
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import java.lang.NumberFormatException
import java.lang.NullPointerException
import net.minestom.server.command.builder.NodeMaker
import net.minestom.server.network.packet.server.play.DeclareCommandsPacket
import java.math.BigDecimal
import net.minestom.server.utils.location.RelativeVec
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec
import net.minestom.server.utils.location.RelativeVec.CoordinateType
import net.minestom.server.coordinate.Vec
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentRegistry
import net.minestom.server.entity.EntityType
import net.minestom.server.item.Enchantment
import net.minestom.server.potion.PotionEffect
import java.time.temporal.TemporalUnit
import net.minestom.server.command.builder.arguments.minecraft.ArgumentTime
import it.unimi.dsi.fastutil.chars.CharList
import it.unimi.dsi.fastutil.chars.CharArrayList
import java.lang.IllegalArgumentException
import net.minestom.server.command.builder.arguments.minecraft.ArgumentUUID
import net.kyori.adventure.text.format.NamedTextColor
import net.minestom.server.command.builder.arguments.minecraft.ArgumentColor
import net.minestom.server.command.builder.arguments.minecraft.ArgumentRange
import net.minestom.server.utils.entity.EntityFinder
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import java.lang.StringBuilder
import net.minestom.server.entity.GameMode
import net.minestom.server.utils.entity.EntityFinder.EntitySort
import net.minestom.server.command.builder.arguments.minecraft.ArgumentIntRange
import java.lang.IllegalStateException
import org.jglrxavpok.hephaistos.nbt.NBT
import org.jglrxavpok.hephaistos.nbt.NBTException
import net.minestom.server.command.builder.arguments.minecraft.ArgumentNbtTag
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.minestom.server.command.builder.arguments.minecraft.ArgumentComponent
import net.minestom.server.item.ItemStack
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack
import net.minestom.server.item.Material
import org.jglrxavpok.hephaistos.nbt.NBTCompound
import net.minestom.server.command.builder.arguments.minecraft.ArgumentBlockState
import net.minestom.server.command.builder.arguments.minecraft.ArgumentNbtCompoundTag
import net.minestom.server.command.builder.arguments.minecraft.ArgumentResourceLocation
import net.minestom.server.command.builder.ArgumentCallback
import net.minestom.server.command.builder.suggestion.SuggestionCallback
import net.minestom.server.command.builder.arguments.Argument.ArgumentMap
import net.minestom.server.command.builder.arguments.Argument.ArgumentFilter
import java.util.function.UnaryOperator
import java.util.Locale
import java.lang.SafeVarargs
import net.minestom.server.command.builder.NodeMaker.ConfiguredNodes
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.arguments.number.ArgumentFloat
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEnchantment
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentParticle
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentPotionEffect
import net.minestom.server.command.builder.arguments.minecraft.registry.ArgumentEntityType
import net.minestom.server.command.builder.arguments.minecraft.ArgumentFloatRange
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeBlockPosition
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec3
import net.minestom.server.command.builder.arguments.relative.ArgumentRelativeVec2
import net.minestom.server.command.builder.arguments.number.ArgumentLong
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.parser.ValidSyntaxHolder
import net.minestom.server.command.builder.parser.CommandParser
import net.minestom.server.command.builder.CommandResult
import net.minestom.server.command.builder.CommandDispatcher
import net.minestom.server.MinecraftServer
import net.minestom.server.command.builder.arguments.*
import net.minestom.server.utils.StringUtils
import java.util.Set
import java.util.function.Function

/**
 * Common interface for all the relative location arguments.
 */
abstract class ArgumentRelativeVec(
    id: String,
    /**
     * Gets the amount of numbers that this relative location needs.
     *
     * @return the amount of coordinate required
     */
    val numberCount: Int
) : Argument<RelativeVec>(id, true) {

    abstract val relativeNumberParser: Function<String, out Number>
    abstract val absoluteNumberParser: Function<String, out Number>
    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): RelativeVec {
        val split = input.split(StringUtils.SPACE).toTypedArray()
        if (split.size != numberCount) {
            throw ArgumentSyntaxException("Invalid number of values", input, INVALID_NUMBER_COUNT_ERROR)
        }
        val coordinates = DoubleArray(split.size)
        val isRelative = BooleanArray(split.size)
        var type: CoordinateType? = null
        for (i in split.indices) {
            val element = split[i]
            try {
                val modifierChar = element[0]
                if (MODIFIER_CHARS.contains(modifierChar)) {
                    isRelative[i] = true
                    if (type == null) {
                        type = if (modifierChar == LOCAL_CHAR) CoordinateType.LOCAL else CoordinateType.RELATIVE
                    } else if (type != (if (modifierChar == LOCAL_CHAR) CoordinateType.LOCAL else CoordinateType.RELATIVE)) {
                        throw ArgumentSyntaxException(
                            "Cannot mix world & local coordinates (everything must either use ^ or not)",
                            input,
                            MIXED_TYPE_ERROR
                        )
                    }
                    if (element.length > 1) {
                        val potentialNumber = element.substring(1)
                        coordinates[i] = relativeNumberParser.apply(potentialNumber).toDouble()
                    }
                } else {
                    if (type == null) {
                        type = CoordinateType.ABSOLUTE
                    } else if (type == CoordinateType.LOCAL) {
                        throw ArgumentSyntaxException(
                            "Cannot mix world & local coordinates (everything must either use ^ or not)",
                            input,
                            MIXED_TYPE_ERROR
                        )
                    }
                    coordinates[i] = absoluteNumberParser.apply(element).toDouble()
                }
            } catch (e: NumberFormatException) {
                throw ArgumentSyntaxException("Invalid number", input, INVALID_NUMBER_ERROR)
            }
        }
        return RelativeVec(
            if (split.size == 3) Vec(coordinates[0], coordinates[1], coordinates[2]) else Vec(
                coordinates[0],
                coordinates[1]
            ),
            type!!,
            isRelative[0], split.size == 3 && isRelative[1], isRelative[if (split.size == 3) 2 else 1]
        )
    }

    companion object {
        private const val RELATIVE_CHAR = '~'
        private const val LOCAL_CHAR = '^'
        private val MODIFIER_CHARS = Set.of(RELATIVE_CHAR, LOCAL_CHAR)
        const val INVALID_NUMBER_COUNT_ERROR = 1
        const val INVALID_NUMBER_ERROR = 2
        const val MIXED_TYPE_ERROR = 3
    }
}