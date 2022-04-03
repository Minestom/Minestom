package net.minestom.server.command.builder.exception

import java.lang.RuntimeException

/**
 * Exception triggered when an [Argument] is wrongly parsed.
 *
 *
 * Retrieved in [ArgumentCallback] defined in [Command.setArgumentCallback].
 *
 *
 * Be aware that the message returned by [.getMessage] is only here for debugging purpose,
 * you should refer to [.getErrorCode] to identify the exceptions.
 */
class ArgumentSyntaxException(
    message: String,
    /**
     * Gets the problematic command input.
     *
     * @return the command input which triggered the exception
     */
    val input: String,
    /**
     * Gets the error code of the exception.
     *
     *
     * The code is decided arbitrary by the argument,
     * check the argument class to know the meaning of each one.
     *
     * @return the argument error code
     */
    val errorCode: Int
) : RuntimeException(message)