package net.minestom.server.command.builder.parser

import net.minestom.server.command.builder.arguments.Argument.id
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity.singleEntity
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity.onlyPlayers
import net.minestom.server.command.builder.arguments.Argument.useRemaining
import net.minestom.server.command.builder.arguments.Argument.parse
import net.minestom.server.command.builder.arguments.Argument.allowSpace
import net.minestom.server.command.builder.CommandDispatcher
import net.minestom.server.command.builder.parser.CommandQueryResult
import net.minestom.server.command.builder.parser.CommandParser
import net.minestom.server.command.builder.CommandSyntax
import net.minestom.server.command.builder.parser.ValidSyntaxHolder
import it.unimi.dsi.fastutil.ints.Int2ObjectRBTreeMap
import net.minestom.server.command.builder.parser.CommandSuggestionHolder
import net.minestom.server.command.builder.parser.ArgumentParser.ArgumentResult
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.parser.ArgumentQueryResult
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import java.lang.StringBuilder
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import java.util.Locale
import java.lang.IllegalArgumentException
import java.util.function.IntFunction
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.utils.StringUtils
import org.jetbrains.annotations.ApiStatus
import java.util.ArrayList
import java.util.function.Function

object ArgumentParser {
    private val ARGUMENT_FUNCTION_MAP: MutableMap<String, Function<String, Argument<*>>> = ConcurrentHashMap()

