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
import net.minestom.server.command.builder.Command
import net.minestom.server.command.builder.parser.CommandSuggestionHolder
import net.minestom.server.command.builder.parser.ArgumentParser.ArgumentResult
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.command.builder.parser.ArgumentQueryResult
import java.util.concurrent.ConcurrentHashMap
import net.minestom.server.command.builder.arguments.minecraft.ArgumentEntity
import java.lang.StringBuilder
import net.minestom.server.command.builder.arguments.ArgumentLiteral
import java.lang.IllegalArgumentException
import java.util.function.IntFunction
import net.minestom.server.command.builder.exception.ArgumentSyntaxException
import net.minestom.server.utils.StringUtils
import java.util.*
import java.util.function.Predicate

/**
 * Class used to parse complete command inputs.
 */
object CommandParser {
    private fun recursiveCommandQuery(
        dispatcher: CommandDispatcher,
        parents: MutableList<Command>,
        parentCommand: Command?, commandName: String, args: Array<String?>
    ): CommandQueryResult? {
        val command = parentCommand ?: dispatcher.findCommand(commandName) ?: return null
        val commandQueryResult = CommandQueryResult(parents, command, commandName, args)
        // Search for subcommand
        if (args.size > 0) {
            val subCommandName = args[0]!!
            for (subcommand in command.subcommands) {
                if (Command.isValidName(subcommand, subCommandName)) {
                    val subArgs = Arrays.copyOfRange(args, 1, args.size)
                    parents.add(command)
                    return recursiveCommandQuery(dispatcher, parents, subcommand, subCommandName, subArgs)
                }
            }
        }
        return commandQueryResult
    }

    @JvmStatic
    fun findCommand(dispatcher: CommandDispatcher, input: String): CommandQueryResult? {
        val parts = input.split(StringUtils.SPACE).toTypedArray()
        val commandName = parts[0]
        val args = arrayOfNulls<String>(parts.size - 1)
        System.arraycopy(parts, 1, args, 0, args.size)
        val parents: MutableList<Command> = ArrayList()
        return recursiveCommandQuery(dispatcher, parents, null, commandName, args)
    }

    @JvmStatic
    fun parse(
        syntax: CommandSyntax?, commandArguments: Array<Argument<*>>, inputArguments: Array<String?>,
        commandString: String,
        validSyntaxes: MutableList<ValidSyntaxHolder?>?,
        syntaxesSuggestions: Int2ObjectRBTreeMap<CommandSuggestionHolder?>?
    ) {
        val argumentValueMap: MutableMap<Argument<*>?, ArgumentResult> = HashMap()
        var syntaxCorrect = true
        // The current index in the raw command string arguments
        var inputIndex = 0
        var useRemaining = false
        // Check the validity of the arguments...
        for (argIndex in commandArguments.indices) {
            val argument = commandArguments[argIndex]
            val argumentResult = ArgumentParser.validate(
                argument,
                commandArguments,
                argIndex,
                inputArguments,
                inputIndex
            )
                ?: break

            // Update local var
            useRemaining = argumentResult.useRemaining
            inputIndex = argumentResult.inputIndex
            if (argumentResult.correct) {
                argumentValueMap[argumentResult.argument] = argumentResult
            } else {
                // Argument is not correct, add it to the syntax suggestion with the number
                // of correct argument(s) and do not check the next syntax argument
                syntaxCorrect = false
                if (syntaxesSuggestions != null) {
                    syntaxesSuggestions[argIndex] =
                        CommandSuggestionHolder(syntax, argumentResult.argumentSyntaxException, argIndex)
                }
                break
            }
        }

        // Add the syntax to the list of valid syntaxes if correct
        if (syntaxCorrect) {
            if (commandArguments.size == argumentValueMap.size || useRemaining) {
                validSyntaxes?.add(ValidSyntaxHolder(commandString, syntax, argumentValueMap))
            }
        }
    }

    /**
     * Retrieves from the valid syntax map the arguments condition result and get the one with the most
     * valid arguments.
     *
     * @param validSyntaxes the list containing all the valid syntaxes
     * @param context       the recipient of the argument parsed values
     * @return the command syntax with all of its arguments correct and with the most arguments count, null if not any
     */
    @JvmStatic
    fun findMostCorrectSyntax(
        validSyntaxes: List<ValidSyntaxHolder>,
        context: CommandContext
    ): ValidSyntaxHolder? {
        if (validSyntaxes.isEmpty()) {
            return null
        }
        var finalSyntax: ValidSyntaxHolder? = null
        var maxArguments = 0
        var finalContext: CommandContext? = null
        for (validSyntaxHolder in validSyntaxes) {
            val argsValues = validSyntaxHolder.argumentResults()
            val argsSize = argsValues.size

            // Check if the syntax has more valid arguments
            if (argsSize > maxArguments) {
                finalSyntax = validSyntaxHolder
                maxArguments = argsSize

                // Fill arguments map
                finalContext = CommandContext(validSyntaxHolder.commandString())
                for ((argument, argumentResult) in argsValues) {
                    finalContext.setArg(argument.id, argumentResult.parsedValue, argumentResult.rawArg)
                }
            }
        }

        // Get the arguments values
        if (finalSyntax != null) {
            context.copy(finalContext!!)
        }
        return finalSyntax
    }

    @JvmStatic
    fun findEligibleArgument(
        command: Command, args: Array<String?>, commandString: String?,
        trailingSpace: Boolean, forceCorrect: Boolean,
        syntaxPredicate: Predicate<CommandSyntax?>,
        argumentPredicate: Predicate<Argument<*>?>
    ): ArgumentQueryResult? {
        val syntaxes = command.syntaxes
        val suggestions = Int2ObjectRBTreeMap<ArgumentQueryResult>(Collections.reverseOrder())
        for (syntax in syntaxes) {
            if (!syntaxPredicate.test(syntax)) {
                continue
            }
            val context = CommandContext(commandString!!)
            val commandArguments = syntax.arguments
            var inputIndex = 0
            var maxArg: ArgumentQueryResult? = null
            var maxArgIndex = 0
            for (argIndex in commandArguments.indices) {
                val argument = commandArguments[argIndex]
                var argumentResult = ArgumentParser.validate(argument, commandArguments, argIndex, args, inputIndex)
                if (argumentResult == null) {
                    // Nothing to analyze, create a dummy object
                    argumentResult = ArgumentResult()
                    argumentResult.argument = argument
                    argumentResult.correct = false
                    argumentResult.inputIndex = inputIndex
                    argumentResult.rawArg = ""
                }

                // Update local var
                inputIndex = argumentResult.inputIndex
                if (argumentResult.correct) {
                    // Fill context
                    context.setArg(argument.id, argumentResult.parsedValue, argumentResult.rawArg)
                }

                // Save result
                if ((!forceCorrect || argumentResult.correct) &&
                    argumentPredicate.test(argument)
                ) {
                    maxArg = ArgumentQueryResult(syntax, argument, context, argumentResult.rawArg)
                    maxArgIndex = argIndex
                }

                // Don't compute following arguments if the syntax is incorrect
                if (!argumentResult.correct) {
                    break
                }

                // Don't compute unrelated arguments
                val isLast = inputIndex == args.size
                if (isLast && !trailingSpace) {
                    break
                }
            }
            if (maxArg != null) {
                suggestions[maxArgIndex] = maxArg
            }
        }
        if (suggestions.isEmpty()) {
            // No suggestion
            return null
        }
        val max = suggestions.firstIntKey()
        return suggestions[max]
    }
}