package net.minestom.server.command.builder;

import net.minestom.server.command.CommandSender;

@FunctionalInterface
public interface CommandExecutor {
    void apply(CommandSender source, Arguments args);
}