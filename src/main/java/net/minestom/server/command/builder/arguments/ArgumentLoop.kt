package net.minestom.server.command.builder.arguments

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
import net.minestom.server.command.builder.arguments.ArgumentEnum
import java.util.function.UnaryOperator
import java.lang.SafeVarargs
import net.minestom.server.command.builder.arguments.ArgumentLoop
import net.minestom.server.command.builder.NodeMaker.ConfiguredNodes
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import net.minestom.server.command.builder.arguments.ArgumentGroup
import net.minestom.server.command.builder.arguments.ArgumentBoolean
import net.minestom.server.command.builder.arguments.number.ArgumentInteger
import net.minestom.server.command.builder.arguments.number.ArgumentDouble
import net.minestom.server.command.builder.arguments.number.ArgumentFloat
import net.minestom.server.command.builder.arguments.ArgumentString
import net.minestom.server.command.builder.arguments.ArgumentWord
import net.minestom.server.command.builder.arguments.ArgumentStringArray
import net.minestom.server.command.builder.arguments.ArgumentCommand
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
import net.minestom.server.utils.StringUtils
import java.util.*

class ArgumentLoop<T> @SafeVarargs constructor(id: String, vararg arguments: Argument<T>) :
    Argument<List<T>>(id, true, true) {
    private val arguments: MutableList<Argument<T>> = ArrayList()

    init {
        this.arguments.addAll(Arrays.asList(*arguments))
    }

    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): List<T> {
        val result: MutableList<T> = ArrayList()
        val split = input.split(StringUtils.SPACE).toTypedArray()
        val builder = StringBuilder()
        var success = false
        for (s in split) {
            builder.append(s)
            for (argument in arguments) {
                try {
                    val inputString = builder.toString()
                    val value = argument.parse(inputString)
                    success = true
                    result.add(value)
                    break
                } catch (ignored: ArgumentSyntaxException) {
                    success = false
                }
            }
            if (success) {
                builder.setLength(0) // Clear
            } else {
                builder.append(StringUtils.SPACE)
            }
        }
        if (result.isEmpty() || !success) {
            throw ArgumentSyntaxException("Invalid loop, there is no valid argument found", input, INVALID_INPUT_ERROR)
        }
        return result
    }

    override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
        val latestNodes = nodeMaker.latestNodes
        for (latestNode in latestNodes) {
            val id = nodeMaker.nodeIdsMap.getInt(latestNode)
            for (argument in arguments) {
                argument.processNodes(nodeMaker, executable)
                val configuredNodes = nodeMaker.latestConfiguredNodes
                // For the next loop argument to start at the same place
                configuredNodes.options.previousNodes = latestNodes
                for (lastArgumentNode in configuredNodes.nodes) {
                    lastArgumentNode.flags = lastArgumentNode.flags or 0x08
                    lastArgumentNode.redirectedNode = id
                }
            }
        }
    }

    companion object {
        const val INVALID_INPUT_ERROR = 1
    }
}