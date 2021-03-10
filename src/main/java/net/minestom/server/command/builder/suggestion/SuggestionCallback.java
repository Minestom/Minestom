package net.minestom.server.command.builder.suggestion;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.CommandContext;
import org.jetbrains.annotations.NotNull;

@FunctionalInterface
public interface SuggestionCallback {
    void apply(@NotNull CommandSender sender, @NotNull CommandContext context, @NotNull Suggestion suggestion);
}
