package net.minestom.server.command.builder

import net.minestom.server.command.builder.arguments.Argument.id
import net.minestom.server.command.builder.CommandContext
import net.minestom.server.command.builder.arguments.Argument
import net.minestom.server.utils.StringUtils
import java.lang.NullPointerException
import java.util.HashMap
import java.util.function.Supplier

/**
 * Class used to retrieve argument data in a [CommandExecutor].
 *
 *
 * All id are the one specified in the [Argument] constructor.
 *
 *
 * All methods are @[NotNull] in the sense that you should not have to verify their validity since if the syntax
 * is called, it means that all of its arguments are correct. Be aware that trying to retrieve an argument not present
 * in the syntax will result in a [NullPointerException].
 */
class CommandContext(val input: String) {
    val commandName: String
    protected var args: MutableMap<String, Any> = HashMap()
    protected var rawArgs: MutableMap<String, String> = HashMap()
    var returnData: CommandData? = null

    init {
        commandName = input.split(StringUtils.SPACE).toTypedArray()[0]
    }

    operator fun <T> get(argument: Argument<T>): T {
        return get(argument.id)
    }

    operator fun <T> get(identifier: String): T? {
        return args[identifier] as T?
    }

    fun <T> getOrDefault(argument: Argument<T>, defaultValue: T): T {
        return getOrDefault(argument.id, defaultValue)
    }

    fun <T> getOrDefault(identifier: String, defaultValue: T): T {
        var value: T
        return if (get<T>(identifier).also { value = it } != null) value else defaultValue
    }

    fun has(argument: Argument<*>): Boolean {
        return args.containsKey(argument.id)
    }

    fun has(identifier: String): Boolean {
        return args.containsKey(identifier)
    }

    val map: Map<String, Any>
        get() = args

    fun copy(context: CommandContext) {
        args = context.args
        rawArgs = context.rawArgs
    }

    fun getRaw(argument: Argument<*>): String? {
        return rawArgs[argument.id]
    }

    fun getRaw(identifier: String): String {
        return rawArgs.computeIfAbsent(identifier) { s: String? ->
            throw NullPointerException(
                "The argument with the id '$identifier' has no value assigned, be sure to check your arguments id, your syntax, and that you do not change the argument id dynamically."
            )
        }
    }

    fun setArg(id: String, value: Any, rawInput: String) {
        args[id] = value
        rawArgs[id] = rawInput
    }

    protected fun clear() {
        args.clear()
    }

    fun retrieveDefaultValues(defaultValuesMap: Map<String, Supplier<Any>>?) {
        if (defaultValuesMap == null) return
        for ((key, supplier) in defaultValuesMap) {
            if (!args.containsKey(key)) {
                args[key] = supplier.get()
            }
        }
    }
}