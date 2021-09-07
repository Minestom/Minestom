package net.minestom.server.event.command;

import net.minestom.server.command.CommandSender;
import net.minestom.server.event.Event;
import org.jetbrains.annotations.NotNull;

/**
 * Called every time a sender sends a command.
 */
interface CommandEvent extends Event {
    @NotNull CommandSender getSender();
}