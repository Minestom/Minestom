package net.minestom.server.command.builder.condition

import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender

/**
 * Used to know if the [CommandSender] is allowed to run the command or a specific syntax.
 */
fun interface CommandCondition {
    /**
     * Called when the sender permission needs to be checked.
     *
     *
     * The first time will be during player connection in order to know
     * if the command/syntax should be displayed as tab-completion suggestion,
     * `commandString` will be null in this case. (It is also possible for the command string
     * to be null if a new command packet is built)
     *
     *
     * Otherwise, `commandString` will never be null
     * but will instead be the raw command string given by the sender.
     * You should in this case warn the sender (eg by sending a message) if the condition is unsuccessful.
     *
     * @param sender        the sender of the command
     * @param commandString the raw command string,
     * null if this is an access request
     * @return true if the sender has the right to use the command, false otherwise
     */
    fun canUse(sender: CommandSender, commandString: String?): Boolean
}