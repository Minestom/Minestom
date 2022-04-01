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
import net.minestom.server.utils.math.IntRange
import java.util.*
import java.util.regex.Pattern

/**
 * Represents the target selector argument.
 * https://minecraft.gamepedia.com/Commands#Target_selectors
 */
class ArgumentEntity(id: String) : Argument<EntityFinder>(id, true) {
    var isOnlySingleEntity = false
        private set
    var isOnlyPlayers = false
        private set

    fun singleEntity(singleEntity: Boolean): ArgumentEntity {
        isOnlySingleEntity = singleEntity
        return this
    }

    fun onlyPlayers(onlyPlayers: Boolean): ArgumentEntity {
        isOnlyPlayers = onlyPlayers
        return this
    }

    @Throws(ArgumentSyntaxException::class)
    override fun parse(input: String): EntityFinder {
        return staticParse(input, isOnlySingleEntity, isOnlyPlayers)
    }

    override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
        val argumentNode: DeclareCommandsPacket.Node =
            Argument.Companion.simpleArgumentNode(this, executable, false, false)
        argumentNode.parser = "minecraft:entity"
        argumentNode.properties = BinaryWriter.makeArray { packetWriter: BinaryWriter ->
            var mask: Byte = 0
            if (isOnlySingleEntity) {
                mask = mask or 0x01
            }
            if (isOnlyPlayers) {
                mask = mask or 0x02
            }
            packetWriter.writeByte(mask)
        }
        nodeMaker.addNodes(arrayOf(argumentNode))
    }

    override fun toString(): String {
        if (isOnlySingleEntity) {
            return if (isOnlyPlayers) {
                String.format("Player<%s>", id)
            } else String.format("Entity<%s>", id)
        }
        return if (isOnlyPlayers) {
            String.format("Players<%s>", id)
        } else String.format("Entities<%s>", id)
    }

    companion object {
        const val INVALID_SYNTAX = -2
        const val ONLY_SINGLE_ENTITY_ERROR = -3
        const val ONLY_PLAYERS_ERROR = -4
        const val INVALID_ARGUMENT_NAME = -5
        const val INVALID_ARGUMENT_VALUE = -6
        private val USERNAME_PATTERN = Pattern.compile("[a-zA-Z0-9_]{1,16}")
        private const val SELECTOR_PREFIX = "@"
        private val SELECTOR_VARIABLES = Arrays.asList("@p", "@r", "@a", "@e", "@s")
        private val PLAYERS_ONLY_SELECTOR = Arrays.asList("@p", "@r", "@a", "@s")
        private val SINGLE_ONLY_SELECTOR = Arrays.asList("@p", "@r", "@s")

        // List with all the valid arguments
        private val VALID_ARGUMENTS = Arrays.asList(
            "x", "y", "z",
            "distance", "dx", "dy", "dz",
            "scores", "tag", "team", "limit", "sort", "level", "gamemode", "name",
            "x_rotation", "y_rotation", "type", "nbt", "advancements", "predicate"
        )

        // List with all the easily parsable arguments which only require reading until a specific character (comma)
        private val SIMPLE_ARGUMENTS = Arrays.asList(
            "x", "y", "z",
            "distance", "dx", "dy", "dz",
            "scores", "tag", "team", "limit", "sort", "level", "gamemode",
            "x_rotation", "y_rotation", "type", "advancements", "predicate"
        )

        @Deprecated("use {@link Argument#parse(Argument)}")
        @Throws(ArgumentSyntaxException::class)
        fun staticParse(
            input: String,
            onlySingleEntity: Boolean,
            onlyPlayers: Boolean
        ): EntityFinder {
            // Check for raw player name or UUID
            if (!input.contains(SELECTOR_PREFIX) && !input.contains(StringUtils.SPACE)) {

                // Check if the input is a valid UUID
                try {
                    val uuid = UUID.fromString(input)
                    return EntityFinder()
                        .setTargetSelector(EntityFinder.TargetSelector.MINESTOM_UUID)
                        .setConstantUuid(uuid)
                } catch (ignored: IllegalArgumentException) {
                }

                // Check if the input is a valid player name
                if (USERNAME_PATTERN.matcher(input).matches()) {
                    return EntityFinder()
                        .setTargetSelector(EntityFinder.TargetSelector.MINESTOM_USERNAME)
                        .setConstantName(input)
                }
            }

            // The minimum size is always 2 (for the selector variable, ex: @p)
            if (input.length < 2) throw ArgumentSyntaxException("Length needs to be > 1", input, INVALID_SYNTAX)

            // The target selector variable always start by '@'
            if (!input.startsWith(SELECTOR_PREFIX)) throw ArgumentSyntaxException(
                "Target selector needs to start with @",
                input,
                INVALID_SYNTAX
            )
            val selectorVariable = input.substring(0, 2)

            // Check if the selector variable used exists
            if (!SELECTOR_VARIABLES.contains(selectorVariable)) throw ArgumentSyntaxException(
                "Invalid selector variable",
                input,
                INVALID_SYNTAX
            )

            // Check if it should only select single entity and if the selector variable valid the condition
            if (onlySingleEntity && !SINGLE_ONLY_SELECTOR.contains(selectorVariable)) throw ArgumentSyntaxException(
                "Argument requires only a single entity",
                input,
                ONLY_SINGLE_ENTITY_ERROR
            )

            // Check if it should only select players and if the selector variable valid the condition
            if (onlyPlayers && !PLAYERS_ONLY_SELECTOR.contains(selectorVariable)) throw ArgumentSyntaxException(
                "Argument requires only players",
                input,
                ONLY_PLAYERS_ERROR
            )

            // Create the EntityFinder which will be used for the rest of the parsing
            val entityFinder = EntityFinder()
                .setTargetSelector(toTargetSelector(selectorVariable))

            // The selector is a single selector variable which verify all the conditions
            if (input.length == 2) return entityFinder

            // START PARSING THE STRUCTURE
            val structure = input.substring(2)
            return parseStructure(input, entityFinder, structure)
        }

        @Throws(ArgumentSyntaxException::class)
        private fun parseStructure(
            input: String,
            entityFinder: EntityFinder,
            structure: String
        ): EntityFinder {
            // The structure isn't opened or closed properly
            if (!structure.startsWith("[") || !structure.endsWith("]")) throw ArgumentSyntaxException(
                "Target selector needs to start and end with brackets",
                input,
                INVALID_SYNTAX
            )

            // Remove brackets
            val structureData = structure.substring(1, structure.length - 1)
            //System.out.println("structure data: " + structureData);
            var currentArgument = ""
            var i = 0
            while (i < structureData.length) {
                val c = structureData[i]
                if (c == '=') {

                    // Replace all unnecessary spaces
                    currentArgument = currentArgument.trim { it <= ' ' }
                    if (!VALID_ARGUMENTS.contains(currentArgument)) throw ArgumentSyntaxException(
                        "Argument name '$currentArgument' does not exist", input, INVALID_ARGUMENT_NAME
                    )
                    i = parseArgument(entityFinder, currentArgument, input, structureData, i)
                    currentArgument = "" // Reset current argument
                } else {
                    currentArgument += c
                }
                i++
            }
            return entityFinder
        }

        @Throws(ArgumentSyntaxException::class)
        private fun parseArgument(
            entityFinder: EntityFinder,
            argumentName: String,
            input: String,
            structureData: String, beginIndex: Int
        ): Int {
            val comma = ','
            val isSimple = SIMPLE_ARGUMENTS.contains(argumentName)
            var finalIndex = beginIndex + 1
            val valueBuilder = StringBuilder()
            while (finalIndex < structureData.length) {
                val c = structureData[finalIndex]

                // Command is parsed
                if (isSimple && c == comma) break
                valueBuilder.append(c)
                finalIndex++
            }
            val value = valueBuilder.toString().trim { it <= ' ' }
            when (argumentName) {
                "type" -> {
                    val include = !value.startsWith("!")
                    val entityName = if (include) value else value.substring(1)
                    val entityType = EntityType.fromNamespaceId(entityName)
                        ?: throw ArgumentSyntaxException("Invalid entity name", input, INVALID_ARGUMENT_VALUE)
                    entityFinder.setEntity(
                        entityType,
                        if (include) EntityFinder.ToggleableType.INCLUDE else EntityFinder.ToggleableType.EXCLUDE
                    )
                }
                "gamemode" -> {
                    val include = !value.startsWith("!")
                    val gameModeName = if (include) value else value.substring(1)
                    try {
                        val gameMode = GameMode.valueOf(gameModeName)
                        entityFinder.setGameMode(
                            gameMode,
                            if (include) EntityFinder.ToggleableType.INCLUDE else EntityFinder.ToggleableType.EXCLUDE
                        )
                    } catch (e: IllegalArgumentException) {
                        throw ArgumentSyntaxException("Invalid entity game mode", input, INVALID_ARGUMENT_VALUE)
                    }
                }
                "limit" -> try {
                    val limit = value.toInt()
                    entityFinder.setLimit(limit)
                } catch (e: NumberFormatException) {
                    throw ArgumentSyntaxException("Invalid limit number", input, INVALID_ARGUMENT_VALUE)
                }
                "sort" -> try {
                    val entitySort = EntitySort.valueOf(value.toUpperCase())
                    entityFinder.setEntitySort(entitySort)
                } catch (e: IllegalArgumentException) {
                    throw ArgumentSyntaxException("Invalid entity sort", input, INVALID_ARGUMENT_VALUE)
                }
                "level" -> try {
                    val level: IntRange = Argument.Companion.parse<IntRange>(
                        ArgumentIntRange(value)
                    )
                    entityFinder.setLevel(level)
                } catch (e: ArgumentSyntaxException) {
                    throw ArgumentSyntaxException("Invalid level number", input, INVALID_ARGUMENT_VALUE)
                }
                "distance" -> try {
                    val distance: IntRange = Argument.Companion.parse<IntRange>(
                        ArgumentIntRange(value)
                    )
                    entityFinder.setDistance(distance)
                } catch (e: ArgumentSyntaxException) {
                    throw ArgumentSyntaxException("Invalid level number", input, INVALID_ARGUMENT_VALUE)
                }
            }
            return finalIndex
        }

        private fun toTargetSelector(selectorVariable: String): EntityFinder.TargetSelector {
            if (selectorVariable == "@p") return EntityFinder.TargetSelector.NEAREST_PLAYER
            if (selectorVariable == "@r") return EntityFinder.TargetSelector.RANDOM_PLAYER
            if (selectorVariable == "@a") return EntityFinder.TargetSelector.ALL_PLAYERS
            if (selectorVariable == "@e") return EntityFinder.TargetSelector.ALL_ENTITIES
            if (selectorVariable == "@s") return EntityFinder.TargetSelector.SELF
            throw IllegalStateException("Weird selector variable: $selectorVariable")
        }
    }
}