package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;

public interface CommandExecutor {
    void apply(CommandSender source, Arguments args);
}