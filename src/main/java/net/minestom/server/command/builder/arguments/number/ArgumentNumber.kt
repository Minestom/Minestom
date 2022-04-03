package net.minestom.server.command.builder.arguments.number

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
import java.util.Comparator
import java.util.function.BiFunction
import java.util.function.Function
import java.util.regex.Pattern

open class ArgumentNumber<T : Number?> internal constructor(
    id: String, protected val parserName: String, protected val parser: Function<String, T>,
    protected val radixParser: BiFunction<String, Int?, T>, protected val propertiesWriter: BiConsumer<BinaryWriter, T>,
    comparator: Comparator<T>
) : Argument<T>(id) {
    protected var hasMin = false
    protected var hasMax = false
    protected var min: T? = null
    protected var max: T? = null
    protected val comparator: Comparator<T?>

    init {
        this.comparator = comparator
    }

    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): T {
        return try {
            val value: T
            val radix = getRadix(input)
            value = if (radix == 10) {
                parser.apply(parseValue(input))
            } else {
                radixParser.apply(parseValue(input), radix)
            }

            // Check range
            if (hasMin && comparator.compare(value, min) < 0) {
                throw ArgumentSyntaxException("Input is lower than the minimum allowed value", input, TOO_LOW_ERROR)
            }
            if (hasMax && comparator.compare(value, max) > 0) {
                throw ArgumentSyntaxException("Input is higher than the maximum allowed value", input, TOO_HIGH_ERROR)
            }
            value
        } catch (e: NumberFormatException) {
            throw ArgumentSyntaxException(
                "Input is not a number, or it's invalid for the given type",
                input,
                NOT_NUMBER_ERROR
            )
        } catch (e: NullPointerException) {
            throw ArgumentSyntaxException(
                "Input is not a number, or it's invalid for the given type",
                input,
                NOT_NUMBER_ERROR
            )
        }
    }

    override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
        val argumentNode: DeclareCommandsPacket.Node =
            Argument.Companion.simpleArgumentNode(this, executable, false, false)
        argumentNode.parser = parserName
        argumentNode.properties = BinaryWriter.makeArray { packetWriter: BinaryWriter ->
            packetWriter.writeByte(numberProperties)
            if (hasMin()) propertiesWriter.accept(packetWriter, getMin())
            if (hasMax()) propertiesWriter.accept(packetWriter, getMax())
        }
        nodeMaker.addNodes(arrayOf(argumentNode))
    }

    fun min(value: T): ArgumentNumber<T> {
        min = value
        hasMin = true
        return this
    }

    fun max(value: T): ArgumentNumber<T> {
        max = value
        hasMax = true
        return this
    }

    fun between(min: T, max: T): ArgumentNumber<T> {
        this.min = min
        this.max = max
        hasMin = true
        hasMax = true
        return this
    }

    /**
     * Creates the byteflag based on the number's min/max existance.
     *
     * @return A byteflag for argument specification.
     */
    val numberProperties: Byte
        get() {
            var result: Byte = 0
            if (hasMin()) result = result or 0x1
            if (hasMax()) result = result or 0x2
            return result
        }

    /**
     * Gets if the argument has a minimum.
     *
     * @return true if the argument has a minimum
     */
    fun hasMin(): Boolean {
        return hasMin
    }

    /**
     * Gets the minimum value for this argument.
     *
     * @return the minimum of this argument
     */
    fun getMin(): T {
        return min
    }

    /**
     * Gets if the argument has a maximum.
     *
     * @return true if the argument has a maximum
     */
    fun hasMax(): Boolean {
        return hasMax
    }

    /**
     * Gets the maximum value for this argument.
     *
     * @return the maximum of this argument
     */
    fun getMax(): T {
        return max
    }

    protected fun parseValue(value: String): String {
        var value = value
        if (value.startsWith("0b")) {
            value = value.replaceFirst(Pattern.quote("0b").toRegex(), "")
        } else if (value.startsWith("0x")) {
            value = value.replaceFirst(Pattern.quote("0x").toRegex(), "")
        } else if (value.toLowerCase().contains("e")) {
            value = removeScientificNotation(value)!!
        }
        // TODO number suffix support (k,m,b,t)
        return value
    }

    protected fun getRadix(value: String): Int {
        if (value.startsWith("0b")) {
            return 2
        } else if (value.startsWith("0x")) {
            return 16
        }
        return 10
    }

    protected fun removeScientificNotation(value: String): String? {
        return try {
            BigDecimal(value).toPlainString()
        } catch (e: NumberFormatException) {
            null
        }
    }

    companion object {
        const val NOT_NUMBER_ERROR = 1
        const val TOO_LOW_ERROR = 2
        const val TOO_HIGH_ERROR = 3
    }
}