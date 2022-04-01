package net.minestom.server.command.builder

import net.minestom.server.command.CommandSender
import net.minestom.server.command.builder.exception.ArgumentSyntaxException

/**
 * Callback executed when an error is found within the [Argument].
 */
fun interface ArgumentCallback {
    /**
     * Executed when an error is found.
     *
     * @param sender    the sender which executed the command
     * @param exception the exception containing the message, input and error code related to the issue
     */
    fun apply(sender: CommandSender, exception: ArgumentSyntaxException)
}