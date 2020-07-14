package net.minestom.server.command.builder.condition;

import net.minestom.server.command.CommandSender;

public interface CommandCondition {
    boolean apply(CommandSender source);
}