    init {
        ARGUMENT_FUNCTION_MAP["literal"] =
            Function { id: String? -> ArgumentLiteral(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["boolean"] =
            Function { id: String? -> ArgumentBoolean(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["integer"] =
            Function { id: String? -> ArgumentInteger(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["double"] =
            Function { id: String? -> ArgumentDouble(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["float"] =
            Function { id: String? -> ArgumentFloat(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["string"] =
            Function { id: String? -> ArgumentString(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["word"] =
            Function { id: String? -> ArgumentWord(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["stringarray"] =
            Function { id: String? -> ArgumentStringArray(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["command"] =
            Function { id: String? -> ArgumentCommand(net.minestom.server.command.builder.parser.id) }
        // TODO enum
        ARGUMENT_FUNCTION_MAP["color"] =
            Function { id: String? -> ArgumentColor(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["time"] =
            Function { id: String? -> ArgumentTime(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["enchantment"] =
            Function { id: String? -> ArgumentEnchantment(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["particle"] =
            Function { id: String? -> ArgumentParticle(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["resourceLocation"] =
            Function { id: String? -> ArgumentResourceLocation(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["potion"] =
            Function { id: String? -> ArgumentPotionEffect(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["entityType"] =
            Function { id: String? -> ArgumentEntityType(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["blockState"] =
            Function { id: String? -> ArgumentBlockState(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["intrange"] =
            Function { id: String? -> ArgumentIntRange(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["floatrange"] =
            Function { id: String? -> ArgumentFloatRange(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["entity"] =
            Function { s: String? -> ArgumentEntity(net.minestom.server.command.builder.parser.s).singleEntity(true) }
        ARGUMENT_FUNCTION_MAP["entities"] =
            Function { id: String? -> ArgumentEntity(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["player"] = Function { s: String? ->
            ArgumentEntity(net.minestom.server.command.builder.parser.s).singleEntity(true).onlyPlayers(true)
        }
        ARGUMENT_FUNCTION_MAP["players"] =
            Function { s: String? -> ArgumentEntity(net.minestom.server.command.builder.parser.s).onlyPlayers(true) }
        ARGUMENT_FUNCTION_MAP["itemstack"] =
            Function { id: String? -> ArgumentItemStack(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["component"] =
            Function { id: String? -> ArgumentComponent(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["uuid"] =
            Function { id: String? -> ArgumentUUID(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["nbt"] =
            Function { id: String? -> ArgumentNbtTag(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["nbtcompound"] =
            Function { id: String? -> ArgumentNbtCompoundTag(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["relativeblockposition"] =
            Function { id: String? -> ArgumentRelativeBlockPosition(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["relativevec3"] =
            Function { id: String? -> ArgumentRelativeVec3(net.minestom.server.command.builder.parser.id) }
        ARGUMENT_FUNCTION_MAP["relativevec2"] =
            Function { id: String? -> ArgumentRelativeVec2(net.minestom.server.command.builder.parser.id) }
    }

    @ApiStatus.Experimental
    fun generate(format: String): Array<Argument<*>> {
        val result: MutableList<Argument<*>> = ArrayList()

        // 0 = no state
        // 1 = inside angle bracket <>
        var state = 0
        // function to create an argument from its identifier
        // not null during state 1
        var argumentFunction: Function<String, Argument<*>>? = null
        var builder = StringBuilder()

        // test: Integer<name> String<hey>
        for (i in 0 until format.length) {
            val c = format[i]

            // No state
            if (state == 0) {
                if (c == ' ') {
                    // Use literal as the default argument
                    val argument = builder.toString()
                    if (argument.length != 0) {
                        result.add(ArgumentLiteral(argument))
                        builder = StringBuilder()
                    }
                } else if (c == '<') {
                    // Retrieve argument type
                    val argument = builder.toString()
                    argumentFunction = ARGUMENT_FUNCTION_MAP[argument.toLowerCase(Locale.ROOT)]
                    requireNotNull(argumentFunction) { "error invalid argument name: $argument" }
                    builder = StringBuilder()
                    state = 1
                } else {
                    // Append to builder
                    builder.append(c)
                }
                continue
            }

            // Inside bracket <>
            if (state == 1) {
                if (c == '>') {
                    val param = builder.toString()
                    // TODO argument options
                    val argument = argumentFunction!!.apply(param)
                    result.add(argument)
                    builder = StringBuilder()
                    state = 0
                } else {
                    builder.append(c)
                }
                continue
            }
        }

        // Use remaining as literal if present
        if (state == 0) {
            val argument = builder.toString()
            if (argument.length != 0) {
                result.add(ArgumentLiteral(argument))
            }
        }
        return result.toArray { _Dummy_.__Array__() }
    }

    fun validate(
        argument: Argument<*>,
        arguments: Array<Argument<*>>, argIndex: Int,
        inputArguments: Array<String?>, inputIndex: Int
    ): ArgumentResult? {
        var inputIndex = inputIndex
        val end = inputIndex == inputArguments.size
        if (end) // Stop if there is no input to analyze left
            return null

        // the parsed argument value, null if incorrect
        var parsedValue: Any? = null
        // the argument exception, null if the input is correct
        var argumentSyntaxException: ArgumentSyntaxException? = null
        // true if the arg is valid, false otherwise
        var correct = false
        // The raw string value of the argument
        var rawArg: String? = null
        if (argument.useRemaining()) {
            val hasArgs = inputArguments.size > inputIndex
            // Verify if there is any string part available
            if (hasArgs) {
                val builder = StringBuilder()
                // Argument is supposed to take the rest of the command input
                for (i in inputIndex until inputArguments.size) {
                    val arg = inputArguments[i]
                    if (builder.length > 0) builder.append(StringUtils.SPACE)
                    builder.append(arg)
                }
                rawArg = builder.toString()
                try {
                    parsedValue = argument.parse(rawArg)
                    correct = true
                } catch (exception: ArgumentSyntaxException) {
                    argumentSyntaxException = exception
                }
            }
        } else {
            // Argument is either single-word or can accept optional delimited space(s)
            val builder = StringBuilder()
            for (i in inputIndex until inputArguments.size) {
                builder.append(inputArguments[i])
                rawArg = builder.toString()
                try {
                    parsedValue = argument.parse(rawArg)

                    // Prevent quitting the parsing too soon if the argument
                    // does not allow space
                    val lastArgumentIteration = argIndex + 1 == arguments.size
                    if (lastArgumentIteration && i + 1 < inputArguments.size) {
                        if (!argument.allowSpace()) break
                        builder.append(StringUtils.SPACE)
                        continue
                    }
                    correct = true
                    inputIndex = i + 1
                    break
                } catch (exception: ArgumentSyntaxException) {
                    argumentSyntaxException = exception
                    if (!argument.allowSpace()) {
                        // rawArg should be the remaining
                        var j = i + 1
                        while (j < inputArguments.size) {
                            val arg = inputArguments[j]
                            if (builder.length > 0) builder.append(StringUtils.SPACE)
                            builder.append(arg)
                            j++
                        }
                        rawArg = builder.toString()
                        break
                    }
                    builder.append(StringUtils.SPACE)
                }
            }
        }
        val argumentResult = ArgumentResult()
        argumentResult.argument = argument
        argumentResult.correct = correct
        argumentResult.inputIndex = inputIndex
        argumentResult.argumentSyntaxException = argumentSyntaxException
        argumentResult.useRemaining = argument.useRemaining()
        argumentResult.rawArg = rawArg
        argumentResult.parsedValue = parsedValue
        return argumentResult
    }

    class ArgumentResult {
        var argument: Argument<*>? = null
        var correct = false
        var inputIndex = 0
        var argumentSyntaxException: ArgumentSyntaxException? = null
        var useRemaining = false
        var rawArg: String? = null

        // If correct
        var parsedValue: Any? = null
    }
}