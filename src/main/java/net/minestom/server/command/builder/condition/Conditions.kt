package net.minestom.server.command.builder.condition

import net.kyori.adventure.text.Component
import net.minestom.server.command.CommandSender
import net.minestom.server.command.ConsoleSender
import net.minestom.server.entity.Player

/**
 * Common command conditions
 */
object Conditions {
    @JvmStatic
    fun playerOnly(sender: CommandSender, commandString: String?): Boolean {
        if (sender !is Player) {
            sender.sendMessage(Component.text("The command is only available for players"))
            return false
        }
        return true
    }

    fun consoleOnly(sender: CommandSender, commandString: String?): Boolean {
        if (sender !is ConsoleSender) {
            sender.sendMessage(Component.text("The command is only available form the console"))
            return false
        }
        return true
    }
}