package net.minestom.server.command.builder.arguments

import org.jglrxavpok.hephaistos.parser.SNBTParser.parse
import net.minestom.server.command.builder.arguments.number.ArgumentNumber
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
import org.jetbrains.annotations.ApiStatus
import java.util.function.*
import java.util.function.Function
import java.util.function.Supplier

/**
 * An argument is meant to be parsed when added into a [Command]'s syntax with [Command.addSyntax].
 *
 *
 * You can create your own with your own special conditions.
 *
 *
 * Arguments are parsed using [.parse].
 *
 * @param <T> the type of this parsed argument
</T> */
abstract class Argument<T>
/**
 * Creates a new argument with `useRemaining` sets to false.
 *
 * @param id         the id of the argument, used to retrieve the parsed value
 * @param allowSpace true if the argument can/should have spaces in it
 */ @JvmOverloads constructor(
    /**
     * Gets the ID of the argument, showed in-game above the chat bar
     * and used to retrieve the data when the command is parsed in [net.minestom.server.command.builder.CommandContext].
     *
     * @return the argument id
     */
    val id: String, protected val allowSpace: Boolean = false, protected val useRemaining: Boolean = false
) {
    /**
     * Gets the [ArgumentCallback] to check if the argument-specific conditions are validated or not.
     *
     * @return the argument callback, null if not any
     */
    /**
     * Sets the [ArgumentCallback].
     *
     * @param callback the argument callback, null to do not have one
     */
    var callback: ArgumentCallback? = null
    var defaultValue: Supplier<T>? = null
        private set

    /**
     * Gets the suggestion callback of the argument
     *
     * @return the suggestion callback of the argument, null if it doesn't exist
     * @see .setSuggestionCallback
     */
    var suggestionCallback: SuggestionCallback? = null
        private set
    /**
     * Creates a new argument.
     *
     * @param id           the id of the argument, used to retrieve the parsed value
     * @param allowSpace   true if the argument can/should have spaces in it
     * @param useRemaining true if the argument will always take the rest of the command arguments
     */
    /**
     * Creates a new argument with `useRemaining` and `allowSpace` sets to false.
     *
     * @param id the id of the argument, used to retrieve the parsed value
     */
    /**
     * Parses the given input, and throw an [ArgumentSyntaxException]
     * if the input cannot be converted to `T`
     *
     * @param input the argument to parse
     * @return the parsed argument
     * @throws ArgumentSyntaxException if `value` is not valid
     */
    @Throws(ArgumentSyntaxException::class)
    abstract fun parse(input: String): T

    /**
     * Turns the argument into a list of nodes for command dispatching. Make sure to set the Node's parser.
     *
     * @param nodeMaker  helper object used to create and modify nodes
     * @param executable true if this will be the last argument, false otherwise
     */
    abstract fun processNodes(nodeMaker: NodeMaker, executable: Boolean)

    /**
     * Gets if the argument can contain space.
     *
     * @return true if the argument allows space, false otherwise
     */
    fun allowSpace(): Boolean {
        return allowSpace
    }

    /**
     * Gets if the argument always use all the remaining characters.
     *
     *
     * ex: /help I am a test - will always give you "I am a test"
     * if the first and single argument does use the remaining.
     *
     * @return true if the argument use all the remaining characters, false otherwise
     */
    fun useRemaining(): Boolean {
        return useRemaining
    }

    /**
     * Gets if the argument has any error callback.
     *
     * @return true if the argument has an error callback, false otherwise
     */
    fun hasErrorCallback(): Boolean {
        return callback != null
    }

    /**
     * Gets if this argument is 'optional'.
     *
     *
     * Optional means that this argument can be put at the end of a syntax
     * and obtains a default value ([.getDefaultValue]).
     *
     * @return true if this argument is considered optional
     */
    val isOptional: Boolean
        get() = defaultValue != null

    /**
     * Sets the default value supplier of the argument.
     *
     *
     * A non-null value means that the argument can be put at the end of a syntax
     * to act as an optional one.
     *
     * @param defaultValue the default argument value, null to make the argument non-optional
     * @return 'this' for chaining
     */
    fun setDefaultValue(defaultValue: Supplier<T>?): Argument<T> {
        this.defaultValue = defaultValue
        return this
    }

    /**
     * Sets the default value supplier of the argument.
     *
     * @param defaultValue the default argument value
     * @return 'this' for chaining
     */
    fun setDefaultValue(defaultValue: T): Argument<T> {
        this.defaultValue = Supplier { defaultValue }
        return this
    }

    /**
     * Sets the suggestion callback (for dynamic tab completion) of this argument.
     *
     *
     * Note: This will not automatically filter arguments by user input.
     *
     * @param suggestionCallback The suggestion callback to set.
     * @return 'this' for chaining
     */
    fun setSuggestionCallback(suggestionCallback: SuggestionCallback): Argument<T> {
        this.suggestionCallback = suggestionCallback
        return this
    }

    /**
     * Check if the argument has a suggestion.
     *
     * @return If this argument has a suggestion.
     */
    fun hasSuggestion(): Boolean {
        return suggestionCallback != null
    }

    /**
     * Maps this argument's output to another result.
     *
     * @param mapper The mapper to use (this argument's input = desired output)
     * @param <O>    The type of output expected.
     * @return A new ArgumentMap that can get this complex object type.
    </O> */
    @ApiStatus.Experimental
    fun <O> map(mapper: Function<T, O>): Argument<O> {
        return ArgumentMap(this, mapper)
    }

    /**
     * Maps this argument's output to another result.
     *
     * @param predicate the argument predicate
     * @return A new ArgumentMap that filters using this filterer.
     */
    @ApiStatus.Experimental
    fun filter(predicate: Predicate<T>): Argument<T> {
        return ArgumentFilter(this, predicate)
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false
        val argument = o as Argument<*>
        return id == argument.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }

    private class ArgumentMap<I, O>(argument: Argument<I>, mapper: Function<I, O>) :
        Argument<O>(argument.id, argument.allowSpace(), argument.useRemaining()) {
        val argument: Argument<I>
        val mapper: Function<I, O>

        init {
            if (argument.suggestionCallback != null) setSuggestionCallback(argument.suggestionCallback!!)
            if (argument.defaultValue != null) this.setDefaultValue {
                mapper.apply(
                    argument.defaultValue!!.get()
                )
            }
            this.argument = argument
            this.mapper = mapper
        }

        @Throws(ArgumentSyntaxException::class)
        override fun parse(input: String): O {
            val value = argument.parse(input)
            return mapper.apply(value)
                ?: throw ArgumentSyntaxException("Couldn't be converted to map type", input, INVALID_MAP)
        }

        override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
            argument.processNodes(nodeMaker, executable)
        }

        companion object {
            const val INVALID_MAP = 555
        }
    }

    private class ArgumentFilter<T>(argument: Argument<T>, predicate: Predicate<T>) :
        Argument<T>(argument.id, argument.allowSpace(), argument.useRemaining()) {
        val argument: Argument<T>
        val predicate: Predicate<T>

        init {
            if (argument.suggestionCallback != null) setSuggestionCallback(argument.suggestionCallback!!)
            if (argument.defaultValue != null) this.setDefaultValue(argument.defaultValue)
            this.argument = argument
            this.predicate = predicate
        }

        @Throws(ArgumentSyntaxException::class)
        override fun parse(input: String): T {
            val result = argument.parse(input)
            if (!predicate.test(result)) throw ArgumentSyntaxException("Predicate failed", input, INVALID_FILTER)
            return result
        }

        override fun processNodes(nodeMaker: NodeMaker, executable: Boolean) {
            argument.processNodes(nodeMaker, executable)
        }

        companion object {
            const val INVALID_FILTER = 556
        }
    }

    companion object {
        /**
         * Parses an argument, using [Argument.getId] as the input
         *
         * @param argument the argument, with the input as id
         * @param <T>      the result type
         * @return the parsed result
         * @throws ArgumentSyntaxException if the argument cannot be parsed due to a fault input (argument id)
        </T> */
        @kotlin.jvm.JvmStatic
        @ApiStatus.Experimental
        @Throws(ArgumentSyntaxException::class)
        fun <T> parse(argument: Argument<T>): T {
            return argument.parse(argument.id)
        }

        /**
         * Builds an argument node.
         *
         * @param argument   the argument
         * @param executable true if this will be the last argument, false otherwise
         * @return the created [DeclareCommandsPacket.Node]
         */
        protected fun simpleArgumentNode(
            argument: Argument<*>,
            executable: Boolean, redirect: Boolean, suggestion: Boolean
        ): DeclareCommandsPacket.Node {
            val argumentNode = DeclareCommandsPacket.Node()
            argumentNode.flags =
                DeclareCommandsPacket.getFlag(DeclareCommandsPacket.NodeType.ARGUMENT, executable, redirect, suggestion)
            argumentNode.name = argument.id
            return argumentNode
        }
    }
}