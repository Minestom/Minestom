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
import java.util.Locale
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
import net.minestom.server.utils.validate.Check

/**
 * Represents a single word in the command.
 *
 *
 * You can specify the valid words with [.from] (do not abuse it or the client will not be able to join).
 *
 *
 * Example: hey
 */
class ArgumentWord(id: String) : Argument<String>(id) {
    /**
     * Gets all the word restrictions.
     *
     * @return the word restrictions, can be null
     */
    var restrictions: Array<String>?
        protected set

    /**
     * Used to force the use of a few precise words instead of complete freedom.
     *
     *
     * WARNING: having an array too long would result in a packet too big or the client being stuck during login.
     *
     * @param restrictions the accepted words,
     * can be null but if an array is passed
     * you need to ensure that it is filled with non-null values
     * @return 'this' for chaining
     * @throws NullPointerException if `restrictions` is not null but contains null value(s)
     */
    fun from(vararg restrictions: String): ArgumentWord {
        if (restrictions != null) {
            for (restriction in restrictions) {
                Check.notNull(
                    restriction,
                    "ArgumentWord restriction cannot be null, you can pass 'null' instead of an empty array"
                )
            }
        }
        this.restrictions = restrictions
        return this
    }

    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): String {
        if (input.contains(StringUtils.SPACE)) throw ArgumentSyntaxException(
            "Word cannot contain space character",
            input,
            SPACE_ERROR
        )

        // Check restrictions (acting as literal)
        if (hasRestrictions()) {
            for (r in restrictions!!) {
                if (input == r) return input
            }
            throw ArgumentSyntaxException("Word needs to be in the restriction list", input, RESTRICTION_ERROR)
        }
        return input
    }

    override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
        if (restrictions != null) {

            // Create a primitive array for mapping
            val nodes = arrayOfNulls<DeclareCommandsPacket.Node>(restrictions!!.size)

            // Create a node for each restrictions as literal
            for (i in nodes.indices) {
                val argumentNode = DeclareCommandsPacket.Node()
                argumentNode.flags = DeclareCommandsPacket.getFlag(
                    DeclareCommandsPacket.NodeType.LITERAL,
                    executable, false, false
                )
                argumentNode.name = restrictions!![i]
                nodes[i] = argumentNode
            }
            nodeMaker.addNodes(nodes)
        } else {
            // Can be any word, add only one argument node
            val argumentNode: DeclareCommandsPacket.Node =
                Argument.Companion.simpleArgumentNode(this, executable, false, false)
            argumentNode.parser = "brigadier:string"
            argumentNode.properties = BinaryWriter.makeArray { packetWriter: BinaryWriter ->
                packetWriter.writeVarInt(0) // Single word
            }
            nodeMaker.addNodes(arrayOf(argumentNode))
        }
    }

    /**
     * Gets if this argument allow complete freedom in the word choice or if a list has been defined.
     *
     * @return true if the word selection is restricted
     */
    fun hasRestrictions(): Boolean {
        return restrictions != null && restrictions!!.size > 0
    }

    override fun toString(): String {
        return String.format("Word<%s>", id)
    }

    companion object {
        const val SPACE_ERROR = 1
        const val RESTRICTION_ERROR = 2
    }
}