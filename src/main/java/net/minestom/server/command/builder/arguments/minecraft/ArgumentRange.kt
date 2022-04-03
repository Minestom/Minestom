package net.minestom.server.command.builder.arguments.minecraft

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
import net.minestom.server.utils.math.Range
import java.util.function.BiFunction
import java.util.function.Function
import java.util.regex.Pattern

/**
 * Abstract class used by [ArgumentIntRange] and [ArgumentFloatRange].
 *
 * @param <T> the type of the range
</T> */
abstract class ArgumentRange<T : Range<N>?, N : Number?>(
    id: String,
    private val parserName: String,
    private val min: N,
    private val max: N,
    private val parser: Function<String, N>,
    private val rangeConstructor: BiFunction<N, N, T>
) : Argument<T>(id) {
    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): T {
        try {
            val split = input.split(Pattern.quote("..")).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (split.size == 2) {
                val min: N
                val max: N
                if (split[0].length == 0 && split[1].length > 0) {
                    // Format ..NUMBER
                    min = this.min
                    max = parser.apply(split[1])
                } else if (split[0].length > 0 && split[1].length == 0) {
                    // Format NUMBER..
                    min = parser.apply(split[0])
                    max = this.max
                } else if (split[0].length > 0) {
                    // Format NUMBER..NUMBER
                    min = parser.apply(split[0])
                    max = parser.apply(split[1])
                } else {
                    // Format ..
                    throw ArgumentSyntaxException("Invalid range format", input, FORMAT_ERROR)
                }
                return rangeConstructor.apply(min, max)
            } else if (split.size == 1) {
                val number = parser.apply(input)
                return rangeConstructor.apply(number, number)
            }
        } catch (e2: NumberFormatException) {
            throw ArgumentSyntaxException("Invalid number", input, FORMAT_ERROR)
        }
        throw ArgumentSyntaxException("Invalid range format", input, FORMAT_ERROR)
    }

    override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
        val argumentNode: DeclareCommandsPacket.Node =
            Argument.Companion.simpleArgumentNode(this, executable, false, false)
        argumentNode.parser = parserName
        nodeMaker.addNodes(arrayOf(argumentNode))
    }

    companion object {
        const val FORMAT_ERROR = -1
    }
}