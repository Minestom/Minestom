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
import net.minestom.server.utils.time.TimeUnit
import java.time.Duration

/**
 * Represents an argument giving a time (day/second/tick).
 *
 *
 * Example: 50d, 25s, 75t
 */
class ArgumentTime(id: String) : Argument<Duration>(id) {
    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): Duration {
        var input = input
        val lastChar = input[input.length - 1]
        val timeUnit: TemporalUnit
        if (Character.isDigit(lastChar)) timeUnit = TimeUnit.SERVER_TICK else if (SUFFIXES.contains(lastChar)) {
            input = input.substring(0, input.length - 1)
            timeUnit = if (lastChar == 'd') {
                TimeUnit.DAY
            } else if (lastChar == 's') {
                TimeUnit.SECOND
            } else if (lastChar == 't') {
                TimeUnit.SERVER_TICK
            } else {
                throw ArgumentSyntaxException("Time needs to have the unit d, s, t, or none", input, NO_NUMBER)
            }
        } else throw ArgumentSyntaxException("Time needs to have a unit", input, NO_NUMBER)
        return try {
            // Check if value is a number
            val time = input.toInt()
            Duration.of(time.toLong(), timeUnit)
        } catch (e: NumberFormatException) {
            throw ArgumentSyntaxException("Time needs to be a number", input, NO_NUMBER)
        }
    }

    override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
        val argumentNode: DeclareCommandsPacket.Node =
            Argument.Companion.simpleArgumentNode(this, executable, false, false)
        argumentNode.parser = "minecraft:time"
        nodeMaker.addNodes(arrayOf(argumentNode))
    }

    override fun toString(): String {
        return String.format("Time<%s>", id)
    }

    companion object {
        const val INVALID_TIME_FORMAT = -2
        const val NO_NUMBER = -3
        private val SUFFIXES: CharList = CharArrayList(charArrayOf('d', 's', 't'))
    }
}