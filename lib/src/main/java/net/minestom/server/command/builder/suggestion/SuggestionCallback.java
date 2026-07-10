package net.minestom.server.command.builder.suggestion;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;

@FunctionalInterface
public interface SuggestionCallback {
    void apply(CommandSender sender, CommandContext context, Suggestion suggestion);
}
